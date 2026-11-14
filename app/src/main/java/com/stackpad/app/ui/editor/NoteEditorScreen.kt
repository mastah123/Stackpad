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
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
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
import com.stackpad.app.domain.notes.TrashPolicy
import com.stackpad.app.notifications.ReminderNotifications
import com.stackpad.app.domain.text.SpanEditing
import com.stackpad.app.ui.components.ConfirmDialog
import com.stackpad.app.ui.components.InlineTextField
import com.stackpad.app.util.openAppSettings
import com.stackpad.app.util.openUrl
import com.stackpad.app.util.shareText
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.saveable.rememberSaveable
import com.stackpad.app.domain.security.LockPolicy
import com.stackpad.app.domain.templates.TemplateRules
import com.stackpad.app.security.AppLockController
import com.stackpad.app.ui.components.TextInputDialog
import com.stackpad.app.domain.export.ExportFormat
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import android.os.Build
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * The editor. A locked note shows [NoteLockedGate] instead of its content until the user proves who
 * they are, and locks again after the screen has been away for [LockPolicy.GRACE_MS].
 */
@Composable
fun NoteEditorScreen(
    viewModel: NoteEditorViewModel,
    audioPlayer: AudioPlayer,
    media: MediaStorage,
    appLock: AppLockController,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var unlocked by rememberSaveable { mutableStateOf(false) }
    var stoppedAt by remember { mutableStateOf<Long?>(null) }

    LifecycleEventEffect(Lifecycle.Event.ON_STOP) { stoppedAt = SystemClock.elapsedRealtime() }
    LifecycleEventEffect(Lifecycle.Event.ON_START) {
        if (unlocked && LockPolicy.shouldLock(true, false, stoppedAt, SystemClock.elapsedRealtime())) unlocked = false
        stoppedAt = null
    }

    val gated = state.loaded && state.locked && !unlocked && appLock.canVerify()
    val unlock = {
        appLock.verify(context.getString(R.string.lock_prompt_title)) { ok -> if (ok) unlocked = true }
    }
    LaunchedEffect(gated) { if (gated) unlock() }

    if (gated) {
        NoteLockedGate(onBack = onBack, onUnlock = unlock)
    } else {
        NoteEditorContent(viewModel, audioPlayer, media, appLock, onBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteEditorContent(
    viewModel: NoteEditorViewModel,
    audioPlayer: AudioPlayer,
    media: MediaStorage,
    appLock: AppLockController,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activeText by viewModel.activeText.collectAsStateWithLifecycle()
    val recording by viewModel.recording.collectAsStateWithLifecycle()
    val typingStyles by viewModel.typingStyles.collectAsStateWithLifecycle()
    val canUndo by viewModel.canUndo.collectAsStateWithLifecycle()
    val canRedo by viewModel.canRedo.collectAsStateWithLifecycle()
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val knownTags by viewModel.knownTags.collectAsStateWithLifecycle()
    val playback by audioPlayer.state.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val snackbar = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    var confirmDelete by remember { mutableStateOf(false) }
    var optionsOpen by remember { mutableStateOf(false) }
    var optionRowOpen by remember { mutableStateOf(false) }
    var showTemplates by remember { mutableStateOf(false) }
    var saveTemplateDialog by remember { mutableStateOf(false) }
    var selecting by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(emptySet<String>()) }
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current

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
            val itemIndex = index + (if (state.tags.isEmpty()) 1 else 2) // the optional tags row and the title come first
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
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (!granted) showMessage(R.string.msg_notifications_off)
    }

    // Due dates only produce reminders when notifications are allowed, so ask the first time one is set.
    fun onDueDateSet() {
        if (ReminderNotifications.canNotify(context)) return
        val needsPermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        if (needsPermission) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            showMessage(R.string.msg_notifications_off)
        }
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

    val closeSelection = {
        selecting = false
        selectedIds = emptySet()
    }
    BackHandler(enabled = selecting, onBack = closeSelection)

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
            )
        }
    }

    Scaffold(
        topBar = {
            if (selecting) {
                SelectionTopBar(
                    selectedCount = selectedIds.size,
                    onClose = closeSelection,
                    onSelectAll = { selectedIds = state.blocks.map { it.id }.toSet() },
                    canMoveUp = viewModel.canMove(selectedIds, up = true),
                    canMoveDown = viewModel.canMove(selectedIds, up = false),
                    onMoveUp = { viewModel.moveBlocks(selectedIds, up = true) },
                    onMoveDown = { viewModel.moveBlocks(selectedIds, up = false) },
                    onCopy = {
                        val text = viewModel.exportBlocks(selectedIds)
                        if (text == null) {
                            showMessage(R.string.msg_share_empty)
                        } else {
                            clipboard.setText(AnnotatedString(text))
                            showMessage(R.string.msg_blocks_copied)
                            closeSelection()
                        }
                    },
                )
            } else TopAppBar(
                title = {
                    Text(stringResource(R.string.editor_title), style = MaterialTheme.typography.titleMedium)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::undo, enabled = canUndo) {
                        Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = stringResource(R.string.cd_undo))
                    }
                    IconButton(onClick = viewModel::redo, enabled = canRedo) {
                        Icon(Icons.AutoMirrored.Filled.Redo, contentDescription = stringResource(R.string.cd_redo))
                    }
                    IconButton(onClick = viewModel::togglePin) {
                        Icon(
                            imageVector = if (state.pinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                            contentDescription = stringResource(if (state.pinned) R.string.cd_unpin else R.string.cd_pin),
                            tint = if (state.pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    NoteActionsMenu(
                        options = NoteOptionsState(state.folderId, state.tags, state.color, folders, knownTags),
                        optionsActions = NoteOptionsActions(
                            onSelectFolder = viewModel::setFolder,
                            onCreateFolder = viewModel::createFolder,
                            onAddTag = viewModel::addTag,
                            onRemoveTag = viewModel::removeTag,
                            onSelectColor = viewModel::setColor,
                        ),
                        optionsOpen = optionsOpen,
                        onOptionsOpenChange = { optionsOpen = it },
                        locked = state.locked,
                        onToggleLock = {
                            if (!appLock.canVerify()) {
                                showMessage(R.string.msg_lock_unavailable)
                            } else {
                                appLock.verify(context.getString(R.string.lock_prompt_title)) { ok ->
                                    if (ok) {
                                        val nowLocked = !viewModel.state.value.locked
                                        viewModel.toggleLocked()
                                        showMessage(if (nowLocked) R.string.msg_note_lock_on else R.string.msg_note_lock_off)
                                    }
                                }
                            }
                        },
                        onInsertTemplate = { showTemplates = true },
                        onSaveTemplate = { saveTemplateDialog = true },
                        onSelectBlocks = {
                            focusManager.clearFocus()
                            selectedIds = emptySet()
                            selecting = true
                        },
                        onCopy = {
                            val text = viewModel.exportText(ExportFormat.PLAIN)
                            if (text == null) {
                                showMessage(R.string.msg_share_empty)
                            } else {
                                clipboard.setText(AnnotatedString(text))
                                showMessage(R.string.msg_note_copied)
                            }
                        },
                        onShare = { format ->
                            val text = viewModel.exportText(format)
                            val shared = text != null &&
                                shareText(context, state.title.ifBlank { context.getString(R.string.note_untitled) }, text)
                            if (!shared) {
                                scope.launch {
                                    snackbar.showSnackbar(
                                        context.getString(if (text == null) R.string.msg_share_empty else R.string.msg_share_failed),
                                    )
                                }
                            }
                        },
                        onTrash = { confirmDelete = true },
                    )
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (!selecting) Surface(color = MaterialTheme.colorScheme.surface, tonalElevation = 2.dp) {
                Column(modifier = Modifier.navigationBarsPadding().imePadding()) {
                    recording?.let { live ->
                        RecordingBar(state = live, onStop = viewModel::stopRecording, onCancel = viewModel::cancelRecording)
                    }
                    // The text toolbar steps aside while an option row (List, Image, Cards) or a recording is open.
                    formatState?.takeIf { !optionRowOpen && recording == null }?.let { format ->
                        FormatToolbar(
                            state = format,
                            onToggleLevel = viewModel::toggleLevel,
                            onToggleSpan = viewModel::toggleSpan,
                        )
                    }
                    AddBlockBar(
                        recordingActive = recording != null,
                        onAddBlock = viewModel::addBlock,
                        onPickFromGallery = pickFromGallery,
                        onTakePhoto = takePhoto,
                        onRecordAudio = recordAudio,
                        onOptionRowChange = { optionRowOpen = it },
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
                if (state.tags.isNotEmpty()) {
                    item(key = "tags") {
                        NoteTagsRow(tags = state.tags, onClick = { optionsOpen = true })
                    }
                }
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
                    SelectableBlock(
                        selecting = selecting,
                        selected = block.id in selectedIds,
                        onToggle = {
                            selectedIds = if (block.id in selectedIds) selectedIds - block.id else selectedIds + block.id
                        },
                    ) {
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
                        onDueDateSet = ::onDueDateSet,
                        // Consecutive text lines sit close together; cards get a little more air.
                        modifier = Modifier.padding(vertical = if (block is TextBlock) 1.dp else 2.dp),
                    )
                    }
                }
            }
        }
    }

    if (showTemplates) {
        TemplatePickerSheet(
            templates = templates,
            onPick = {
                viewModel.applyTemplate(it)
                showTemplates = false
            },
            onDelete = { viewModel.deleteTemplate(it.id) },
            onDismiss = { showTemplates = false },
        )
    }

    if (saveTemplateDialog) {
        TextInputDialog(
            title = stringResource(R.string.action_save_template),
            initialText = state.title,
            placeholder = stringResource(R.string.template_name_hint),
            confirmLabel = stringResource(R.string.action_save),
            maxLength = TemplateRules.MAX_NAME_LENGTH,
            onConfirm = {
                saveTemplateDialog = false
                viewModel.saveAsTemplate(it)
            },
            onDismiss = { saveTemplateDialog = false },
        )
    }

    if (confirmDelete) {
        ConfirmDialog(
            title = stringResource(R.string.dialog_trash_note_title),
            message = stringResource(R.string.dialog_trash_note_message, TrashPolicy.RETENTION_DAYS),
            confirmLabel = stringResource(R.string.action_move_to_trash),
            onConfirm = {
                confirmDelete = false
                viewModel.trashNote(onTrashed = onBack)
            },
            onDismiss = { confirmDelete = false },
        )
    }
}
