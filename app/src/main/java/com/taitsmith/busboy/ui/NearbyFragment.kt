package com.taitsmith.busboy.ui

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.taitsmith.busboy.R
import com.taitsmith.busboy.databinding.FragmentNearbyBinding
import com.taitsmith.busboy.di.LocationRepository
import com.taitsmith.busboy.utils.NearbyAdapter
import com.taitsmith.busboy.viewmodels.MainActivityViewModel
import com.taitsmith.busboy.viewmodels.NearbyViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NearbyFragment : Fragment(), AdapterView.OnItemSelectedListener, DialogInterface.OnClickListener {
    @Inject
    lateinit var locationRepository: LocationRepository

    private lateinit var nearbyStopListView: RecyclerView
    private lateinit var nearbySearchButton: Button
    private lateinit var buslineSpinner: Spinner
    private lateinit var nearbyEditText: EditText
    private lateinit var nearbyAdapter: NearbyAdapter
    private lateinit var buslineAdapter: ArrayAdapter<CharSequence>

    private var _binding: FragmentNearbyBinding? = null
    private val binding get() = _binding!!

    private val nearbyViewModel: NearbyViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNearbyBinding.inflate(inflater, container, false)
        buslineSpinner = binding.buslineSpinner
        nearbySearchButton = binding.nearbySearchButton
        nearbyEditText = binding.nearbyEditText

        buslineAdapter= ArrayAdapter.createFromResource(
            requireContext(),
            R.array.bus_lines, android.R.layout.simple_spinner_item
        )
        buslineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        buslineSpinner.adapter = buslineAdapter
        buslineSpinner.onItemSelectedListener = this

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

        nearbyAdapter = NearbyAdapter ({ stop ->
            MainActivityViewModel.mutableStatusMessage.value = "LOADING"
            val action = NearbyFragmentDirections
                .actionNearbyFragmentToByIdFragment(stop)
            view.findNavController().navigate(action)
        }, {
            nearbyViewModel.setIsUpdated(true)
            MainActivityViewModel.mutableStatusMessage.value = "LOADING"
            val (_, _, _, latitude, longitude) = it
            val start =
                NearbyViewModel.currentLocation.latitude.toString() + "," +
                        NearbyViewModel.currentLocation.longitude.toString()
            val end = (latitude!!).toString() + "," + (longitude!!).toString()
            nearbyViewModel.getDirectionsToStop(start, end)
        })
        nearbyStopListView.adapter = nearbyAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        buslineSpinner.onItemSelectedListener = null
        buslineSpinner.adapter = null
        nearbyAdapter.submitList(null)
        _binding = null
        locationRepository.stopUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun setObservers() {
        nearbyViewModel.nearbyStops.observe(viewLifecycleOwner) {
            nearbyAdapter.submitList(it)
            MainActivityViewModel.mutableStatusMessage.value = "LOADED"
        }

        nearbyViewModel.directionPolylineCoords.observe(viewLifecycleOwner) {
            if (nearbyViewModel.isUpdated.value == true) {
                val action =
                    NearbyFragmentDirections.actionNearbyFragmentToMapsFragment("directions")
                findNavController().navigate(action)
                nearbyViewModel.setIsUpdated(false)
            }
        }

        nearbyViewModel.permGrantedAndEnabled.observe(viewLifecycleOwner) {
            if (it) {
                locationRepository.startUpdates()
                lifecycleScope.launch {
                    locationRepository.lastLocation.collect {
                        location ->
                        if (location != null) {
                            nearbyViewModel.setLocation(location)
                        }
                    }
                }
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
                        MainActivityViewModel.mutableErrorMessage.setValue("BAD_DISTANCE")
                    } else {
                        nearbyViewModel.distance = distance
                        nearbyEditText.text = null
                        nearbyEditText.hint = getString(R.string.nearby_edit_text_hint_updated, s)
                    }
                }
                nearbyViewModel.getNearbyStops()
            }
        }
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
            }
        }
    }
}