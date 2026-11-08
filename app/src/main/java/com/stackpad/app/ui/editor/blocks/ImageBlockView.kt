package com.stackpad.app.ui.editor.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.stackpad.app.R
import com.stackpad.app.ui.components.InlineTextField
import java.io.File

/**
 * Full-width image. Tap it to view full screen with zoom. The pencil adds or edits a caption
 * shown under the image, and the "x" removes the image (and its file).
 */
@Composable
fun ImageBlockView(
    file: File,
    caption: String,
    onCaptionChange: (String) -> Unit,
    onBlockFocused: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(10.dp)
    val available = remember(file) { file.exists() && file.length() > 0 }
    var viewing by remember { mutableStateOf(false) }
    var editingCaption by remember { mutableStateOf(false) }
    val captionFocus = remember { FocusRequester() }
    val showCaption = caption.isNotEmpty() || editingCaption

    LaunchedEffect(editingCaption) {
        if (editingCaption) runCatching { captionFocus.requestFocus() }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Box(modifier = Modifier.fillMaxWidth().clip(shape)) {
            if (available) {
                AsyncImage(
                    model = file,
                    contentDescription = stringResource(R.string.cd_image),
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable(
                            onClickLabel = stringResource(R.string.cd_view_image),
                            role = Role.Image,
                        ) { viewing = true },
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 72.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = stringResource(R.string.image_unavailable),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            OverlayButton(
                icon = Icons.Filled.Edit,
                description = stringResource(R.string.cd_edit_caption),
                onClick = { editingCaption = true },
                modifier = Modifier.align(Alignment.TopStart),
            )
            OverlayButton(
                icon = Icons.Filled.Close,
                description = stringResource(R.string.cd_remove_image),
                onClick = onRemove,
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
        if (showCaption) {
            InlineTextField(
                text = caption,
                onTextChange = onCaptionChange,
                placeholder = stringResource(R.string.caption_hint),
                textStyle = MaterialTheme.typography.bodySmall.copy(
                    fontStyle = FontStyle.Italic,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                focusRequester = captionFocus,
                onFocusChange = { focused ->
                    if (focused) onBlockFocused() else if (caption.isEmpty()) editingCaption = false
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp),
            )
        }
    }

    if (viewing) ImageViewer(file = file, caption = caption, onDismiss = { viewing = false })
}

@Composable
private fun OverlayButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(6.dp)
            .size(30.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            modifier = Modifier.size(16.dp),
            tint = Color.White,
        )
    }
}
