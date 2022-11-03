package com.taitsmith.busboy.ui

import android.annotation.SuppressLint
import dagger.hilt.android.AndroidEntryPoint
import com.taitsmith.busboy.utils.NearbyAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.taitsmith.busboy.R
import com.taitsmith.busboy.databinding.NearbyFragmentBinding
import com.taitsmith.busboy.viewmodels.NearbyViewModel
import com.taitsmith.busboy.viewmodels.MainActivityViewModel

@AndroidEntryPoint
class NearbyFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private lateinit var nearbyStopListView: RecyclerView
    private lateinit var nearbySearchButton: Button
    private lateinit var buslineSpinner: Spinner
    private lateinit var nearbyEditText: EditText
    private lateinit var adapter: NearbyAdapter
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var _binding: NearbyFragmentBinding? = null
    private val binding get() = _binding!!

    private val nearbyViewModel: NearbyViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = NearbyFragmentBinding.inflate(inflater, container, false)
        buslineSpinner = binding.buslineSpinner
        nearbySearchButton = binding.nearbySearchButton
        nearbyEditText = binding.nearbyEditText

        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.bus_lines, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        buslineSpinner.adapter = adapter
        buslineSpinner.onItemSelectedListener = this

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        setListeners()
        setObservers()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        nearbyStopListView = binding.nearbyListView
        nearbyStopListView.layoutManager = LinearLayoutManager(requireContext())

        adapter = NearbyAdapter ({ stop ->
            MainActivityViewModel.mutableStatusMessage.value = "LOADING"
            val action = NearbyFragmentDirections
                .actionNearbyFragmentToByIdFragment(stop)
            view.findNavController().navigate(action)
        }, {
            nearbyViewModel.setIsUpdated(true)
            MainActivityViewModel.mutableStatusMessage.value = "LOADING"
            val (_, _, _, latitude, longitude) = it
            val start =
                NearbyViewModel.loc.latitude.toString() + "," + NearbyViewModel.loc.longitude.toString()
            val end = (latitude!!).toString() + "," + (longitude!!).toString()
            nearbyViewModel.getDirectionsToStop(start, end)
        })

        nearbyStopListView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        buslineSpinner.onItemSelectedListener = null
        buslineSpinner.adapter = null
        _binding = null
    }

    private fun setObservers() {
        nearbyViewModel.nearbyStops.observe(viewLifecycleOwner) {
            adapter.submitList(it)
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
    }

    private fun setListeners() {
        nearbySearchButton.setOnClickListener {
            if (nearbyViewModel.checkLocationPerm()) {
                val s = nearbyEditText.text.toString()
                if (s.isNotEmpty()) {
                    val distance = s.toInt()
                    if (distance < 500 || distance > 5000) {
                        MainActivityViewModel.mutableErrorMessage.setValue("BAD_DISTANCE")
                    } else {
                        nearbyViewModel.distance = distance
                        nearbyEditText.text = null
                        nearbyEditText.hint = getString(R.string.neaby_edit_text_hint_updated, s)
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
}