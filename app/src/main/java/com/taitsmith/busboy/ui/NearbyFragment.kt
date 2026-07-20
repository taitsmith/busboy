package com.taitsmith.busboy.ui

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.taitsmith.busboy.R
import com.taitsmith.busboy.data.Agency
import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.databinding.FragmentNearbyBinding
import com.taitsmith.busboy.di.SettingsRepository
import com.taitsmith.busboy.utils.NearbyAdapter
import com.taitsmith.busboy.viewmodels.NearbyViewModel
import com.taitsmith.busboy.viewmodels.NearbyViewModel.ListLoadingState
import com.taitsmith.busboy.viewmodels.NearbyViewModel.NearbyStopsState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NearbyFragment : Fragment(), AdapterView.OnItemSelectedListener, DialogInterface.OnClickListener {

    @Inject lateinit var settingsRepository: SettingsRepository

    private lateinit var nearbyStopListView: RecyclerView
    private lateinit var nearbySearchButton: Button
    private lateinit var nearbyEditText: EditText
    private lateinit var nearbyAdapter: NearbyAdapter

    private var _binding: FragmentNearbyBinding? = null

    private val binding get() = _binding!!
    private val stopList = mutableListOf<Stop>()
    private val nearbyViewModel: NearbyViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNearbyBinding.inflate(inflater, container, false)
        nearbySearchButton = binding.nearbySearchButton
        nearbyEditText = binding.nearbyEditText

        val buslineArray =
            if (settingsRepository.selectedAgencyState.value == Agency.CTA) R.array.cta_bus_lines
            else R.array.bus_lines
        val buslineAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            buslineArray, android.R.layout.simple_spinner_item
        )
        buslineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.buslineSpinner.adapter = buslineAdapter
        binding.buslineSpinner.onItemSelectedListener = this

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                nearbyViewModel.nearbyStopsState.collect {
                    when (it) {
                        is NearbyStopsState.Loading -> {
                            when (it.loadState) {
                                ListLoadingState.START -> {}
                                ListLoadingState.PARTIAL -> nearbyViewModel.getNearbyStopsWithLines(it.stopList)
                                ListLoadingState.COMPLETE -> nearbyAdapter.submitList(it.stopList)
                            }
                        }
                        is NearbyStopsState.Success -> {
                            val index = stopList.size
                            stopList.add(stopList.size, it.stops)
                            nearbyAdapter.notifyItemInserted(index)
                            nearbyAdapter.submitList(stopList)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                nearbyViewModel.enableSearchButton.collect { enabled ->
                    binding.nearbySearchButton.isEnabled = enabled
                }
            }
        }

        setListeners()
        setObservers()
        if (nearbyViewModel.shouldShowDialog) showDialog()
        return binding.root
    }

    //allow users to choose whether to use their device location or pick a location from the map
    private fun showDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setMessage(R.string.dialog_location_method)
            .setPositiveButton(R.string.dialog_choose_on_map, this)
            .setNegativeButton(R.string.dialog_use_location, this) //cancel dialog, use device location
            .setCancelable(false)
            .create()
            .show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nearbyStopListView = binding.nearbyListView
        nearbyStopListView.layoutManager = LinearLayoutManager(requireContext())

        //create the adapter and pass in short (view stop predictions) / long (walking directions) press
        nearbyAdapter = NearbyAdapter ({ stop ->
            val action = NearbyFragmentDirections
                .actionNearbyFragmentToByIdFragment(stop)
            view.findNavController().navigate(action)
        }, {
            nearbyViewModel.setIsUpdated(true)
            val (_, _, _, latitude, longitude) = it
            val start = //maps api expects encoded lat/lon
                NearbyViewModel.currentLocation.latitude.toString() + "," +
                        NearbyViewModel.currentLocation.longitude.toString()
            val end = (latitude).toString() + "," + (longitude).toString()
            nearbyViewModel.getDirectionsToStop(start, end)
        })
        nearbyStopListView.adapter = nearbyAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.buslineSpinner.onItemSelectedListener = null
        binding.buslineSpinner.adapter = null
        nearbyStopListView.adapter = null
        _binding = null
    }

    private fun setObservers() {
        nearbyViewModel.directionPolylineCoords.observe(viewLifecycleOwner) {
            if (nearbyViewModel.isUpdated.value == true) {
                val action =
                    NearbyFragmentDirections.actionNearbyFragmentToMapsFragment("directions")
                findNavController().navigate(action)
                nearbyViewModel.setIsUpdated(false)
            }
        }
    }

    private fun setListeners() {
        nearbySearchButton.setOnClickListener {
            //only perform search in two cases- user picked a location from the map,
            //or we're using device location and have necessary permissions
            if ((nearbyViewModel.isUsingLocation && nearbyViewModel.checkLocationPerm())
                || !nearbyViewModel.isUsingLocation) {
                val s = nearbyEditText.text.toString()
                if (s.isNotEmpty()) {
                    val distance = s.toInt()
                    if (distance < 500 || distance > 5000) {
                        nearbyViewModel.updateStatus("BAD_DISTANCE")
                    } else {
                        nearbyViewModel.distance = distance
                        nearbyEditText.text = null
                        nearbyEditText.hint = getString(R.string.nearby_edit_text_hint_updated, s)
                    }
                }
                nearbyViewModel.getNearbyStops()
            }
        }

        binding.resetNearbyFab.setOnClickListener { resetScreen() }
    }

    //clear everything back to the initial state and re-prompt for a location. the fragment-local
    //stopList feeds the RecyclerView on the Success path, so it must be cleared or old stops reappear.
    private fun resetScreen() {
        stopList.clear()
        nearbyAdapter.submitList(emptyList())
        nearbyEditText.text = null
        nearbyEditText.hint = getString(R.string.nearby_edit_text_hint)
        binding.buslineSpinner.setSelection(0)
        nearbyViewModel.reset()
        showDialog()
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val s = p0?.getItemAtPosition(p2).toString()
        if (s == "All lines") nearbyViewModel.rt = null
        else nearbyViewModel.rt = s
    }

    override fun onNothingSelected(adapterView: AdapterView<*>?) {}

    override fun onClick(p0: DialogInterface?, p1: Int) {
        when (p1) {
            //user picked 'choose on map'
            DialogInterface.BUTTON_POSITIVE -> {
                nearbyViewModel.setIsUsingLocation(false)
                val action = NearbyFragmentDirections.actionNearbyFragmentToMapsFragment("choice")
                findNavController().navigate(action)
            }
            //user picked device location
            DialogInterface.BUTTON_NEGATIVE -> {
                nearbyViewModel.setIsUsingLocation(true)
                nearbyViewModel.checkLocationPerm()
            }
        }
    }
}