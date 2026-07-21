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
                    if (binding.agencyRadioGroup.checkedRadioButtonId != id) {
                        //detach while syncing the UI to the persisted value, so echoing that
                        //value into the RadioGroup cannot loop back through the listener and
                        //write it straight back to DataStore. Since MainActivity now calls
                        //recreate() on every agency change, such a write-back is not merely
                        //redundant — a stale one would rebuild the Activity for no reason.
                        binding.agencyRadioGroup.setOnCheckedChangeListener(null)
                        binding.agencyRadioGroup.check(id)
                        binding.agencyRadioGroup.setOnCheckedChangeListener(agencySelectionListener)
                    }
                    binding.settingsActiveAgency.text = getString(
                        R.string.settings_active_agency, getString(agency.displayNameRes)
                    )
                }
            }
        }

        binding.agencyRadioGroup.setOnCheckedChangeListener(agencySelectionListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
