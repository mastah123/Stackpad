package com.stackpad.app.ui.components

import androidx.compose.runtime.compositionLocalOf
import com.stackpad.app.data.media.ThumbnailStore

/** Where note cards get small picture thumbnails from; null in previews and tests. */
val LocalThumbnails = compositionLocalOf<ThumbnailStore?> { null }
