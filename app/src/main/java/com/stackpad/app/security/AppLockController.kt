package com.stackpad.app.security

import android.app.Activity
import android.app.KeyguardManager
import android.os.Build
import android.os.SystemClock
import android.view.WindowManager
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.stackpad.app.R
import com.stackpad.app.data.prefs.SecurityPreferences
import com.stackpad.app.domain.security.LockPolicy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/** Result of asking to turn the app lock on or off. */
enum class LockChangeResult { CHANGED, CANCELLED, UNAVAILABLE }

/**
 * Keeps the app locked behind the phone's screen lock or biometrics when the user turns that on.
 * Must be created in `Activity.onCreate` (it registers an activity-result launcher).
 * Call [onStart] and [onStop] from the matching activity callbacks.
 */
class AppLockController(
    private val activity: FragmentActivity,
    private val prefs: SecurityPreferences,
) {
    private val _locked = MutableStateFlow(false)
    val locked: StateFlow<Boolean> = _locked.asStateFlow()

    private val _enabled = MutableStateFlow(false)
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private var backgroundedAt: Long? = null
    private var authenticating = false
    private var pending: ((Boolean) -> Unit)? = null

    /** Used on Android 8-10, where the system "confirm credentials" screen is the only option. */
    private val credentialLauncher: ActivityResultLauncher<android.content.Intent> =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            finish(result.resultCode == Activity.RESULT_OK)
        }

    init {
        // One quick read so the very first frame is already locked when it has to be.
        val on = runBlocking { prefs.isAppLockEnabled() } && DeviceCredential.isAvailable(activity)
        _enabled.value = on
        _locked.value = on
        applySecureFlag(on)
        activity.lifecycleScope.launch {
            prefs.appLockEnabled.collect { stored ->
                _enabled.value = stored
                applySecureFlag(stored)
                if (!stored) _locked.value = false
            }
        }
    }

    fun onStart() {
        if (_enabled.value && !DeviceCredential.isAvailable(activity)) {
            // The phone's screen lock was removed: keep the user out of a lock they cannot satisfy.
            _locked.value = false
            activity.lifecycleScope.launch { prefs.setAppLockEnabled(false) }
            return
        }
        if (authenticating) return
        val lock = LockPolicy.shouldLock(
            enabled = _enabled.value,
            alreadyLocked = _locked.value,
            backgroundedAt = backgroundedAt,
            now = SystemClock.elapsedRealtime(),
        )
        if (lock) {
            _locked.value = true
            unlock()
        }
    }

    fun onStop() {
        if (!authenticating) backgroundedAt = SystemClock.elapsedRealtime()
    }

    /** Asks for the screen lock to open the app. Called automatically and by the Unlock button. */
    fun unlock() {
        authenticate(activity.getString(R.string.lock_prompt_title)) { ok ->
            if (ok) {
                _locked.value = false
                backgroundedAt = null
            }
        }
    }

    /** Turns the lock on or off after the user proves who they are. */
    fun requestChange(enable: Boolean, onResult: (LockChangeResult) -> Unit) {
        if (!DeviceCredential.isAvailable(activity)) {
            onResult(LockChangeResult.UNAVAILABLE)
            return
        }
        val title = activity.getString(if (enable) R.string.lock_prompt_enable else R.string.lock_prompt_disable)
        authenticate(title) { ok ->
            if (!ok) {
                onResult(LockChangeResult.CANCELLED)
                return@authenticate
            }
            activity.lifecycleScope.launch {
                prefs.setAppLockEnabled(enable)
                _locked.value = false
                backgroundedAt = null
                onResult(LockChangeResult.CHANGED)
            }
        }
    }

    private fun authenticate(title: String, onDone: (Boolean) -> Unit) {
        if (authenticating) return
        authenticating = true
        pending = onDone
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val info = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setAllowedAuthenticators(DeviceCredential.AUTHENTICATORS)
                .build()
            val prompt = BiometricPrompt(
                activity,
                ContextCompat.getMainExecutor(activity),
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = finish(true)
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = finish(false)
                },
            )
            prompt.authenticate(info)
        } else {
            val keyguard = activity.getSystemService(KeyguardManager::class.java)
            @Suppress("DEPRECATION")
            val intent = keyguard?.createConfirmDeviceCredentialIntent(title, null)
            if (intent == null) finish(false) else credentialLauncher.launch(intent)
        }
    }

    private fun finish(ok: Boolean) {
        authenticating = false
        val callback = pending
        pending = null
        callback?.invoke(ok)
    }

    private fun applySecureFlag(on: Boolean) {
        // Hides the app in the recent-apps screen and blocks screenshots while the lock is on.
        if (on) {
            activity.window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        } else {
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
}
