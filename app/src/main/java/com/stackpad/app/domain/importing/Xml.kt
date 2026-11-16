package com.stackpad.app.domain.importing

import org.xml.sax.Attributes
import org.xml.sax.InputSource
import org.xml.sax.helpers.DefaultHandler
import java.io.InputStream
import java.io.StringReader
import javax.xml.parsers.SAXParserFactory

/** Safe SAX parsing shared by the Evernote and Word importers: no DTDs and no outside files or addresses are ever read. */
internal object Xml {

    fun parse(input: InputStream, handler: DefaultHandler, namespaceAware: Boolean = false) {
        val factory = SAXParserFactory.newInstance().apply { isNamespaceAware = namespaceAware }
        val reader = factory.newSAXParser().xmlReader
        reader.contentHandler = handler
        reader.errorHandler = handler
        // Answer every external entity (such as Evernote's DTD address) with nothing.
        reader.entityResolver = org.xml.sax.EntityResolver { _, _ -> InputSource(StringReader("")) }
        reader.parse(InputSource(input))
    }

    fun parse(text: String, handler: DefaultHandler) = parse(text.byteInputStream(Charsets.UTF_8), handler)

    fun Attributes.value(name: String): String? = getValue(name)
}
