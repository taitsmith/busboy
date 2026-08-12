package com.taitsmith.busboy.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.taitsmith.busboy.R
import com.taitsmith.busboy.data.Agency
import com.taitsmith.busboy.databinding.SettingsFragmentBinding
import com.taitsmith.busboy.ui.theme.displayNameRes
import com.taitsmith.busboy.viewmodels.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private val settingsViewModel: SettingsViewModel by viewModels()

    private var _binding: SettingsFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = SettingsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val agencySelectionListener =
        RadioGroup.OnCheckedChangeListener { _, checkedId ->
            val agency = when (checkedId) {
                R.id.agency_cta -> Agency.CTA
                else -> Agency.AC_TRANSIT
            }
            settingsViewModel.setAgency(agency)
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                settingsViewModel.selectedAgency.collect { agency ->
                    val id = when (agency) {
                        Agency.AC_TRANSIT -> R.id.agency_ac_transit
                        Agency.CTA -> R.id.agency_cta
                    }
                    //no RadioButton starts checked, so this runs on every visit to Settings.
                    //Detach while syncing the UI to the persisted value, or check() re-enters
                    //the listener and writes the value straight back to DataStore.
                    if (binding.agencyRadioGroup.checkedRadioButtonId != id) {
                        binding.agencyRadioGroup.withListenerDetached { check(id) }
                    }
                    binding.settingsActiveAgency.text = getString(
                        R.string.settings_active_agency, getString(agency.displayNameRes)
                    )
                }
            }
        }

        binding.agencyRadioGroup.setOnCheckedChangeListener(agencySelectionListener)
    }

    /**
     * Runs [block] with the agency listener detached, always reattaching afterwards.
     *
     * The try/finally is what makes this safe rather than incidentally safe: if the listener
     * were ever left detached, agency switching would stop working silently — no crash, no
     * log, and a UI that still looks entirely normal.
     */
    private inline fun RadioGroup.withListenerDetached(block: RadioGroup.() -> Unit) {
        setOnCheckedChangeListener(null)
        try {
            block()
        } finally {
            setOnCheckedChangeListener(agencySelectionListener)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
