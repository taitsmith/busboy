package com.taitsmith.busboy.ui.theme

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Guards the invariant that makes agency theming correct: **AppTheme.AcTransit and
 * AppTheme.Cta must declare exactly the same set of attributes.**
 *
 * `Activity.setTheme()` layers a style on top of the theme already applied from the
 * manifest (AppTheme.AcTransit) rather than replacing it. So an attribute defined in
 * AppTheme.AcTransit but missing from AppTheme.Cta does not fall back to a neutral — it
 * leaks AC Transit's green to CTA users. There is no build error and nothing visible in
 * review; it just quietly renders the wrong city's color.
 *
 * This parses themes.xml rather than reading resources because the JVM test source set has
 * no resource table (`unitTests.returnDefaultValues = true`), and asserting on the XML is
 * what catches the drift at its source anyway. Test working directory is `app/`.
 */
class AgencyThemeParityTest {

    private val themes = File("src/main/res/values/themes.xml")

    private fun attributesOf(styleName: String): Set<String> {
        val doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(themes)
        val styles = doc.getElementsByTagName("style")
        for (i in 0 until styles.length) {
            val style = styles.item(i) as Element
            if (style.getAttribute("name") != styleName) continue

            val items = style.getElementsByTagName("item")
            return (0 until items.length)
                .map { (items.item(it) as Element).getAttribute("name") }
                .toSet()
        }
        error("style '$styleName' not found in ${themes.path}")
    }

    @Test
    fun `both agency themes declare the same attributes`() {
        val acTransit = attributesOf("AppTheme.AcTransit")
        val cta = attributesOf("AppTheme.Cta")

        //named separately so a failure says which side is short, not just "sets differ"
        (acTransit - cta).shouldBeEmpty()
        (cta - acTransit).shouldBeEmpty()
    }

    @Test
    fun `agency themes actually declare colors`() {
        //a guard on the guard: if both styles were emptied the parity test above would
        //still pass, so pin that the ramp is really there.
        attributesOf("AppTheme.AcTransit").size shouldBe 24
    }
}
