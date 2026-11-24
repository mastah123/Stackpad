package com.stackpad.app.ui.editor.blocks

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.stackpad.app.data.model.ListKind

/**
 * A bullet drawn as a shape (round, ring or square) so every style has the same size and sits
 * in the middle of a line [lineHeight] tall, whatever glyphs the font has.
 */
@Composable
internal fun BulletMark(kind: ListKind, color: Color, lineHeight: Dp, modifier: Modifier = Modifier) {
    Box(modifier = modifier.height(lineHeight), contentAlignment = Alignment.CenterEnd) {
        when (kind) {
            ListKind.BULLET_HOLLOW -> Box(Modifier.size(7.dp).border(1.5.dp, color, CircleShape))
            ListKind.BULLET_SQUARE -> Box(Modifier.size(6.dp).background(color))
            else -> Box(Modifier.size(7.dp).background(color, CircleShape))
        }
    }
}
