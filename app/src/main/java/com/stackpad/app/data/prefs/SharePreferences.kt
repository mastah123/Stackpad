package com.stackpad.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.stackpad.app.domain.share.ShareOptions
import com.stackpad.app.domain.share.ShareOptionsCodec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Remembers the image and PDF share options and whether the options sheet shows before every share. */
class SharePreferences(private val context: Context) {
    private val optionsKey = stringPreferencesKey("share_options")
    private val askKey = booleanPreferencesKey("share_ask_each_time")

    val options: Flow<ShareOptions> = context.settingsDataStore.data.map { ShareOptionsCodec.decode(it[optionsKey]) }

    /** True (the default) when the options sheet opens before every image or PDF share. */
    val askEachTime: Flow<Boolean> = context.settingsDataStore.data.map { it[askKey] ?: true }

    suspend fun save(options: ShareOptions, askEachTime: Boolean) {
        context.settingsDataStore.edit {
            it[optionsKey] = ShareOptionsCodec.encode(options)
            it[askKey] = askEachTime
        }
    }
}
