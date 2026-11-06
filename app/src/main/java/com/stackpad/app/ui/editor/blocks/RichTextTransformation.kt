package com.stackpad.app.ui.editor.blocks

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import com.stackpad.app.data.model.SpanType
import com.stackpad.app.data.model.TextSpan
import com.stackpad.app.domain.text.UrlUtils

/**
 * Renders bold/italic/underline spans and highlights detected URLs while the text stays
 * editable. The text itself is unchanged, so offsets map one-to-one.
 */
class RichTextTransformation(
    private val spans: List<TextSpan>,
    private val linkColor: Color,
) : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val styled = buildAnnotatedString {
            append(raw)
            spans.forEach { span ->
                val start = span.start.coerceIn(0, raw.length)
                val end = span.end.coerceIn(0, raw.length)
                if (end > start) addStyle(span.type.toSpanStyle(), start, end)
            }
            UrlUtils.findUrls(raw).forEach { url ->
                addStyle(
                    SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline),
                    url.start,
                    url.end,
                )
            }
        }
        return TransformedText(styled, OffsetMapping.Identity)
    }

    private fun SpanType.toSpanStyle(): SpanStyle = when (this) {
        SpanType.BOLD -> SpanStyle(fontWeight = FontWeight.Bold)
        SpanType.ITALIC -> SpanStyle(fontStyle = FontStyle.Italic)
        SpanType.UNDERLINE -> SpanStyle(textDecoration = TextDecoration.Underline)
    }
}
