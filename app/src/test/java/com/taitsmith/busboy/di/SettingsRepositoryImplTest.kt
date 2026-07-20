package com.taitsmith.busboy.di

import com.taitsmith.busboy.data.Agency
import io.kotest.matchers.shouldBe
import org.junit.Test

/**
 * Covers the selected-agency parsing/default logic that guards the switch feature against bad or
 * missing persisted data. The DataStore round-trip itself is left to instrumented coverage; this
 * pins the pure branch that decides what a stored (or absent, or invalid) value resolves to.
 */
class SettingsRepositoryImplTest {

    @Test
    fun `parses a valid stored agency name`() {
        SettingsRepositoryImpl.toAgency("CTA") shouldBe Agency.CTA
        SettingsRepositoryImpl.toAgency("AC_TRANSIT") shouldBe Agency.AC_TRANSIT
    }

    @Test
    fun `defaults to AC_TRANSIT when nothing is stored`() {
        SettingsRepositoryImpl.toAgency(null) shouldBe Agency.AC_TRANSIT
    }

    @Test
    fun `defaults to AC_TRANSIT for an unrecognized value`() {
        SettingsRepositoryImpl.toAgency("MUNI") shouldBe Agency.AC_TRANSIT
    }
}
