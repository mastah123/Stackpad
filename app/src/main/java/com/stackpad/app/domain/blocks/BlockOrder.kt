package com.stackpad.app.domain.blocks

import com.stackpad.app.data.model.Block

/** Moves selected blocks one step up or down. Pure, so it is unit tested. */
object BlockOrder {

    /** Each selected block swaps with the unselected block above it. Selected blocks at the top stay put. */
    fun moveUp(blocks: List<Block>, ids: Set<String>): List<Block> {
        val result = blocks.toMutableList()
        for (i in 1 until result.size) {
            if (result[i].id in ids && result[i - 1].id !in ids) {
                val above = result[i - 1]
                result[i - 1] = result[i]
                result[i] = above
            }
        }
        return result
    }

    /** Each selected block swaps with the unselected block below it. Selected blocks at the bottom stay put. */
    fun moveDown(blocks: List<Block>, ids: Set<String>): List<Block> {
        val result = blocks.toMutableList()
        for (i in result.size - 2 downTo 0) {
            if (result[i].id in ids && result[i + 1].id !in ids) {
                val below = result[i + 1]
                result[i + 1] = result[i]
                result[i] = below
            }
        }
        return result
    }

    fun canMoveUp(blocks: List<Block>, ids: Set<String>): Boolean = moveUp(blocks, ids) != blocks

    fun canMoveDown(blocks: List<Block>, ids: Set<String>): Boolean = moveDown(blocks, ids) != blocks
}
