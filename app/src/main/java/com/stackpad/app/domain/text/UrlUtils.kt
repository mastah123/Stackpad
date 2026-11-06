package com.stackpad.app.domain.text

/** URL detection (for inline links) and validation (for link blocks). */
object UrlUtils {

    /** A URL found inside text, covering `[start, end)`. */
    data class UrlMatch(val start: Int, val end: Int, val url: String)

    private val urlRegex = Regex("""(?i)\b(?:https?://|www\.)[^\s<>"]+""")
    private val schemeRegex = Regex("(?i)^[a-z][a-z0-9+.-]*://")
    private val trailingPunctuation = charArrayOf('.', ',', ';', ':', '!', '?', ')', ']', '}', '\'')

    fun findUrls(text: String): List<UrlMatch> =
        urlRegex.findAll(text).mapNotNull { match ->
            val trimmed = match.value.trimEnd(*trailingPunctuation)
            if (trimmed.length < 4) {
                null
            } else {
                UrlMatch(match.range.first, match.range.first + trimmed.length, trimmed)
            }
        }.toList()

    /**
     * Returns a usable http(s) URL for [input] (adding `https://` when missing),
     * or null when [input] is not a valid web address.
     */
    fun normalize(input: String): String? {
        val trimmed = input.trim()
        if (trimmed.isEmpty() || trimmed.any { it.isWhitespace() }) return null

        val withScheme = if (schemeRegex.containsMatchIn(trimmed)) trimmed else "https://$trimmed"
        val lower = withScheme.lowercase()
        if (!lower.startsWith("http://") && !lower.startsWith("https://")) return null

        val host = withScheme.substringAfter("://")
            .substringBefore('/')
            .substringBefore('?')
            .substringBefore('#')
            .substringAfterLast('@')
            .substringBefore(':')
        if (host.isEmpty() || host.startsWith('.') || host.endsWith('.')) return null
        if (!host.contains('.') && !host.equals("localhost", ignoreCase = true)) return null
        return withScheme
    }
}
