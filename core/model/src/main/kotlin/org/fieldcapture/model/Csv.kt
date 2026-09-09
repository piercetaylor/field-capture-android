/*
 * :core:model — CSV tokenizer and writer.
 *
 * Responsibility: RFC 4180-style parsing (quoted fields, doubled quotes, embedded commas and
 * newlines, CRLF or LF) and symmetric writing, with no third-party dependency so the module
 * stays pure JVM. Every import/export contract in docs/data-formats.md goes through this file.
 *
 * Interface: Csv.parse(text) -> List<List<String>>; Csv.write(rows) -> String.
 */
package org.fieldcapture.model

object Csv {
    /** Parse CSV text into rows of fields. A trailing newline does not produce an empty row. */
    fun parse(text: String, delimiter: Char = ','): List<List<String>> {
        val rows = ArrayList<List<String>>()
        var row = ArrayList<String>()
        val field = StringBuilder()
        var inQuotes = false
        var i = 0
        val s = text.removePrefix("﻿")
        while (i < s.length) {
            val c = s[i]
            when {
                inQuotes -> {
                    if (c == '"') {
                        if (i + 1 < s.length && s[i + 1] == '"') {
                            field.append('"'); i++
                        } else {
                            inQuotes = false
                        }
                    } else {
                        field.append(c)
                    }
                }
                c == '"' -> inQuotes = true
                c == delimiter -> { row.add(field.toString()); field.setLength(0) }
                c == '\r' -> { /* handled with following \n */ }
                c == '\n' -> {
                    row.add(field.toString()); field.setLength(0)
                    rows.add(row); row = ArrayList()
                }
                else -> field.append(c)
            }
            i++
        }
        if (field.isNotEmpty() || row.isNotEmpty()) {
            row.add(field.toString())
            rows.add(row)
        }
        return rows
    }

    /** Quote a field when it contains the delimiter, a quote, or a line break. */
    fun quote(value: String?, delimiter: Char = ','): String {
        if (value == null) return ""
        val needs = value.any { it == delimiter || it == '"' || it == '\n' || it == '\r' }
        return if (needs) "\"" + value.replace("\"", "\"\"") + "\"" else value
    }

    /** Write rows as CSV with CRLF line endings (RFC 4180). */
    fun write(rows: Iterable<Iterable<String?>>, delimiter: Char = ','): String =
        buildString {
            for (r in rows) {
                append(r.joinToString(delimiter.toString()) { quote(it, delimiter) })
                append("\r\n")
            }
        }
}
