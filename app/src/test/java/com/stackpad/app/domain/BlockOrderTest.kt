package com.stackpad.app.domain

import com.stackpad.app.data.model.Block
import com.stackpad.app.data.model.TextBlock
import com.stackpad.app.domain.blocks.BlockOrder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BlockOrderTest {
    private val blocks: List<Block> = listOf("a", "b", "c", "d", "e").map { TextBlock(it, text = it) }
    private fun ids(list: List<Block>) = list.map { it.id }

    @Test fun movesOneBlockUp() = assertEquals(listOf("a", "c", "b", "d", "e"), ids(BlockOrder.moveUp(blocks, setOf("c"))))
    @Test fun movesOneBlockDown() = assertEquals(listOf("a", "c", "b", "d", "e"), ids(BlockOrder.moveDown(blocks, setOf("b"))))
    @Test fun topBlockStaysAtTop() = assertEquals(ids(blocks), ids(BlockOrder.moveUp(blocks, setOf("a"))))
    @Test fun bottomBlockStaysAtBottom() = assertEquals(ids(blocks), ids(BlockOrder.moveDown(blocks, setOf("e"))))

    @Test fun adjacentGroupMovesTogether() {
        assertEquals(listOf("b", "c", "a", "d", "e"), ids(BlockOrder.moveUp(blocks, setOf("b", "c"))))
        assertEquals(listOf("a", "d", "b", "c", "e"), ids(BlockOrder.moveDown(blocks, setOf("b", "c"))))
    }

    @Test fun groupTouchingTheTopDoesNotMove() {
        assertEquals(ids(blocks), ids(BlockOrder.moveUp(blocks, setOf("a", "b"))))
    }

    @Test fun separateBlocksEachMove() {
        assertEquals(listOf("b", "a", "d", "c", "e"), ids(BlockOrder.moveUp(blocks, setOf("b", "d"))))
    }

    @Test fun blockAtTopAndAnotherInMiddle() {
        // "a" cannot move, "c" can.
        assertEquals(listOf("a", "c", "b", "d", "e"), ids(BlockOrder.moveUp(blocks, setOf("a", "c"))))
    }

    @Test fun keepsEveryBlockOnce() {
        val moved = BlockOrder.moveDown(blocks, setOf("a", "c", "e"))
        assertEquals(ids(blocks).sorted(), ids(moved).sorted())
    }

    @Test fun emptySelectionChangesNothing() = assertEquals(ids(blocks), ids(BlockOrder.moveUp(blocks, emptySet())))

    @Test fun canMoveReflectsPosition() {
        assertFalse(BlockOrder.canMoveUp(blocks, setOf("a")))
        assertTrue(BlockOrder.canMoveUp(blocks, setOf("b")))
        assertFalse(BlockOrder.canMoveDown(blocks, setOf("e")))
        assertTrue(BlockOrder.canMoveDown(blocks, setOf("d")))
        assertFalse(BlockOrder.canMoveDown(blocks, emptySet()))
    }
}
