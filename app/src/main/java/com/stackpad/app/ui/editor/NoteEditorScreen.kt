package com.stackpad.app.ui.editor

import com.stackpad.app.ui.icons.AppIcons
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import com.stackpad.app.ui.theme.NoteColors
import com.stackpad.app.domain.share.ShareOptions
import com.stackpad.app.data.model.Note
import com.stackpad.app.data.export.GallerySaver
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.layout.onSizeChanged
import com.stackpad.app.ui.components.LocalCaretRevealKey
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
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
import com.stackpad.app.data.model.look
import com.stackpad.app.data.model.TextLevel
import com.stackpad.app.domain.notes.TrashPolicy
import com.stackpad.app.notifications.ReminderNotifications
import com.stackpad.app.domain.text.HeadingSpans
import com.stackpad.app.domain.text.SpanEditing
import com.stackpad.app.ui.components.ConfirmDialog
import com.stackpad.app.ui.components.InlineTextField
import com.stackpad.app.util.openAppSettings
import com.stackpad.app.util.openUrl
import com.stackpad.app.util.shareText
import com.stackpad.app.domain.text.WordCount
import com.stackpad.app.util.shareFile
import com.stackpad.app.data.export.NoteFileExporter
import com.stackpad.app.data.export.NoteTooTallException
import com.stackpad.app.domain.notes.Tags
import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.saveable.rememberSaveable
import com.stackpad.app.domain.security.LockPolicy
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
    val knownTags by viewModel.knownTags.collectAsStateWithLifecycle()
    val playback by audioPlayer.state.collectAsStateWithLifecycle()
    val shareOptions by viewModel.shareOptions.collectAsStateWithLifecycle()
    val askBeforeSharing by viewModel.askBeforeSharing.collectAsStateWithLifecycle()
    val appIsDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val snackbar = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    var confirmDelete by remember { mutableStateOf(false) }
    var barHeight by remember { mutableIntStateOf(0) }
    var newTagDialog by remember { mutableStateOf(false) }
    // The text block the Format chips change; kept after the block loses focus (the drop-down menus take it).
    var formatTarget by remember { mutableStateOf<String?>(null) }
    var selecting by remember { mutableStateOf(false) }
    var selectedIds by remember { mutableStateOf(emptySet<String>()) }
    var showHistory by remember { mutableStateOf(false) }
    var showOptions by remember { mutableStateOf(false) }
    val clipboard = LocalClipboardManager.current

    fun showMessage(resId: Int) {
        scope.launch { snackbar.showSnackbar(context.getString(resId)) }
    }

    val fileExporter = remember { NoteFileExporter(context, media) }

    val gallerySaver = remember { GallerySaver(context) }
    var pendingImageNote by remember { mutableStateOf<Note?>(null) }
    var pendingPdfNote by remember { mutableStateOf<Note?>(null) }
    var pendingOptions by remember { mutableStateOf(ShareOptions()) }
    /** Which share the options sheet is open for, or null when it is closed. */
    var optionsSheetFor by remember { mutableStateOf<ShareTarget?>(null) }

    /** The note's color as 0xAARRGGBB, or null for a plain note. */
    fun accentOf(note: Note): Int? = NoteColors.accent(note.color)?.toArgb()

    /** Renders the note as one PNG, saves it to the gallery, then opens the share sheet. */
    fun shareImage(note: Note, options: ShareOptions, saveToGallery: Boolean) {
        scope.launch {
            snackbar.currentSnackbarData?.dismiss()
            val title = com.stackpad.app.domain.notes.AutoTitle.orDefault(note.title, note.createdAt)
            val result = runCatching { fileExporter.toPng(note, options, appIsDark, accentOf(note)) }
            val file = result.getOrNull()
            if (file == null) {
                showMessage(if (result.exceptionOrNull() is NoteTooTallException) R.string.msg_share_too_tall else R.string.msg_share_render_failed)
                return@launch
            }
            if (saveToGallery) {
                val saved = gallerySaver.savePng(file, "${fileExporter.baseName(note)}-${System.currentTimeMillis()}")
                showMessage(if (saved) R.string.msg_image_saved else R.string.msg_image_save_failed)
            } else {
                showMessage(R.string.msg_image_save_failed)
            }
            if (!shareFile(context, file, "image/png", title)) showMessage(R.string.msg_share_failed)
        }
    }

    val storagePermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        pendingImageNote?.let { shareImage(it, pendingOptions, saveToGallery = granted) }
        pendingImageNote = null
    }

    // The person picks where the PDF is saved; afterwards it is shared too.
    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        val note = pendingPdfNote
        pendingPdfNote = null
        if (uri == null || note == null) return@rememberLauncherForActivityResult
        scope.launch {
            val title = com.stackpad.app.domain.notes.AutoTitle.orDefault(note.title, note.createdAt)
            val result = runCatching { fileExporter.toPdf(note, pendingOptions, appIsDark, accentOf(note)) }
            val file = result.getOrNull()
            if (file == null) {
                showMessage(R.string.msg_share_render_failed)
                return@launch
            }
            val saved = withContext(Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openOutputStream(uri)?.use { out -> file.inputStream().use { it.copyTo(out) } } != null
                }.getOrDefault(false)
            }
            if (!saved) {
                showMessage(R.string.msg_pdf_save_failed)
                return@launch
            }
            showMessage(R.string.msg_pdf_saved)
            if (!shareFile(context, file, "application/pdf", title)) showMessage(R.string.msg_share_failed)
        }
    }

    /** Shares the open note as an image or a PDF with [options]. */
    fun shareVisual(target: ShareTarget, options: ShareOptions) {
        val note = viewModel.exportNote()
        if (note == null) {
            showMessage(R.string.msg_share_empty)
            return
        }
        pendingOptions = options
        if (target == ShareTarget.IMAGE) {
            // Android 9 and older must be allowed to write to the gallery first.
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                pendingImageNote = note
                storagePermission.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                shareImage(note, options, saveToGallery = true)
            }
        } else {
            pendingPdfNote = note
            pdfLauncher.launch(fileExporter.baseName(note))
        }
    }

    fun share(target: ShareTarget) {
        val title = com.stackpad.app.domain.notes.AutoTitle.orDefault(state.title, state.createdAt)
        when (target) {
            ShareTarget.COPY -> {
                val text = viewModel.exportText(ExportFormat.PLAIN)
                if (text == null) {
                    showMessage(R.string.msg_share_empty)
                } else {
                    clipboard.setText(AnnotatedString(text))
                    showMessage(R.string.msg_note_copied)
                }
            }
            ShareTarget.TEXT, ShareTarget.MARKDOWN -> {
                val format = if (target == ShareTarget.TEXT) ExportFormat.PLAIN else ExportFormat.MARKDOWN
                val text = viewModel.exportText(format)
                when {
                    text == null -> showMessage(R.string.msg_share_empty)
                    !shareText(context, title, text) -> showMessage(R.string.msg_share_failed)
                }
            }
            ShareTarget.IMAGE, ShareTarget.PDF ->
                if (askBeforeSharing) optionsSheetFor = target else shareVisual(target, shareOptions)
            ShareTarget.OPTIONS -> optionsSheetFor = target
        }
    }

    optionsSheetFor?.let { target ->
        ShareOptionsSheet(
            initial = shareOptions,
            dontShowAgain = !askBeforeSharing,
            confirmLabel = if (target == ShareTarget.OPTIONS) R.string.share_save_options else R.string.share_confirm,
            onDismiss = { optionsSheetFor = null },
            onConfirm = { options, dontShowAgain ->
                optionsSheetFor = null
                viewModel.saveShareOptions(options, askEachTime = !dontShowAgain)
                if (target != ShareTarget.OPTIONS) shareVisual(target, options)
            },
        )
    }

    // Save what has been typed (and keep a running recording) when the app goes to the background.
    LifecycleEventEffect(Lifecycle.Event.ON_STOP) {
        viewModel.stopRecording()
        viewModel.flush()
    }

    LaunchedEffect(viewModel) {
        viewModel.messages.collect { message -> snackbar.showSnackbar(context.getString(message.resId)) }
    }

    LaunchedEffect(viewModel) {
        viewModel.undoOffers.collect { messageRes ->
            val result = snackbar.showSnackbar(
                message = context.getString(messageRes),
                actionLabel = context.getString(R.string.action_undo),
                duration = SnackbarDuration.Long,
            )
            if (result == SnackbarResult.ActionPerformed) viewModel.undo()
        }
    }

    // Bring a newly added block into view.
    LaunchedEffect(listState) {
        snapshotFlow { state.focusBlockId }.filterNotNull().collectLatest { id ->
            withFrameNanos { }
            val index = state.blocks.indexOfFirst { it.id == id }
            val itemIndex = index + (if (state.tags.isEmpty()) 2 else 3) // the optional tags row, the title and the info line come first
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

    LaunchedEffect(activeText?.blockId) { activeText?.blockId?.let { formatTarget = it } }
    val formatLook = (state.blocks.firstOrNull { it.id == formatTarget } as? TextBlock)?.look()

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
            val pendingHeading = typingStyles?.firstOrNull(HeadingSpans::isHeading)
            val level = when {
                !collapsed -> HeadingSpans.commonLevel(block.text, block.spans, active.start, active.end)
                    ?: TextLevel.PARAGRAPH
                pendingHeading != null -> HeadingSpans.levelOf(pendingHeading)
                else -> HeadingSpans.levelAt(block.text, block.spans, active.start)
            }
            FormatState(
                level = level,
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
                    onSelectRange = {
                        val picked = state.blocks.indices.filter { state.blocks[it].id in selectedIds }
                        if (picked.size >= 2) {
                            selectedIds = state.blocks.slice(picked.first()..picked.last()).map { it.id }.toSet()
                        }
                    },
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
                        Icon(AppIcons.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::undo, enabled = canUndo) {
                        Icon(AppIcons.Undo, contentDescription = stringResource(R.string.cd_undo))
                    }
                    IconButton(onClick = viewModel::redo, enabled = canRedo) {
                        Icon(AppIcons.Redo, contentDescription = stringResource(R.string.cd_redo))
                    }
                    IconButton(onClick = viewModel::togglePin) {
                        Icon(
                            imageVector = if (state.pinned) AppIcons.PushPin else AppIcons.PushPin,
                            contentDescription = stringResource(if (state.pinned) R.string.cd_unpin else R.string.cd_pin),
                            tint = if (state.pinned) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    IconButton(onClick = { focusManager.clearFocus(); showOptions = true }) {
                        Icon(
                            AppIcons.MoreVert,
                            contentDescription = stringResource(R.string.cd_more_actions),
                            tint = if (state.color != 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (!selecting) Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.onSizeChanged { barHeight = it.height },
            ) {
                Column(modifier = Modifier.navigationBarsPadding().imePadding()) {
                    recording?.let { live ->
                        RecordingBar(state = live, onStop = viewModel::stopRecording, onCancel = viewModel::cancelRecording)
                    }
                    AddBlockBar(
                        recordingActive = recording != null,
                        onAddBlock = viewModel::addBlock,
                        onPickFromGallery = pickFromGallery,
                        onTakePhoto = takePhoto,
                        onRecordAudio = recordAudio,
                        format = formatState?.takeIf { recording == null },
                        onToggleLevel = viewModel::toggleLevel,
                        onToggleSpan = viewModel::toggleSpan,
                        look = formatLook,
                        onLookChange = { look -> formatTarget?.let { viewModel.setTextLook(it, look) } },
                    )
                }
            }
        },
    ) { padding ->
        if (!state.loaded) {
            Box(Modifier.fillMaxSize().padding(padding))
        } else {
            CompositionLocalProvider(LocalCaretRevealKey provides barHeight) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp),
            ) {
                item(key = "tags") {
                    NoteTagsRow(
                        tags = state.tags,
                        knownTags = knownTags,
                        onToggle = { tag -> if (tag in state.tags) viewModel.removeTag(tag) else viewModel.addTag(tag) },
                        onRemove = viewModel::removeTag,
                        onNewTag = { newTagDialog = true },
                    )
                }
                item(key = "title") {
                    InlineTextField(
                        text = state.title,
                        onTextChange = viewModel::setTitle,
                        placeholder = remember(state.createdAt) { com.stackpad.app.domain.notes.AutoTitle.of(state.createdAt) },
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
                item(key = "info") {
                    NoteInfoLine(
                        wordCount = remember(state.title, state.blocks) { WordCount.of(state.title, state.blocks) },
                        createdAt = state.createdAt,
                        modifier = Modifier.padding(bottom = 16.dp),
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
    }

    if (newTagDialog) {
        TextInputDialog(
            title = stringResource(R.string.tag_new),
            placeholder = stringResource(R.string.tag_hint),
            confirmLabel = stringResource(R.string.action_add),
            maxLength = Tags.MAX_LENGTH,
            onConfirm = {
                newTagDialog = false
                viewModel.addTag(it)
            },
            onDismiss = { newTagDialog = false },
        )
    }

    if (showOptions) {
        NoteOptionsSheet(
            title = com.stackpad.app.domain.notes.AutoTitle.orDefault(state.title, state.createdAt),
            tags = state.tags,
            knownTags = knownTags,
            createdAt = state.createdAt,
            updatedAt = state.updatedAt,
            color = state.color,
            pinned = state.pinned,
            archived = state.archived,
            locked = state.locked,
            options = remember {
                buildSet {
                    addAll(NoteOption.values())
                    remove(NoteOption.MOVE_TO_FOLDER)
                    if (!com.stackpad.app.util.NoteShortcut.isSupported(context)) remove(NoteOption.ADD_TO_HOME)
                }
            },
            canShare = true,
            onDismiss = { showOptions = false },
            onToggleTag = { tag -> if (tag in state.tags) viewModel.removeTag(tag) else viewModel.addTag(tag) },
            onNewTag = {
                showOptions = false
                newTagDialog = true
            },
            onSelectColor = viewModel::setColor,
            onShare = { target ->
                showOptions = false
                share(target)
            },
            onOption = { option ->
                showOptions = false
                when (option) {
                    NoteOption.PIN -> viewModel.togglePin()
                    NoteOption.ARCHIVE -> viewModel.toggleArchived(onDone = onBack)
                    NoteOption.LOCK ->
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
                    NoteOption.HISTORY -> {
                        focusManager.clearFocus()
                        showHistory = true
                    }
                    NoteOption.DUPLICATE -> viewModel.duplicateNote { ok ->
                        showMessage(if (ok) R.string.msg_note_duplicated else R.string.msg_duplicate_empty)
                    }
                    NoteOption.ADD_TO_HOME -> {
                        viewModel.flush()
                        val label = com.stackpad.app.domain.notes.AutoTitle.orDefault(state.title, state.createdAt)
                        if (!com.stackpad.app.util.NoteShortcut.pin(context, viewModel.noteId, label)) {
                            showMessage(R.string.msg_shortcut_failed)
                        }
                    }
                    NoteOption.SELECT_BLOCKS -> {
                        focusManager.clearFocus()
                        selectedIds = emptySet()
                        selecting = true
                    }
                    NoteOption.MOVE_TO_FOLDER -> Unit
                    NoteOption.TRASH -> confirmDelete = true
                }
            },
        )
    }

    if (showHistory) {
        HistorySheet(
            loadVersions = viewModel::loadVersions,
            onRestore = viewModel::restoreVersion,
            onDismiss = { showHistory = false },
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
