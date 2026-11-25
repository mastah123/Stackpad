package com.stackpad.app.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * The app's icons: thin rounded line icons from Lucide (ISC license, see docs/licenses).
 * Names follow the purpose of the icon; each is drawn once on first use. The color comes from the
 * `tint` of the Icon that shows it.
 */
object AppIcons {
    private const val STROKE_WIDTH = 1.75f

    private fun lucide(name: String, vararg paths: String): ImageVector =
        ImageVector.Builder(
            name = name,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            paths.forEach { path ->
                addPath(
                    pathData = addPathNodes(path),
                    stroke = SolidColor(Color.Black),
                    strokeLineWidth = STROKE_WIDTH,
                    strokeLineCap = StrokeCap.Round,
                    strokeLineJoin = StrokeJoin.Round,
                )
            }
        }.build()

    /** Lucide "plus". */
    val Add: ImageVector by lazy { lucide("plus", "M5 12h14", "M12 5v14") }

    /** Lucide "house-plus". */
    val AddToHomeScreen: ImageVector by lazy { lucide("house-plus", "M12.35 21H5a2 2 0 0 1-2-2v-9a2 2 0 0 1 .71-1.53l7-6a2 2 0 0 1 2.58 0l7 6A2 2 0 0 1 21 10v2.35", "M14.8 12.4A1 1 0 0 0 14 12h-4a1 1 0 0 0-1 1v8", "M15 18h6", "M18 15v6") }

    /** Lucide "archive". */
    val Archive: ImageVector by lazy { lucide("archive", "M3 3h18a1 1 0 0 1 1 1v3a1 1 0 0 1 -1 1h-18a1 1 0 0 1 -1 -1v-3a1 1 0 0 1 1 -1Z", "M4 8v11a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8", "M10 12h4") }

    /** Lucide "arrow-left". */
    val ArrowBack: ImageVector by lazy { lucide("arrow-left", "m12 19-7-7 7-7", "M19 12H5") }

    /** Lucide "arrow-down". */
    val ArrowDownward: ImageVector by lazy { lucide("arrow-down", "M12 5v14", "m19 12-7 7-7-7") }

    /** Lucide "arrow-right". */
    val ArrowForward: ImageVector by lazy { lucide("arrow-right", "M5 12h14", "m12 5 7 7-7 7") }

    /** Lucide "arrow-up". */
    val ArrowUpward: ImageVector by lazy { lucide("arrow-up", "m5 12 7-7 7 7", "M12 19V5") }

    /** Lucide "sparkles". */
    val AutoAwesome: ImageVector by lazy { lucide("sparkles", "M11.017 2.814a1 1 0 0 1 1.966 0l1.051 5.558a2 2 0 0 0 1.594 1.594l5.558 1.051a1 1 0 0 1 0 1.966l-5.558 1.051a2 2 0 0 0-1.594 1.594l-1.051 5.558a1 1 0 0 1-1.966 0l-1.051-5.558a2 2 0 0 0-1.594-1.594l-5.558-1.051a1 1 0 0 1 0-1.966l5.558-1.051a2 2 0 0 0 1.594-1.594z", "M20 2v4", "M22 4h-4", "M2 20a2 2 0 1 0 4 0a2 2 0 1 0 -4 0") }

    /** Lucide "bookmark-plus". */
    val BookmarkAdd: ImageVector by lazy { lucide("bookmark-plus", "M12 7v6", "M15 10H9", "M17 3a2 2 0 0 1 2 2v15a1 1 0 0 1-1.496.868l-4.512-2.578a2 2 0 0 0-1.984 0l-4.512 2.578A1 1 0 0 1 5 20V5a2 2 0 0 1 2-2z") }

    /** Lucide "sun-moon". */
    val BrightnessMedium: ImageVector by lazy { lucide("sun-moon", "M12 2v2", "M14.837 16.385a6 6 0 1 1-7.223-7.222c.624-.147.97.66.715 1.248a4 4 0 0 0 5.26 5.259c.589-.255 1.396.09 1.248.715", "M16 12a4 4 0 0 0-4-4", "m19 5-1.256 1.256", "M20 12h2") }

