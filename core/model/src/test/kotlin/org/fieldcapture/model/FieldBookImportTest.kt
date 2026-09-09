package org.fieldcapture.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FieldBookImportTest {
    private fun fixture(name: String): String =
        checkNotNull(javaClass.classLoader.getResourceAsStream(name)) { "missing fixture $name" }
            .bufferedReader().use { it.readText() }

    private val spec = FieldBookImportSpec(trial = "13RPN", field = "north")

    @Test
    fun `parses Field Book sample with auto-detected unique, primary and secondary columns`() {
        val r = FieldBookImport.parse(fixture("field_sample_geo.csv"), spec)
        assertFalse(r.hasErrors, r.issues.toString())
        assertEquals("plot_id", r.uniqueIdColumn)
        assertEquals("row", r.primaryIdColumn)
        assertEquals("plot", r.secondaryIdColumn)
        assertEquals(5, r.plots.size)
        val first = r.plots.first()
        assertEquals("13RPN00001", first.plotId)
        assertEquals(PlotKey("13RPN", "north", "13RPN00001"), first.key)
        assertEquals("Kharkof", first.attributes["seed_name"])
        assertEquals(1, first.plantingOrder)
        assertEquals(
            listOf("plot_id", "row", "column", "plot", "tray_row", "tray_id", "seed_id", "seed_name", "pedigree", "geo_coordinates"),
            r.columns,
        )
    }

    @Test
    fun `geo_coordinates in the bundled sample are lon-lat and are read with a warning`() {
        val r = FieldBookImport.parse(fixture("field_sample_geo.csv"), spec)
        val c = r.plots.first().centroid
        assertNotNull(c)
        assertEquals(36.6677618, c.lat, 1e-9)
        assertEquals(-96.3431604, c.lon, 1e-9)
        assertTrue(r.issues.any { it.severity == ImportIssue.Severity.WARNING && it.row == 2 && "lon;lat" in it.message })
        assertNull(r.plots[1].centroid)
    }

    @Test
    fun `documented lat-lon order is accepted without warning when unambiguous`() {
        val p = FieldBookImport.parseGeoCoordinates("36.6677618;-96.3431604")
        assertEquals(LatLon(36.6677618, -96.3431604), p.position)
        assertNull(p.warning)
    }

    @Test
    fun `duplicate and blank unique identifiers are errors with row numbers`() {
        val csv = "plot_id,row,plot\nA1,1,1\nA1,1,2\n,1,3\n"
        val r = FieldBookImport.parse(csv, spec)
        assertTrue(r.hasErrors)
        assertEquals(1, r.plots.size)
        assertTrue(r.issues.any { it.row == 3 && "duplicate" in it.message })
        assertTrue(r.issues.any { it.row == 4 && "blank" in it.message })
    }

    @Test
    fun `headers with characters Field Book rejects are reported`() {
        val csv = "plot_id,yield/plot,plot\nA1,1,1\n"
        val r = FieldBookImport.parse(csv, spec)
        assertTrue(r.issues.any { it.severity == ImportIssue.Severity.ERROR && "yield/plot" in it.message })
    }

    @Test
    fun `explicit column selection overrides auto-detection`() {
        val csv = "uid,range,col,name\nP1,1,1,x\nP2,1,2,y\n"
        val r = FieldBookImport.parse(csv, spec.copy(uniqueIdColumn = "uid", primaryIdColumn = "range", secondaryIdColumn = "col"))
        assertFalse(r.hasErrors)
        assertEquals("P2", r.plots[1].plotId)
        assertEquals("1", r.plots[1].primaryId)
        assertEquals("2", r.plots[1].secondaryId)
    }

    @Test
    fun `csv tokenizer handles quotes, embedded commas, CRLF and BOM`() {
        val text = "﻿a,b\r\n\"x, y\",\"say \"\"hi\"\"\"\r\n1,2\r\n"
        val rows = Csv.parse(text)
        assertEquals(listOf(listOf("a", "b"), listOf("x, y", "say \"hi\""), listOf("1", "2")), rows)
        val round = Csv.write(rows)
        assertEquals(rows, Csv.parse(round))
    }
}
