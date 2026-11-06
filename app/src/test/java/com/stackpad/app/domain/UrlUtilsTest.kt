package com.stackpad.app.domain

import com.stackpad.app.domain.text.UrlUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UrlUtilsTest {
    @Test
    fun findsUrlsAndTrimsTrailingPunctuation() {
        val text = "see https://example.com/a, and www.test.org."
        val urls = UrlUtils.findUrls(text)
        assertEquals(2, urls.size)
        assertEquals("https://example.com/a", urls[0].url)
        assertEquals(4, urls[0].start)
        assertEquals("www.test.org", urls[1].url)
        assertEquals("www.test.org", text.substring(urls[1].start, urls[1].end))
    }

    @Test
    fun ignoresPlainWords() {
        assertEquals(0, UrlUtils.findUrls("just some text. nothing here").size)
    }

    @Test
    fun normalizeAddsSchemeAndAcceptsValidUrls() {
        assertEquals("https://example.com", UrlUtils.normalize("example.com"))
        assertEquals("http://a.b/c", UrlUtils.normalize(" http://a.b/c "))
        assertEquals("https://localhost", UrlUtils.normalize("localhost"))
        assertEquals("https://example.com:8080/x?y=1", UrlUtils.normalize("https://example.com:8080/x?y=1"))
    }

    @Test
    fun normalizeRejectsInvalidInput() {
        assertNull(UrlUtils.normalize(""))
        assertNull(UrlUtils.normalize("   "))
        assertNull(UrlUtils.normalize("nodot"))
        assertNull(UrlUtils.normalize("a b.com"))
        assertNull(UrlUtils.normalize("ftp://x.com"))
        assertNull(UrlUtils.normalize("https://.com"))
        assertNull(UrlUtils.normalize("javascript:alert(1)"))
    }
}