    /** Lucide "camera". */
    val CameraAlt: ImageVector by lazy { lucide("camera", "M13.997 4a2 2 0 0 1 1.76 1.05l.486.9A2 2 0 0 0 18.003 7H20a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V9a2 2 0 0 1 2-2h1.997a2 2 0 0 0 1.759-1.048l.489-.904A2 2 0 0 1 10.004 4z", "M9 13a3 3 0 1 0 6 0a3 3 0 1 0 -6 0") }

    /** Lucide "check". */
    val Check: ImageVector by lazy { lucide("check", "M20 6 9 17l-5-5") }

    /** Lucide "square-check". */
    val CheckBox: ImageVector by lazy { lucide("square-check", "M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-14a2 2 0 0 1 2 -2Z", "m16 9-5.5 5.5L8 12") }

    /** Lucide "square". */
    val CheckBoxOutlineBlank: ImageVector by lazy { lucide("square", "M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-14a2 2 0 0 1 2 -2Z") }

    /** Lucide "list-checks". */
    val Checklist: ImageVector by lazy { lucide("list-checks", "M13 5h8", "M13 12h8", "M13 19h8", "m3 17 2 2 4-4", "m3 7 2 2 4-4") }

    /** Lucide "x". */
    val Close: ImageVector by lazy { lucide("x", "M18 6 6 18", "m6 6 12 12") }

    /** Lucide "code". */
    val Code: ImageVector by lazy { lucide("code", "m16 18 6-6-6-6", "m8 6-6 6 6 6") }

    /** Lucide "copy". */
    val ContentCopy: ImageVector by lazy { lucide("copy", "M10 8h10a2 2 0 0 1 2 2v10a2 2 0 0 1 -2 2h-10a2 2 0 0 1 -2 -2v-10a2 2 0 0 1 2 -2Z", "M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2") }

    /** Lucide "crop". */
    val Crop: ImageVector by lazy { lucide("crop", "M6 2v14a2 2 0 0 0 2 2h14", "M18 22V8a2 2 0 0 0-2-2H2") }

    /** Lucide "moon". */
    val DarkMode: ImageVector by lazy { lucide("moon", "M20.985 12.486a9 9 0 1 1-9.473-9.472c.405-.022.617.46.402.803a6 6 0 0 0 8.268 8.268c.344-.215.825-.004.803.401") }

    /** Lucide "layout-dashboard". */
    val Dashboard: ImageVector by lazy { lucide("layout-dashboard", "M4 3h5a1 1 0 0 1 1 1v7a1 1 0 0 1 -1 1h-5a1 1 0 0 1 -1 -1v-7a1 1 0 0 1 1 -1Z", "M15 3h5a1 1 0 0 1 1 1v3a1 1 0 0 1 -1 1h-5a1 1 0 0 1 -1 -1v-3a1 1 0 0 1 1 -1Z", "M15 12h5a1 1 0 0 1 1 1v7a1 1 0 0 1 -1 1h-5a1 1 0 0 1 -1 -1v-7a1 1 0 0 1 1 -1Z", "M4 16h5a1 1 0 0 1 1 1v3a1 1 0 0 1 -1 1h-5a1 1 0 0 1 -1 -1v-3a1 1 0 0 1 1 -1Z") }

    /** Lucide "trash". */
    val Delete: ImageVector by lazy { lucide("trash", "M10 11v6", "M14 11v6", "M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6", "M3 6h18", "M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2") }

    /** Lucide "trash". */
    val DeleteForever: ImageVector by lazy { lucide("trash", "M10 11v6", "M14 11v6", "M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6", "M3 6h18", "M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2") }

    /** Lucide "trash". */
    val DeleteSweep: ImageVector by lazy { lucide("trash", "M10 11v6", "M14 11v6", "M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6", "M3 6h18", "M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2") }

    /** Lucide "file-text". */
    val Description: ImageVector by lazy { lucide("file-text", "M6 22a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h8a2.4 2.4 0 0 1 1.704.706l3.588 3.588A2.4 2.4 0 0 1 20 8v12a2 2 0 0 1-2 2z", "M14 2v5a1 1 0 0 0 1 1h5", "M10 9H8", "M16 13H8", "M16 17H8") }

