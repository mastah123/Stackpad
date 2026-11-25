package com.stackpad.app.ui.editor.blocks

import com.stackpad.app.ui.icons.AppIcons
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalContext
import com.stackpad.app.domain.image.Annotation
import com.stackpad.app.domain.image.ImageFilter
import com.stackpad.app.domain.image.ImageSize
import com.stackpad.app.domain.image.MarkStyle
import com.stackpad.app.domain.image.NormPoint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.stackpad.app.R
import com.stackpad.app.data.media.BitmapLoader
import com.stackpad.app.data.media.ImageTransformer
import com.stackpad.app.domain.image.AspectChoice
import com.stackpad.app.domain.image.CropMath
import com.stackpad.app.domain.image.EditGeometry
import com.stackpad.app.domain.image.EditHistory
import com.stackpad.app.domain.image.ImageEdit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.abs
import kotlin.math.roundToInt

private const val PREVIEW_SIDE = 1600
private const val SNAP_DEGREES = 0.6f

/** The preview picture, or null inside when the file could not be read. */
private class Loaded(val bitmap: Bitmap?)

/** A turned and straightened preview, with the settings it was made for. */
private class Rendered(val key: Triple<Int, Boolean, Float>, val bitmap: Bitmap)

/**
 * Full-screen picture editor: crop (free or fixed shapes), rotate, flip, straighten, undo and reset.
 * [onSave] writes the result and returns whether it worked; the editor closes only then.
 */
