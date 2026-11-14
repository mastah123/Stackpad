package com.stackpad.app.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import com.stackpad.app.domain.search.SearchQuery

/** [text] with every occurrence of the search [words] marked (background and bold). */
fun highlightMatches(text: String, words: List<String>, background: Color): AnnotatedString {
    if (words.isEmpty()) return AnnotatedString(text)
    val ranges = SearchQuery.ranges(text, words)
    if (ranges.isEmpty()) return AnnotatedString(text)
    return buildAnnotatedString {
        append(text)
        val style = SpanStyle(background = background, fontWeight = FontWeight.Bold)
        ranges.forEach { addStyle(style, it.first, it.last + 1) }
    }
}