    /** Lucide "grip-horizontal". */
    val DragHandle: ImageVector by lazy { lucide("grip-horizontal", "M11 9a1 1 0 1 0 2 0a1 1 0 1 0 -2 0", "M18 9a1 1 0 1 0 2 0a1 1 0 1 0 -2 0", "M4 9a1 1 0 1 0 2 0a1 1 0 1 0 -2 0", "M11 15a1 1 0 1 0 2 0a1 1 0 1 0 -2 0", "M18 15a1 1 0 1 0 2 0a1 1 0 1 0 -2 0", "M4 15a1 1 0 1 0 2 0a1 1 0 1 0 -2 0") }

    /** Lucide "calendar". */
    val Event: ImageVector by lazy { lucide("calendar", "M8 2v3", "M16 2v3", "M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-14a2 2 0 0 1 2 -2Z", "M3 9h18") }

    /** Lucide "chevron-up". */
    val ExpandLess: ImageVector by lazy { lucide("chevron-up", "m18 15-6-6-6 6") }

    /** Lucide "chevron-down". */
    val ExpandMore: ImageVector by lazy { lucide("chevron-down", "m6 9 6 6 6-6") }

    /** Lucide "copy-plus". */
    val FileCopy: ImageVector by lazy { lucide("copy-plus", "M15 12L15 18", "M12 15L18 15", "M10 8h10a2 2 0 0 1 2 2v10a2 2 0 0 1 -2 2h-10a2 2 0 0 1 -2 -2v-10a2 2 0 0 1 2 -2Z", "M4 16c-1.1 0-2-.9-2-2V4c0-1.1.9-2 2-2h10c1.1 0 2 .9 2 2") }

    /** Lucide "download". */
    val FileDownload: ImageVector by lazy { lucide("download", "M12 15V3", "M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4", "m7 10 5 5 5-5") }

    /** Lucide "upload". */
    val FileUpload: ImageVector by lazy { lucide("upload", "M12 3v12", "m17 8-5-5-5 5", "M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4") }

    /** Lucide "arrow-left-right". */
    val Flip: ImageVector by lazy { lucide("arrow-left-right", "M8 3 4 7l4 4", "M4 7h16", "m16 21 4-4-4-4", "M20 17H4") }

    /** Lucide "folder". */
    val Folder: ImageVector by lazy { lucide("folder", "M20 20a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-7.9a2 2 0 0 1-1.69-.9L9.6 3.9A2 2 0 0 0 7.93 3H4a2 2 0 0 0-2 2v13a2 2 0 0 0 2 2Z") }

    /** Lucide "folder-x". */
    val FolderOff: ImageVector by lazy { lucide("folder-x", "M20 20a2 2 0 0 0 2-2V8a2 2 0 0 0-2-2h-7.9a2 2 0 0 1-1.69-.9L9.6 3.9A2 2 0 0 0 7.93 3H4a2 2 0 0 0-2 2v13a2 2 0 0 0 2 2Z", "m9.5 10.5 5 5", "m14.5 10.5-5 5") }

    /** Lucide "bold". */
    val FormatBold: ImageVector by lazy { lucide("bold", "M6 12h9a4 4 0 0 1 0 8H7a1 1 0 0 1-1-1V5a1 1 0 0 1 1-1h7a4 4 0 0 1 0 8") }

    /** Lucide "italic". */
    val FormatItalic: ImageVector by lazy { lucide("italic", "M19 4L10 4", "M14 20L5 20", "M15 4L9 20") }

    /** Lucide "list". */
    val FormatListBulleted: ImageVector by lazy { lucide("list", "M3 5h.01", "M3 12h.01", "M3 19h.01", "M8 5h13", "M8 12h13", "M8 19h13") }

    /** Lucide "list-ordered". */
    val FormatListNumbered: ImageVector by lazy { lucide("list-ordered", "M11 5h10", "M11 12h10", "M11 19h10", "M4 4h1v5", "M4 9h2", "M6.5 20H3.4c0-1 2.6-1.925 2.6-3.5a1.5 1.5 0 0 0-2.6-1.02") }

