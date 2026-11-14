package com.stackpad.app.domain.export

/** The part of item [index] drawn on a page: [offset] pixels down into the item, [height] pixels tall. */
data class PageSlice(val index: Int, val offset: Int, val height: Int)

/**
 * Splits items of known heights into pages. Items are kept whole when they fit on a page;
 * an item taller than a whole page is cut into several slices.
 */
object Paginator {

    fun paginate(heights: List<Int>, pageHeight: Int, gap: Int): List<List<PageSlice>> {
        require(pageHeight > 0) { "pageHeight must be positive" }
        val pages = ArrayList<MutableList<PageSlice>>()
        var current = ArrayList<PageSlice>()
        var used = 0

        fun newPage() {
            if (current.isNotEmpty()) pages += current
            current = ArrayList()
            used = 0
        }

        heights.forEachIndexed { index, rawHeight ->
            val height = rawHeight.coerceAtLeast(0)
            val lead = if (current.isEmpty()) 0 else gap
            when {
                used + lead + height <= pageHeight -> {
                    current += PageSlice(index, 0, height)
                    used += lead + height
                }
                height <= pageHeight -> {
                    newPage()
                    current += PageSlice(index, 0, height)
                    used = height
                }
                else -> {
                    newPage()
                    var offset = 0
                    while (offset < height) {
                        val part = minOf(pageHeight, height - offset)
                        current += PageSlice(index, offset, part)
                        used = part
                        offset += part
                        if (offset < height) newPage()
                    }
                }
            }
        }
        if (current.isNotEmpty()) pages += current
        return pages
    }
}