@Composable
fun ImageEditorDialog(
    file: File,
    onDismiss: () -> Unit,
    onSave: suspend (ImageEdit) -> Boolean,
) {
    val scope = rememberCoroutineScope()
    var history by remember { mutableStateOf(EditHistory()) }
    var live by remember { mutableStateOf<ImageEdit?>(null) }
    var ratio by remember { mutableStateOf(AspectChoice.FREE) }
    var saving by remember { mutableStateOf(false) }
    var confirmDiscard by remember { mutableStateOf(false) }
    var mode by remember { mutableStateOf(EditMode.ADJUST) }
    var tool by remember { mutableStateOf(MarkupTool.DRAW) }
    var markColor by remember { mutableStateOf(MarkStyle.colors.first()) }
    var markSize by remember { mutableStateOf(MarkStyle.Size.MEDIUM) }
    var textAt by remember { mutableStateOf<NormPoint?>(null) }
    var labelText by remember { mutableStateOf("") }
    val context = LocalContext.current

    val edit = live ?: history.current
    val orientationKey = Triple(edit.quarterTurns, edit.mirrored, edit.straighten)

    val loaded by produceState<Loaded?>(null, file) {
        value = Loaded(withContext(Dispatchers.IO) { runCatching { BitmapLoader.load(file, PREVIEW_SIDE) }.getOrNull() })
    }
    val source = loaded?.bitmap
    val rendered by produceState<Rendered?>(null, source, orientationKey) {
        if (source == null) return@produceState
        val key = orientationKey
        val oriented = ImageEdit(key.first, key.second, key.third)
        value = Rendered(key, withContext(Dispatchers.Default) { ImageTransformer.apply(source, oriented) })
    }

    // The Mark up tab shows the picture as cropped, with hidden regions already applied.
    val markKey = edit.copy(size = ImageSize.ORIGINAL, annotations = edit.annotations.filterIsInstance<Annotation.Obscure>())
    val markBase by produceState<Bitmap?>(null, source, markKey, mode) {
        value = null
        if (source == null || mode != EditMode.MARKUP) return@produceState
        value = withContext(Dispatchers.Default) { ImageTransformer.apply(source, markKey, withMarks = false) }
    }

    val base = source?.let { EditGeometry.baseSize(it.width, it.height, edit) }

    fun commit(next: ImageEdit) {
        // Marks are placed on the finished picture; moving or cropping it would put them in the wrong place.
        val moved = !next.sameGeometry(history.current) && next.annotations.isNotEmpty()
        if (moved) Toast.makeText(context, R.string.image_mark_removed, Toast.LENGTH_SHORT).show()
        history = history.commit(if (moved) next.withoutMarks() else next)
        live = null
    }

    /** Applies a change of orientation or angle, keeping a fixed crop shape. */
    fun commitOrientation(next: ImageEdit) {
        val size = source?.let { EditGeometry.baseSize(it.width, it.height, next) }
        val pixelRatio = size?.let { ratio.pixelRatio(it) }
        val crop = if (size != null && pixelRatio != null) CropMath.fit(next.crop, CropMath.fractionAspect(pixelRatio, size)) else next.crop
        commit(next.cropped(crop))
    }

    fun selectRatio(choice: AspectChoice) {
        ratio = choice
        val size = base ?: return
        val pixelRatio = choice.pixelRatio(size) ?: return
        commit(history.current.cropped(CropMath.fit(history.current.crop, CropMath.fractionAspect(pixelRatio, size))))
    }

    fun requestClose() {
        if (saving) return
        if (history.isChanged) confirmDiscard = true else onDismiss()
    }

    Dialog(
        onDismissRequest = ::requestClose,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false),
    ) {
        Column(modifier = Modifier.fillMaxSize().background(Color.Black).safeDrawingPadding()) {
            EditorTopBar(
                canSave = history.isChanged && !saving,
                saving = saving,
                onClose = ::requestClose,
                onSave = {
                    saving = true
                    scope.launch {
                        val ok = onSave(history.current)
                        saving = false
                        if (ok) onDismiss()
                    }
                },
            )

            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                val shownBitmap = if (mode == EditMode.MARKUP) markBase else rendered?.bitmap
                if (shownBitmap != null && mode == EditMode.MARKUP) {
                    val image = remember(shownBitmap) { shownBitmap.asImageBitmap() }
                    Image(
                        bitmap = image,
                        contentDescription = stringResource(R.string.cd_image),
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize(),
                    )
                    MarkupOverlay(
                        pictureWidth = shownBitmap.width,
                        pictureHeight = shownBitmap.height,
                        marks = edit.annotations.filterNot { it is Annotation.Obscure },
                        tool = tool,
                        color = markColor,
                        size = markSize,
                        onAdd = { commit(history.current.withMark(it)) },
                        onTextAt = { labelText = ""; textAt = it },
                        modifier = Modifier.fillMaxSize(),
                    )
                } else if (shownBitmap != null) {
                    val image = remember(shownBitmap) { shownBitmap.asImageBitmap() }
                    Image(
                        bitmap = image,
                        contentDescription = stringResource(R.string.cd_image),
                        contentScale = ContentScale.Fit,
                        colorFilter = if (edit.filter == ImageFilter.NONE) null else ColorFilter.colorMatrix(ColorMatrix(edit.filter.matrix)),
                        modifier = Modifier.fillMaxSize(),
                    )
                    if (rendered?.key == orientationKey && base != null) {
                        CropOverlay(
                            pictureWidth = shownBitmap.width,
                            pictureHeight = shownBitmap.height,
                            crop = edit.crop,
                            aspect = ratio.pixelRatio(base)?.let { CropMath.fractionAspect(it, base) },
                            onCropChange = { rect -> live = history.current.cropped(rect) },
                            onCropFinished = { live?.let(::commit) },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                } else if (loaded == null || source != null) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text(stringResource(R.string.image_unavailable), color = Color.White)
                }
            }

            EditorModeTabs(mode = mode, enabled = source != null && !saving, onMode = { mode = it })
            if (mode == EditMode.ADJUST) {
                ImageAdjustControls(
                    edit = edit,
                    enabled = source != null && !saving,
                    onFilter = { commit(history.current.withFilter(it)) },
                    onSize = { commit(history.current.withSize(it)) },
                )
            EditorControls(
                history = history,
                edit = edit,
                ratio = ratio,
                enabled = source != null && !saving,
                onRatio = ::selectRatio,
                onStraightenLive = { degrees -> live = history.current.straightened(snap(degrees)) },
                onStraightenDone = { live?.let { commitOrientation(it) } },
                onRotateLeft = { commitOrientation(history.current.rotateLeft()) },
                onRotateRight = { commitOrientation(history.current.rotateRight()) },
                onFlipHorizontal = { commitOrientation(history.current.flipHorizontal()) },
                onFlipVertical = { commitOrientation(history.current.flipVertical()) },
                onUndo = { history = history.undo(); live = null },
                onReset = { history = history.reset(); live = null; ratio = AspectChoice.FREE },
            )
            } else {
                MarkupControls(
                    tool = tool,
                    onTool = { tool = it },
                    color = markColor,
                    onColor = { markColor = it },
                    size = markSize,
                    onSize = { markSize = it },
                    canUndo = history.canUndo,
                    onUndo = { history = history.undo(); live = null },
                    hasMarks = edit.annotations.isNotEmpty(),
                    onClear = { commit(history.current.withoutMarks()) },
                    enabled = source != null && !saving,
                )
            }
        }
    }

    textAt?.let { point ->
        AlertDialog(
            onDismissRequest = { textAt = null },
            title = { Text(stringResource(R.string.image_mark_text_title)) },
            text = {
                OutlinedTextField(
                    value = labelText,
                    onValueChange = { labelText = it },
                    placeholder = { Text(stringResource(R.string.image_mark_text_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(
                    enabled = labelText.isNotBlank(),
                    onClick = {
                        commit(history.current.withMark(Annotation.Label(labelText.trim(), point.x, point.y, markSize.text, markColor)))
                        textAt = null
                    },
                ) { Text(stringResource(R.string.image_mark_text_add)) }
            },
            dismissButton = { TextButton(onClick = { textAt = null }) { Text(stringResource(R.string.action_cancel)) } },
        )
    }

    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text(stringResource(R.string.image_edit_discard_title)) },
            text = { Text(stringResource(R.string.image_edit_discard_message)) },
            confirmButton = {
                TextButton(onClick = { confirmDiscard = false; onDismiss() }) { Text(stringResource(R.string.image_edit_discard)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDiscard = false }) { Text(stringResource(R.string.image_edit_keep_editing)) }
            },
        )
    }
}

