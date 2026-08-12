package com.taitsmith.busboy.viewmodels

import com.taitsmith.busboy.MainDispatchRule
import com.taitsmith.busboy.di.StatusRepository
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test

/**
 * The consume-once contract on the parked agency-switch message.
 *
 * MainActivity reads this in onPostCreate, which runs on *every* Activity creation — every
 * rotation and config change included — and uses a non-null result to trigger both the
 * confirmation Snackbar and a navigation reset to By-ID. Clearing on read is therefore the
 * only thing stopping a rotation from bouncing the user back to By-ID with a stale
 * "Now showing ..." message.
 */
class MainActivityViewModelTest {

    //MainActivityViewModel.init collects into viewModelScope, so Main must be swapped out.
    @get:Rule
    val mainDispatchRule = MainDispatchRule()

    private fun viewModel() = MainActivityViewModel(StatusRepository())

    @Test
    fun `no message is parked on a fresh instance`() {
        viewModel().consumePendingAgencyMessage() shouldBe null
    }

    @Test
    fun `a parked message is returned to the rebuilt activity`() {
        val viewModel = viewModel()
        viewModel.setPendingAgencyMessage("Now showing CTA (Chicago)")
        viewModel.consumePendingAgencyMessage() shouldBe "Now showing CTA (Chicago)"
    }

    @Test
    fun `a consumed message is cleared, so a later rotation does not replay it`() {
        val viewModel = viewModel()
        viewModel.setPendingAgencyMessage("Now showing CTA (Chicago)")
        viewModel.consumePendingAgencyMessage()

        viewModel.consumePendingAgencyMessage() shouldBe null
    }
}