    /** Lucide "quote". */
    val FormatQuote: ImageVector by lazy { lucide("quote", "M16 3a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2 1 1 0 0 1 1 1v1a2 2 0 0 1-2 2 1 1 0 0 0-1 1v2a1 1 0 0 0 1 1 6 6 0 0 0 6-6V5a2 2 0 0 0-2-2z", "M5 3a2 2 0 0 0-2 2v6a2 2 0 0 0 2 2 1 1 0 0 1 1 1v1a2 2 0 0 1-2 2 1 1 0 0 0-1 1v2a1 1 0 0 0 1 1 6 6 0 0 0 6-6V5a2 2 0 0 0-2-2z") }

    /** Lucide "underline". */
    val FormatUnderlined: ImageVector by lazy { lucide("underline", "M6 4v6a6 6 0 0 0 12 0V4", "M4 20L20 20") }

    /** Lucide "layout-grid". */
    val GridView: ImageVector by lazy { lucide("layout-grid", "M4 3h5a1 1 0 0 1 1 1v5a1 1 0 0 1 -1 1h-5a1 1 0 0 1 -1 -1v-5a1 1 0 0 1 1 -1Z", "M15 3h5a1 1 0 0 1 1 1v5a1 1 0 0 1 -1 1h-5a1 1 0 0 1 -1 -1v-5a1 1 0 0 1 1 -1Z", "M15 14h5a1 1 0 0 1 1 1v5a1 1 0 0 1 -1 1h-5a1 1 0 0 1 -1 -1v-5a1 1 0 0 1 1 -1Z", "M4 14h5a1 1 0 0 1 1 1v5a1 1 0 0 1 -1 1h-5a1 1 0 0 1 -1 -1v-5a1 1 0 0 1 1 -1Z") }

    /** Lucide "clock-arrow-left". */
    val History: ImageVector by lazy { lucide("clock-arrow-left", "M12 6v6l1.5.8", "M12.338 21.994a10 10 0 1 1 9.587-8.767", "M14 18h8", "m18 22-4-4 4-4") }

    /** Lucide "house". */
    val Home: ImageVector by lazy { lucide("house", "M15 21v-8a1 1 0 0 0-1-1h-4a1 1 0 0 0-1 1v8", "M3 10a2 2 0 0 1 .709-1.528l7-6a2 2 0 0 1 2.582 0l7 6A2 2 0 0 1 21 10v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z") }

    /** Lucide "minus". */
    val HorizontalRule: ImageVector by lazy { lucide("minus", "M5 12h14") }

    /** Lucide "image". */
    val Image: ImageVector by lazy { lucide("image", "M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-14a2 2 0 0 1 2 -2Z", "M7 9a2 2 0 1 0 4 0a2 2 0 1 0 -4 0", "m21 15-3.086-3.086a2 2 0 0 0-2.828 0L6 21") }

    /** Lucide "info". */
    val Info: ImageVector by lazy { lucide("info", "M2 12a10 10 0 1 0 20 0a10 10 0 1 0 -20 0", "M12 16v-4", "M12 8h.01") }

    /** Lucide "chevron-down". */
    val KeyboardArrowDown: ImageVector by lazy { lucide("chevron-down", "m6 9 6 6 6-6") }

    /** Lucide "chevron-up". */
    val KeyboardArrowUp: ImageVector by lazy { lucide("chevron-up", "m18 15-6-6-6 6") }

    /** Lucide "link". */
    val Link: ImageVector by lazy { lucide("link", "M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71", "M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71") }

    /** Lucide "lock". */
    val Lock: ImageVector by lazy { lucide("lock", "M5 11h14a2 2 0 0 1 2 2v7a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-7a2 2 0 0 1 2 -2Z", "M7 11V7a5 5 0 0 1 10 0v4") }

    /** Lucide "lock-open". */
    val LockOpen: ImageVector by lazy { lucide("lock-open", "M5 11h14a2 2 0 0 1 2 2v7a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-7a2 2 0 0 1 2 -2Z", "M7 11V7a5 5 0 0 1 9.9-1") }

    /** Lucide "menu". */
    val Menu: ImageVector by lazy { lucide("menu", "M4 5h16", "M4 12h16", "M4 19h16") }

    /** Lucide "mic". */
    val Mic: ImageVector by lazy { lucide("mic", "M12 19v3", "M19 10v2a7 7 0 0 1-14 0v-2", "M12 2h0a3 3 0 0 1 3 3v7a3 3 0 0 1 -3 3h-0a3 3 0 0 1 -3 -3v-7a3 3 0 0 1 3 -3Z") }

