package com.taitsmith.busboy.di

import com.taitsmith.busboy.data.CtaStop
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Unit tests for the pure parsing / geo helpers behind the CTA nearby-stops search (no Room needed).
 */
@RunWith(JUnit4::class)
class CtaStopCatalogTest {

    private fun stop(id: String, lat: Double, lon: Double, lines: String = "22") =
        CtaStop(stopId = id, name = id, lat = lat, lon = lon, linesServed = lines)

    @Test
    fun `parseLine reads a tab-delimited catalog row`() {
        CtaStopCatalog.parseLine("1926\tClark & Addison\t41.94707328\t-87.65630719\t22, 36") shouldBe
            CtaStop("1926", "Clark & Addison", 41.94707328, -87.65630719, "22, 36")
    }

    @Test
    fun `parseLine returns null for malformed rows`() {
        CtaStopCatalog.parseLine("not enough columns") shouldBe null
    }

    @Test
    fun `haversine of a point with itself is zero`() {
        CtaStopCatalog.haversineMeters(41.9, -87.65, 41.9, -87.65) shouldBe 0.0
    }

    @Test
    fun `filterAndSort keeps stops within radius, nearest first`() {
        val near = stop("near", 41.9001, -87.6500) // ~11 m north
        val mid = stop("mid", 41.9010, -87.6500)    // ~111 m north
        val far = stop("far", 41.9100, -87.6500)    // ~1.1 km north

        val result = CtaStopCatalog.filterAndSort(
            listOf(far, mid, near), 41.9000, -87.6500, 200.0, null
        )

        result.map { it.stopId } shouldBe listOf("near", "mid")
    }

    @Test
    fun `filterAndSort applies the route filter`() {
        val a = stop("a", 41.9001, -87.6500, "22, 36")
        val b = stop("b", 41.9001, -87.6500, "8, 9")

        val result = CtaStopCatalog.filterAndSort(
            listOf(a, b), 41.9000, -87.6500, 500.0, "36"
        )

        result.map { it.stopId } shouldBe listOf("a")
    }
}
