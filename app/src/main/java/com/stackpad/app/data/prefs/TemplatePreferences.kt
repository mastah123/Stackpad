package com.stackpad.app.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.stackpad.app.domain.templates.NoteTemplate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/** Stores the templates the user saved (the built-in ones live in code). */
class TemplatePreferences(private val context: Context) {
    private val key = stringPreferencesKey("user_templates")
    private val serializer = ListSerializer(NoteTemplate.serializer())
    private val json = Json {
        classDiscriminator = "type"
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    val templates: Flow<List<NoteTemplate>> = context.settingsDataStore.data.map { prefs -> decode(prefs[key]) }

    suspend fun add(template: NoteTemplate) = update { it + template }

    suspend fun remove(id: String) = update { list -> list.filterNot { it.id == id } }

    suspend fun count(): Int = templates.first().size

    private suspend fun update(change: (List<NoteTemplate>) -> List<NoteTemplate>) {
        context.settingsDataStore.edit { prefs ->
            prefs[key] = json.encodeToString(serializer, change(decode(prefs[key])))
        }
    }

    private fun decode(raw: String?): List<NoteTemplate> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString(serializer, raw)
        } catch (e: SerializationException) {
            emptyList()
        } catch (e: IllegalArgumentException) {
            emptyList()
        }
    }
}
