package com.stackpad.app.ui.notes

import android.util.LruCache
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.stackpad.app.data.BlockJsonCodec
import com.stackpad.app.data.model.NoteSummary
import com.stackpad.app.data.model.SpanType
import com.stackpad.app.data.model.TextLevel
import com.stackpad.app.data.model.TextSpan
import com.stackpad.app.domain.blocks.BlockTone
import com.stackpad.app.domain.export.DocItem
import com.stackpad.app.domain.preview.NotePreview
import com.stackpad.app.ui.components.LocalThumbnails
import com.stackpad.app.ui.theme.container
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private val previewCache = LruCache<String, List<DocItem>>(300)

/**
 * The formatted items for [note]'s card. Cards are built while scrolling, so items are cached and
 * a note is decoded off the main thread only the first time (null until it is ready).
 */
@Composable
fun rememberPreviewItems(note: NoteSummary, maxItems: Int): List<DocItem>? {
    val key = "${note.id}:${note.updatedAt}:$maxItems"
    val state by produceState(initialValue = previewCache.get(key), key) {
        if (value == null) {
            value = withContext(Dispatchers.Default) {
                NotePreview.from(BlockJsonCodec.decode(note.blocksJson), maxItems)
            }.also { previewCache.put(key, it) }
        }
    }
    return state
}

/** A small formatted preview, cut off at [maxHeight] with a fade. */
@Composable
fun NotePreviewView(items: List<DocItem>, maxHeight: Dp, fadeInto: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(max = maxHeight)
            .clipToBounds()
            .fadeBottom(fadeInto),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        items.forEach { PreviewItem(it) }
    }
}

/** Fades the last stretch of the content into [color] so a cut-off preview does not end abruptly. */
private fun Modifier.fadeBottom(color: Color): Modifier = drawWithContent {
    drawContent()
    val fade = 18.dp.toPx()
    if (size.height > fade) {
        drawRect(
            brush = Brush.verticalGradient(listOf(Color.Transparent, color), startY = size.height - fade, endY = size.height),
            topLeft = Offset(0f, size.height - fade),
            size = androidx.compose.ui.geometry.Size(size.width, fade),
        )
    }
}

@Composable
private fun PreviewItem(item: DocItem) {
    val colors = MaterialTheme.colorScheme
    val body = MaterialTheme.typography.bodySmall
    when (item) {
        is DocItem.Heading -> Text(
            text = styled(item.text, item.spans),
            style = when (item.level) {
                TextLevel.H1 -> MaterialTheme.typography.titleMedium
                TextLevel.H2 -> MaterialTheme.typography.titleSmall
                else -> MaterialTheme.typography.labelLarge
            }.copy(fontWeight = FontWeight.Bold),
            color = colors.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        is DocItem.Paragraph -> Text(
            text = styled(item.text, item.spans),
            style = body,
            color = colors.onSurface,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
        is DocItem.Row -> Row {
            Text(item.marker, style = body, color = colors.primary, modifier = Modifier.width(20.dp))
            Text(item.text, style = body, color = colors.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        is DocItem.Checklist -> Card(BlockTone.CHECKLIST) {
            if (item.title.isNotBlank()) {
                Text(item.title, style = body.copy(fontWeight = FontWeight.Bold), color = Color(BlockTone.CHECKLIST.argb), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            item.tasks.forEach { task ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (task.done) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                        contentDescription = null,
                        tint = Color(BlockTone.CHECKLIST.argb),
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = task.text,
                        style = body.copy(textDecoration = if (task.done) TextDecoration.LineThrough else null),
                        color = if (task.done) colors.onSurfaceVariant else colors.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
            }
        }
        is DocItem.Quote -> Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clip(RoundedCornerShape(6.dp))
                .background(BlockTone.QUOTE.container()),
        ) {
            Box(Modifier.width(3.dp).fillMaxHeight().background(Color(BlockTone.QUOTE.argb)))
            Text(
                text = item.text,
                style = body.copy(fontStyle = FontStyle.Italic),
                color = colors.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            )
        }
        is DocItem.Code -> Card(item.tone) {
            Text(
                text = item.text,
                style = body.copy(fontFamily = if (item.tone == BlockTone.CODE) FontFamily.Monospace else FontFamily.Default, fontSize = 11.sp),
                color = colors.onSurface,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
            )
        }
        is DocItem.Link -> Card(BlockTone.LINK) {
            Text(
                text = item.title.ifBlank { item.url },
                style = body.copy(fontWeight = FontWeight.Bold),
                color = Color(BlockTone.LINK.argb),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        is DocItem.Image -> PreviewImage(item.fileName)
        is DocItem.Table -> PreviewTable(item)
        is DocItem.Audio -> Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(BlockTone.AUDIO.container())
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(16.dp).clip(RoundedCornerShape(50)).background(Color(BlockTone.AUDIO.argb)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
            }
            Text(item.label, style = body, color = colors.onSurface, modifier = Modifier.padding(horizontal = 6.dp), maxLines = 1)
        }
        DocItem.Divider -> HorizontalDivider(color = colors.outlineVariant, modifier = Modifier.padding(vertical = 2.dp))
    }
}

/** Small tinted rounded card used by checklists, code, prompts and links. */
@Composable
private fun Card(tone: BlockTone, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(tone.container())
            .padding(horizontal = 6.dp, vertical = 3.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) { content() }
}

@Composable
private fun PreviewImage(fileName: String) {
    val thumbnails = LocalThumbnails.current
    val file by produceState<File?>(null, fileName, thumbnails) { value = thumbnails?.get(fileName) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(PREVIEW_IMAGE_HEIGHT)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        file?.let {
            AsyncImage(model = it, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(PREVIEW_IMAGE_HEIGHT))
        }
    }
}

@Composable
private fun PreviewTable(item: DocItem.Table) {
    val tone = BlockTone.TABLE
    val columns = item.rows.maxOf { it.size }.coerceIn(1, MAX_PREVIEW_COLUMNS)
    val line = Color(tone.argb).copy(alpha = 0.4f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(tone.container()),
    ) {
        item.rows.forEachIndexed { r, row ->
            Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                for (c in 0 until columns) {
                    Text(
                        text = row.getOrElse(c) { "" },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (r == 0) FontWeight.Bold else FontWeight.Normal,
                        ),
                        color = if (r == 0) Color(tone.argb) else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .background(if (r == 0) Color(tone.argb).copy(alpha = 0.15f) else Color.Transparent)
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                    )
                    if (c < columns - 1) Box(Modifier.width(1.dp).fillMaxHeight().background(line))
                }
            }
            if (r < item.rows.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(line))
        }
    }
}

private fun styled(text: String, spans: List<TextSpan>): AnnotatedString = buildAnnotatedString {
    append(text)
    spans.forEach { span ->
        val start = span.start.coerceIn(0, text.length)
        val end = span.end.coerceIn(0, text.length)
        if (end > start) {
            addStyle(
                when (span.type) {
                    SpanType.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
                    SpanType.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
                    SpanType.UNDERLINE -> SpanStyle(textDecoration = TextDecoration.Underline)
                },
                start,
                end,
            )
        }
    }
}

private val PREVIEW_IMAGE_HEIGHT = 90.dp
private const val MAX_PREVIEW_COLUMNS = 4
