package com.stackpad.app.domain

import com.stackpad.app.domain.export.PageSlice
import com.stackpad.app.domain.export.Paginator
import org.junit.Assert.assertEquals
import org.junit.Test

class PaginatorTest {
    @Test fun empty_gives_no_pages() = assertEquals(emptyList<List<PageSlice>>(), Paginator.paginate(emptyList(), 100, 4))

    @Test fun items_that_fit_share_a_page() {
        val pages = Paginator.paginate(listOf(30, 30, 30), 100, 5)
        assertEquals(1, pages.size)
        assertEquals(3, pages[0].size)
    }

    @Test fun gap_counts_toward_the_page() {
        // 40 + 5 + 40 = 85, a third 20 would need 110
        val pages = Paginator.paginate(listOf(40, 40, 20), 100, 5)
        assertEquals(listOf(2, 1), pages.map { it.size })
    }

    @Test fun item_moves_whole_to_next_page() {
        val pages = Paginator.paginate(listOf(70, 50), 100, 0)
        assertEquals(listOf(PageSlice(1, 0, 50)), pages[1])
    }

    @Test fun oversize_item_is_sliced() {
        val pages = Paginator.paginate(listOf(250), 100, 0)
        assertEquals(
            listOf(PageSlice(0, 0, 100), PageSlice(0, 100, 100), PageSlice(0, 200, 50)),
            pages.flatten(),
        )
        assertEquals(3, pages.size)
    }

    @Test fun item_after_oversize_continues_below_its_last_slice() {
        val pages = Paginator.paginate(listOf(150, 10), 100, 0)
        assertEquals(2, pages.size)
        assertEquals(listOf(PageSlice(0, 100, 50), PageSlice(1, 0, 10)), pages[1])
    }
}