    /** Lucide "ellipsis-vertical". */
    val MoreVert: ImageVector by lazy { lucide("ellipsis-vertical", "M11 12a1 1 0 1 0 2 0a1 1 0 1 0 -2 0", "M11 5a1 1 0 1 0 2 0a1 1 0 1 0 -2 0", "M11 19a1 1 0 1 0 2 0a1 1 0 1 0 -2 0") }

    /** Lucide "file-text". */
    val Notes: ImageVector by lazy { lucide("file-text", "M6 22a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2h8a2.4 2.4 0 0 1 1.704.706l3.588 3.588A2.4 2.4 0 0 1 20 8v12a2 2 0 0 1-2 2z", "M14 2v5a1 1 0 0 0 1 1h5", "M10 9H8", "M16 13H8", "M16 17H8") }

    /** Lucide "bell". */
    val Notifications: ImageVector by lazy { lucide("bell", "M10.268 21a2 2 0 0 0 3.464 0", "M3.262 15.326A1 1 0 0 0 4 17h16a1 1 0 0 0 .74-1.673C19.41 13.956 18 12.499 18 8A6 6 0 0 0 6 8c0 4.499-1.411 5.956-2.738 7.326") }

    /** Lucide "external-link". */
    val OpenInNew: ImageVector by lazy { lucide("external-link", "M15 3h6v6", "M10 14 21 3", "M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6") }

    /** Lucide "palette". */
    val Palette: ImageVector by lazy { lucide("palette", "M12 22a1 1 0 0 1 0-20 10 9 0 0 1 10 9 5 5 0 0 1-5 5h-2.25a1.75 1.75 0 0 0-1.4 2.8l.3.4a1.75 1.75 0 0 1-1.4 2.8z", "M13 6.5a0.5 0.5 0 1 0 1 0a0.5 0.5 0 1 0 -1 0", "M17 10.5a0.5 0.5 0 1 0 1 0a0.5 0.5 0 1 0 -1 0", "M6 12.5a0.5 0.5 0 1 0 1 0a0.5 0.5 0 1 0 -1 0", "M8 7.5a0.5 0.5 0 1 0 1 0a0.5 0.5 0 1 0 -1 0") }

    /** Lucide "pause". */
    val Pause: ImageVector by lazy { lucide("pause", "M15 3h3a1 1 0 0 1 1 1v16a1 1 0 0 1 -1 1h-3a1 1 0 0 1 -1 -1v-16a1 1 0 0 1 1 -1Z", "M6 3h3a1 1 0 0 1 1 1v16a1 1 0 0 1 -1 1h-3a1 1 0 0 1 -1 -1v-16a1 1 0 0 1 1 -1Z") }

    /** Lucide "images". */
    val PhotoLibrary: ImageVector by lazy { lucide("images", "m22 11-1.296-1.296a2.4 2.4 0 0 0-3.408 0L11 16", "M4 8a2 2 0 0 0-2 2v10a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2", "M12 7a1 1 0 1 0 2 0a1 1 0 1 0 -2 0", "M10 2h10a2 2 0 0 1 2 2v10a2 2 0 0 1 -2 2h-10a2 2 0 0 1 -2 -2v-10a2 2 0 0 1 2 -2Z") }

    /** Lucide "file-output". */
    val PictureAsPdf: ImageVector by lazy { lucide("file-output", "M4.226 20.925A2 2 0 0 0 6 22h12a2 2 0 0 0 2-2V8a2.4 2.4 0 0 0-.706-1.706l-3.588-3.588A2.4 2.4 0 0 0 14 2H6a2 2 0 0 0-2 2v3.127", "M14 2v5a1 1 0 0 0 1 1h5", "m5 11-3 3", "m5 17-3-3h10") }

    /** Lucide "play". */
    val PlayArrow: ImageVector by lazy { lucide("play", "M5 5a2 2 0 0 1 3.008-1.728l11.997 6.998a2 2 0 0 1 .003 3.458l-12 7A2 2 0 0 1 5 19z") }

