package com.stackpad.app.domain.importing

import com.stackpad.app.data.model.Block
import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import java.io.InputStream
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Base64

/** Reads an Evernote export (`.enex`) one note at a time, so big exports never sit in memory whole. */
object EnexParser {

    /** An attachment of a note. [hash] is the MD5 that the note text uses to refer to it. */
    class Resource(val hash: String, val mime: String, val fileName: String, val data: ByteArray)

    class Note(
        val title: String,
        val createdAt: Long,
        val updatedAt: Long,
        val tags: List<String>,
        /** Pictures are image blocks named `res:<hash>`; see [Resource]. */
        val blocks: List<Block>,
        val resources: List<Resource>,
    )

    private val timeFormat = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")

    fun parseTime(text: String): Long? = runCatching {
        LocalDateTime.parse(text.trim(), timeFormat).toInstant(ZoneOffset.UTC).toEpochMilli()
    }.getOrNull()

    /** Calls [onNote] for each note. Throws [org.xml.sax.SAXException] when the file is not an Evernote export. */
    fun parse(input: InputStream, onNote: (Note) -> Unit) {
        Xml.parse(input, Handler(onNote))
    }

    private class Handler(private val onNote: (Note) -> Unit) : DefaultHandler() {
        private val path = ArrayList<String>()
        private val text = StringBuilder()

        private var title = ""
        private var created: Long? = null
        private var updated: Long? = null
        private val tags = ArrayList<String>()
        private var content = ""
        private val resources = ArrayList<Resource>()

        private var resMime = ""
        private var resName = ""
        private var resData = ""

        override fun startElement(uri: String?, localName: String?, qName: String, attrs: Attributes) {
            path += qName
            text.setLength(0)
            if (qName == "note") {
                title = ""; created = null; updated = null; content = ""
                tags.clear(); resources.clear()
            }
            if (qName == "resource") { resMime = ""; resName = ""; resData = "" }
        }

        override fun characters(ch: CharArray, start: Int, length: Int) {
            // Only the small fields and the note body are collected; resource data is collected as well, as base64.
            text.append(ch, start, length)
        }

        override fun endElement(uri: String?, localName: String?, qName: String) {
            val parent = path.getOrNull(path.size - 2)
            when {
                qName == "title" && parent == "note" -> title = text.toString().trim()
                qName == "created" && parent == "note" -> created = parseTime(text.toString())
                qName == "updated" && parent == "note" -> updated = parseTime(text.toString())
                qName == "tag" && parent == "note" -> text.toString().trim().takeIf { it.isNotEmpty() }?.let(tags::add)
                qName == "content" && parent == "note" -> content = text.toString()
                qName == "mime" && parent == "resource" -> resMime = text.toString().trim()
                qName == "file-name" -> resName = text.toString().trim()
                qName == "data" && parent == "resource" -> resData = text.toString()
                qName == "resource" -> addResource()
                qName == "note" -> emit()
            }
            path.removeAt(path.lastIndex)
            text.setLength(0)
        }

        private fun addResource() {
            val bytes = runCatching { Base64.getMimeDecoder().decode(resData) }.getOrNull() ?: return
            if (bytes.isEmpty()) return
            val hash = MessageDigest.getInstance("MD5").digest(bytes).joinToString("") { "%02x".format(it) }
            resources += Resource(hash, resMime, resName, bytes)
            resData = ""
        }

        private fun emit() {
            val now = System.currentTimeMillis()
            val createdAt = created ?: updated ?: now
            onNote(Note(title, createdAt, updated ?: createdAt, tags.toList(), EnmlConverter.convert(content), resources.toList()))
        }
    }
}
