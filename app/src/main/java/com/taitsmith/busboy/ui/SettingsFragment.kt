package com.taitsmith.busboy.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.taitsmith.busboy.R
import com.taitsmith.busboy.data.Agency
import com.taitsmith.busboy.databinding.SettingsFragmentBinding
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
                        binding.agencyRadioGroup.check(id)
                    }
                }
            }
        }

        binding.agencyRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val agency = when (checkedId) {
                R.id.agency_cta -> Agency.CTA
                else -> Agency.AC_TRANSIT
            }
            settingsViewModel.setAgency(agency)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