    /** Lucide "pin". */
    val PushPin: ImageVector by lazy { lucide("pin", "M12 17v5", "M9 10.76a2 2 0 0 1-1.11 1.79l-1.78.9A2 2 0 0 0 5 15.24V16a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-.76a2 2 0 0 0-1.11-1.79l-1.78-.9A2 2 0 0 1 15 10.76V7a1 1 0 0 1 1-1 2 2 0 0 0 0-4H8a2 2 0 0 0 0 4 1 1 0 0 1 1 1z") }

    /** Lucide "redo-2". */
    val Redo: ImageVector by lazy { lucide("redo-2", "m15 14 5-5-5-5", "M20 9H9.5A5.5 5.5 0 0 0 4 14.5A5.5 5.5 0 0 0 9.5 20H13") }

    /** Lucide "refresh-ccw". */
    val RestartAlt: ImageVector by lazy { lucide("refresh-ccw", "M21 12a9 9 0 0 0-9-9 9.75 9.75 0 0 0-6.74 2.74L3 8", "M3 3v5h5", "M3 12a9 9 0 0 0 9 9 9.75 9.75 0 0 0 6.74-2.74L21 16", "M16 16h5v5") }

    /** Lucide "clock-arrow-left". */
    val Restore: ImageVector by lazy { lucide("clock-arrow-left", "M12 6v6l1.5.8", "M12.338 21.994a10 10 0 1 1 9.587-8.767", "M14 18h8", "m18 22-4-4 4-4") }

    /** Lucide "undo-2". */
    val RestoreFromTrash: ImageVector by lazy { lucide("undo-2", "M9 14 4 9l5-5", "M4 9h10.5a5.5 5.5 0 0 1 5.5 5.5a5.5 5.5 0 0 1-5.5 5.5H11") }

    /** Lucide "rotate-ccw". */
    val RotateLeft: ImageVector by lazy { lucide("rotate-ccw", "M3 12a9 9 0 1 0 9-9 9.75 9.75 0 0 0-6.74 2.74L3 8", "M3 3v5h5") }

    /** Lucide "rotate-cw". */
    val RotateRight: ImageVector by lazy { lucide("rotate-cw", "M21 12a9 9 0 1 1-9-9c2.52 0 4.93 1 6.74 2.74L21 8", "M21 3v5h-5") }

    /** Lucide "clock". */
    val Schedule: ImageVector by lazy { lucide("clock", "M2 12a10 10 0 1 0 20 0a10 10 0 1 0 -20 0", "M12 6v6l4 2") }

    /** Lucide "search". */
    val Search: ImageVector by lazy { lucide("search", "m21 21-4.34-4.34", "M3 11a8 8 0 1 0 16 0a8 8 0 1 0 -16 0") }

    /** Lucide "square-dashed". */
    val SelectAll: ImageVector by lazy { lucide("square-dashed", "M5 3a2 2 0 0 0-2 2", "M19 3a2 2 0 0 1 2 2", "M21 19a2 2 0 0 1-2 2", "M5 21a2 2 0 0 1-2-2", "M9 3h1", "M9 21h1", "M14 3h1", "M14 21h1", "M3 9v1", "M21 9v1", "M3 14v1", "M21 14v1") }

    /** Lucide "tag". */
    val Sell: ImageVector by lazy { lucide("tag", "M12.586 2.586A2 2 0 0 0 11.172 2H4a2 2 0 0 0-2 2v7.172a2 2 0 0 0 .586 1.414l8.704 8.704a2.426 2.426 0 0 0 3.42 0l6.58-6.58a2.426 2.426 0 0 0 0-3.42z", "M7 7.5a0.5 0.5 0 1 0 1 0a0.5 0.5 0 1 0 -1 0") }

    /** Lucide "settings". */
    val Settings: ImageVector by lazy { lucide("settings", "M9.671 4.136a2.34 2.34 0 0 1 4.659 0 2.34 2.34 0 0 0 3.319 1.915 2.34 2.34 0 0 1 2.33 4.033 2.34 2.34 0 0 0 0 3.831 2.34 2.34 0 0 1-2.33 4.033 2.34 2.34 0 0 0-3.319 1.915 2.34 2.34 0 0 1-4.659 0 2.34 2.34 0 0 0-3.32-1.915 2.34 2.34 0 0 1-2.33-4.033 2.34 2.34 0 0 0 0-3.831A2.34 2.34 0 0 1 6.35 6.051a2.34 2.34 0 0 0 3.319-1.915", "M9 12a3 3 0 1 0 6 0a3 3 0 1 0 -6 0") }

