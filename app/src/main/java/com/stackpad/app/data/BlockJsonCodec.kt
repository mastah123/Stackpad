package com.stackpad.app.data

import com.stackpad.app.data.model.Block
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/** Converts a note's blocks to and from the JSON stored in the database. */
object BlockJsonCodec {
    private val json = Json {
        classDiscriminator = "type"
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val serializer = ListSerializer(Block.serializer())

    fun encode(blocks: List<Block>): String = json.encodeToString(serializer, blocks)

    /** Returns an empty list when [raw] cannot be parsed. */
    fun decode(raw: String): List<Block> =
        try {
            json.decodeFromString(serializer, raw)
        } catch (e: IllegalArgumentException) {
            emptyList()
        }
}
