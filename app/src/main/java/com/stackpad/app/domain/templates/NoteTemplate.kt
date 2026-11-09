package com.stackpad.app.domain.templates

import com.stackpad.app.data.model.Block
import kotlinx.serialization.Serializable

/** A starting point for a note: a title and a list of blocks. */
@Serializable
data class NoteTemplate(
    val id: String,
    val name: String,
    val title: String = "",
    val blocks: List<Block>,
    val builtIn: Boolean = false,
)
