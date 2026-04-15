package com.ncwd.porkstreams.data.iptv

import com.ncwd.porkstreams.data.model.IptvChannel

object M3uParser {

    fun parse(content: String): List<IptvChannel> {
        val channels = mutableListOf<IptvChannel>()
        val lines = content.lines()
        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("#EXTINF:")) {
                val name = extractName(line)
                val logo = extractAttribute(line, "tvg-logo")
                val group = extractAttribute(line, "group-title")
                // next non-empty, non-comment line is the URL
                var j = i + 1
                while (j < lines.size && (lines[j].isBlank() || lines[j].trim().startsWith("#"))) j++
                if (j < lines.size) {
                    val url = lines[j].trim()
                    if (url.startsWith("http") && name.isNotBlank()) {
                        channels.add(IptvChannel(name = name, logo = logo, url = url, group = group))
                    }
                    i = j + 1
                } else {
                    i++
                }
            } else {
                i++
            }
        }
        return channels
    }

    private fun extractName(extinf: String): String {
        val commaIdx = extinf.lastIndexOf(',')
        return if (commaIdx >= 0) extinf.substring(commaIdx + 1).trim() else ""
    }

    private fun extractAttribute(extinf: String, attr: String): String? {
        val pattern = Regex("""$attr="([^"]*)"""")
        return pattern.find(extinf)?.groupValues?.get(1)?.takeIf { it.isNotBlank() }
    }
}