    /** Lucide "share-2". */
    val Share: ImageVector by lazy { lucide("share-2", "M15 5a3 3 0 1 0 6 0a3 3 0 1 0 -6 0", "M3 12a3 3 0 1 0 6 0a3 3 0 1 0 -6 0", "M15 19a3 3 0 1 0 6 0a3 3 0 1 0 -6 0", "M8.59 13.51L15.42 17.49", "M15.41 6.51L8.59 10.49") }

    /** Lucide "arrow-down-a-z". */
    val SortByAlpha: ImageVector by lazy { lucide("arrow-down-a-z", "m3 16 4 4 4-4", "M7 20V4", "M20 8h-5", "M15 10V6.5a2.5 2.5 0 0 1 5 0V10", "M15 14h5l-5 6h5") }

    /** Lucide "square". */
    val Stop: ImageVector by lazy { lucide("square", "M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-14a2 2 0 0 1 2 -2Z") }

    /** Lucide "layers". */
    val Style: ImageVector by lazy { lucide("layers", "M12.83 2.18a2 2 0 0 0-1.66 0L2.6 6.08a1 1 0 0 0 0 1.83l8.58 3.91a2 2 0 0 0 1.66 0l8.58-3.9a1 1 0 0 0 0-1.83z", "M2 12a1 1 0 0 0 .58.91l8.6 3.91a2 2 0 0 0 1.65 0l8.58-3.9A1 1 0 0 0 22 12", "M2 17a1 1 0 0 0 .58.91l8.6 3.91a2 2 0 0 0 1.65 0l8.58-3.9A1 1 0 0 0 22 17") }

    /** Lucide "arrow-up-down". */
    val SwapVert: ImageVector by lazy { lucide("arrow-up-down", "m21 16-4 4-4-4", "M17 20V4", "m3 8 4-4 4 4", "M7 4v16") }

    /** Lucide "table". */
    val TableChart: ImageVector by lazy { lucide("table", "M12 3v18", "M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-14a2 2 0 0 1 2 -2Z", "M3 9h18", "M3 15h18") }

    /** Lucide "type". */
    val TextFields: ImageVector by lazy { lucide("type", "M12 4v16", "M4 7V5a1 1 0 0 1 1-1h14a1 1 0 0 1 1 1v2", "M9 20h6") }

    /** Lucide "sliders-horizontal". */
    val Tune: ImageVector by lazy { lucide("sliders-horizontal", "M10 5H3", "M12 19H3", "M14 3v4", "M16 17v4", "M21 12h-9", "M21 19h-5", "M21 5h-7", "M8 10v4", "M8 12H3") }

    /** Lucide "archive-arrow-up". */
    val Unarchive: ImageVector by lazy { lucide("archive-arrow-up", "M3 3h18a1 1 0 0 1 1 1v3a1 1 0 0 1 -1 1h-18a1 1 0 0 1 -1 -1v-3a1 1 0 0 1 1 -1Z", "M4 8v11a2 2 0 0 0 2 2h2", "M20 8v11a2 2 0 0 1-2 2h-2", "m9 15 3-3 3 3", "M12 12v9") }

    /** Lucide "undo-2". */
    val Undo: ImageVector by lazy { lucide("undo-2", "M9 14 4 9l5-5", "M4 9h10.5a5.5 5.5 0 0 1 5.5 5.5a5.5 5.5 0 0 1-5.5 5.5H11") }

    /** Lucide "chevrons-up-down". */
    val UnfoldMore: ImageVector by lazy { lucide("chevrons-up-down", "m7 15 5 5 5-5", "m7 9 5-5 5 5") }

    /** Lucide "rows-3". */
    val ViewAgenda: ImageVector by lazy { lucide("rows-3", "M5 3h14a2 2 0 0 1 2 2v14a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-14a2 2 0 0 1 2 -2Z", "M21 9H3", "M21 15H3") }
}