/** Angles near zero snap to exactly zero, so the picture is easy to set back level. */
private fun snap(degrees: Float): Float = if (abs(degrees) < SNAP_DEGREES) 0f else (degrees * 2f).roundToInt() / 2f

private enum class EditMode { ADJUST, MARKUP }

/** Switches between the Adjust tools (crop, rotate, filter, size) and Mark up (draw, blur, text). */
@Composable
private fun EditorModeTabs(mode: EditMode, enabled: Boolean, onMode: (EditMode) -> Unit) {
    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = mode == EditMode.ADJUST,
                onClick = { onMode(EditMode.ADJUST) },
                enabled = enabled,
                label = { Text(stringResource(R.string.image_tab_adjust)) },
            )
            FilterChip(
                selected = mode == EditMode.MARKUP,
                onClick = { onMode(EditMode.MARKUP) },
                enabled = enabled,
                label = { Text(stringResource(R.string.image_tab_markup)) },
            )
        }
    }
}

@Composable
private fun EditorTopBar(canSave: Boolean, saving: Boolean, onClose: () -> Unit, onSave: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onClose, enabled = !saving) {
            Icon(AppIcons.Close, contentDescription = stringResource(R.string.image_edit_close), tint = Color.White)
        }
        Text(
            stringResource(R.string.image_edit_title),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f).padding(start = 8.dp),
        )
        if (saving) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp).padding(end = 0.dp), color = Color.White, strokeWidth = 2.dp)
            Box(Modifier.size(16.dp))
        } else {
            IconButton(onClick = onSave, enabled = canSave) {
                Icon(
                    AppIcons.Check,
                    contentDescription = stringResource(R.string.action_save),
                    tint = if (canSave) Color.White else Color.White.copy(alpha = 0.35f),
                )
            }
        }
    }
}

@Composable
private fun EditorControls(
    history: EditHistory,
    edit: ImageEdit,
    ratio: AspectChoice,
    enabled: Boolean,
    onRatio: (AspectChoice) -> Unit,
    onStraightenLive: (Float) -> Unit,
    onStraightenDone: () -> Unit,
    onRotateLeft: () -> Unit,
    onRotateRight: () -> Unit,
    onFlipHorizontal: () -> Unit,
    onFlipVertical: () -> Unit,
    onUndo: () -> Unit,
    onReset: () -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                AspectChoice.entries.forEach { choice ->
                    FilterChip(
                        selected = ratio == choice,
                        onClick = { onRatio(choice) },
                        enabled = enabled,
                        label = { Text(stringResource(choice.label())) },
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(R.string.image_edit_straighten),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(end = 12.dp),
                )
                Slider(
                    value = edit.straighten,
                    onValueChange = onStraightenLive,
                    onValueChangeFinished = onStraightenDone,
                    valueRange = -ImageEdit.MAX_STRAIGHTEN..ImageEdit.MAX_STRAIGHTEN,
                    enabled = enabled,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    degreesLabel(edit.straighten),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ToolButton(AppIcons.RotateLeft, R.string.image_edit_rotate_left, enabled, onRotateLeft)
                ToolButton(AppIcons.RotateRight, R.string.image_edit_rotate_right, enabled, onRotateRight)
                ToolButton(AppIcons.Flip, R.string.image_edit_flip_horizontal, enabled, onFlipHorizontal)
                ToolButton(AppIcons.Flip, R.string.image_edit_flip_vertical, enabled, onFlipVertical, turn = 90f)
                ToolButton(AppIcons.Undo, R.string.action_undo, enabled && history.canUndo, onUndo)
                ToolButton(AppIcons.RestartAlt, R.string.image_edit_reset, enabled && history.isChanged, onReset)
            }
        }
    }
}

@Composable
private fun ToolButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    turn: Float = 0f,
) {
    IconButton(onClick = onClick, enabled = enabled) {
        Icon(icon, contentDescription = stringResource(description), modifier = Modifier.rotate(turn))
    }
}

private fun degreesLabel(degrees: Float): String =
    if (degrees == degrees.roundToInt().toFloat()) "${degrees.roundToInt()}°" else "%.1f°".format(degrees)

private fun AspectChoice.label(): Int = when (this) {
    AspectChoice.FREE -> R.string.image_edit_ratio_free
    AspectChoice.ORIGINAL -> R.string.image_edit_ratio_original
    AspectChoice.SQUARE -> R.string.image_edit_ratio_1_1
    AspectChoice.LANDSCAPE_4_3 -> R.string.image_edit_ratio_4_3
    AspectChoice.PORTRAIT_3_4 -> R.string.image_edit_ratio_3_4
    AspectChoice.LANDSCAPE_16_9 -> R.string.image_edit_ratio_16_9
    AspectChoice.PORTRAIT_9_16 -> R.string.image_edit_ratio_9_16
}
