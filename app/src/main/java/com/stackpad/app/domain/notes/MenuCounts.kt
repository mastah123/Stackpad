package com.stackpad.app.domain.notes

/** Note counts shown on the right of the side menu's Home rows. */
data class MenuCounts(val all: Int = 0, val pinned: Int = 0, val trashed: Int = 0) {
    companion object {
        /** [activePinned] holds one flag per note outside the trash; [trashed] is the trash size. */
        fun of(activePinned: List<Boolean>, trashed: Int) =
            MenuCounts(all = activePinned.size, pinned = activePinned.count { it }, trashed = trashed)
    }
}
