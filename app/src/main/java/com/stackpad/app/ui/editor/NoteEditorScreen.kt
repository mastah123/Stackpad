package com.stackpad.app.ui.editor

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stackpad.app.R
import com.stackpad.app.audio.AudioPlayer
import com.stackpad.app.data.media.MediaStorage
import com.stackpad.app.data.model.SpanType
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.domain.text.SpanEditing
import com.stackpad.app.domain.text.UrlUtils
import com.stackpad.app.ui.components.ConfirmDialog
import com.stackpad.app.ui.components.InlineTextField
import com.stackpad.app.util.openAppSettings
import com.stackpad.app.util.openUrl
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    viewModel: NoteEditorViewModel,
    audioPlayer: AudioPlayer,
    media: MediaStorage,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activeText by viewModel.activeText.collectAsStateWithLifecycle()
    val recording by viewModel.recording.collectAsStateWithLifecycle()
    val typingStyles by viewModel.typingStyles.collectAsStateWithLifecycle()
    val playback by audioPlayer.state.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val snackbar = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    var confirmDelete by remember { mutableStateOf(false) }

    fun showMessage(resId: Int) {
        scope.launch { snackbar.showSnackbar(context.getString(resId)) }
    }

    // Save what has been typed (and keep a running recording) when the app goes to the background.
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.stopRecording()
        viewModel.flush()
    }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message -> snackbar.showSnackbar(context.getString(message.resId)) }
    }

    // Bring a newly added block into view.
    LaunchedEffect(listState) {
        snapshotFlow { state.focusBlockId }.filterNotNull().collectLatest { id ->
            withFrameNanos { }
            val index = state.blocks.indexOfFirst { it.id == id }
            val itemIndex = index + 1 // the title field is item 0
            if (index >= 0 && listState.layoutInfo.visibleItemsInfo.none { it.index == itemIndex }) {
                listState.animateScrollToItem(itemIndex)
            }
        }
    }

    // region Launchers

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) viewModel.importImage(uri)
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        viewModel.onCameraResult(saved)
    }
    val micLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            viewModel.startRecording()
        } else {
            scope.launch {
                val result = snackbar.showSnackbar(
                    message = context.getString(R.string.msg_mic_denied),
                    actionLabel = context.getString(R.string.action_settings),
                    duration = SnackbarDuration.Long,
                )
                if (result == SnackbarResult.ActionPerformed) openAppSettings(context)
            }
        }
    }

    val pickFromGallery = {
        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }
    val takePhoto = {
        try {
            cameraLauncher.launch(viewModel.prepareCameraUri())
        } catch (e: ActivityNotFoundException) {
            viewModel.reportCameraUnavailable()
        }
    }
    val recordAudio = {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) viewModel.startRecording() else micLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }
    val openLink = { url: String ->
        if (!openUrl(context, url)) showMessage(R.string.msg_link_open_failed)
    }

    // endregion

    val formatState = activeText?.let { active ->
        (state.blocks.firstOrNull { it.id == active.blockId } as? TextBlock)?.let { block ->
            // With a selection, show what the selected text has. With only a caret, show the
            // style the next typed character will get.
            val collapsed = active.start == active.end
            val atCaret = typingStyles ?: SpanEditing.stylesAt(block.spans, active.start - 1)
            fun hasStyle(type: SpanType) = if (collapsed) {
                type in atCaret
            } else {
                SpanEditing.isFullyStyled(block.spans, active.start, active.end, type)
            }
            FormatState(
                level = block.level,
                bold = hasStyle(SpanType.BOLD),
                italic = hasStyle(SpanType.ITALIC),
                underline = hasStyle(SpanType.UNDERLINE),
                linkUnderCaret = UrlUtils.findUrls(block.text)
                    .firstOrNull { active.start >= it.start && active.end <= it.end }?.url,
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.editor_title), style = MaterialTheme.typography.titleMedium)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::togglePin) {
                        Icon(
                            imageVector = if (state.pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = stringResource(if (state.pinned) R.string.cd_unpin else R.string.cd_pin),
                            tint = if (state.pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.cd_delete_note))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                Column(modifier = Modifier.navigationBarsPadding().imePadding()) {
                    recording?.let { live ->
                        RecordingBar(state = live, onStop = viewModel::stopRecording, onCancel = viewModel::cancelRecording)
                    }
                    formatState?.let { format ->
                        FormatToolbar(
                            state = format,
                            onToggleLevel = viewModel::toggleLevel,
                            onToggleSpan = viewModel::toggleSpan,
                            onOpenLink = openLink,
                        )
                    }
                    AddBlockBar(
                        recordingActive = recording != null,
                        onAddBlock = viewModel::addBlock,
                        onPickFromGallery = pickFromGallery,
                        onTakePhoto = takePhoto,
                        onRecordAudio = recordAudio,
                    )
                }
            }
        },
    ) { padding ->
        if (!state.loaded) {
            Box(Modifier.fillMaxSize().padding(padding))
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                item(key = "title") {
                    InlineTextField(
                        text = state.title,
                        onTextChange = viewModel::setTitle,
                        placeholder = stringResource(R.string.editor_title_hint),
                        textStyle = MaterialTheme.typography.headlineSmall,
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next,
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                items(state.blocks, key = { it.id }) { block ->
                    EditorBlockItem(
                        block = block,
                        isOnlyBlock = state.blocks.size == 1,
                        focusRequested = state.focusBlockId == block.id,
                        focusCaret = state.focusCaret,
                        playback = playback,
                        viewModel = viewModel,
                        media = media,
                        onOpenUrl = openLink,
                        onCopied = { showMessage(R.string.msg_copied) },
                        // Consecutive text lines sit close together; cards get a little more air.
                        modifier = Modifier.padding(vertical = if (block is TextBlock) 1.dp else 2.dp),
                    )
                }
            }
        }
    }

    if (confirmDelete) {
        ConfirmDialog(
            title = stringResource(R.string.dialog_delete_note_title),
            message = stringResource(R.string.dialog_delete_note_message),
            confirmLabel = stringResource(R.string.action_delete),
            onConfirm = {
                confirmDelete = false
                viewModel.deleteNote(onDeleted = onBack)
            },
            onDismiss = { confirmDelete = false },
        )
    }
}
