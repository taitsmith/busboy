package com.taitsmith.busboy.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.taitsmith.busboy.R
import com.taitsmith.busboy.data.Prediction
import com.taitsmith.busboy.databinding.FragmentByIdBinding
import com.taitsmith.busboy.utils.PredictionAdapter
import com.taitsmith.busboy.viewmodels.ByIdViewModel
import com.taitsmith.busboy.viewmodels.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ByIdFragment : Fragment() {

    private val byIdViewModel: ByIdViewModel by activityViewModels()
    private val args: ByIdFragmentArgs by navArgs()

    private var _binding: FragmentByIdBinding? = null
    private var _predictionListView: RecyclerView? = null

    private val predictionListView get() = _predictionListView!!
    private val binding get() = _binding!!

    private lateinit var predictionAdapter: PredictionAdapter

    private var predictionList: List<Prediction>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentByIdBinding.inflate(inflater, container, false)
        _predictionListView = binding.predictionListView

        //if we're coming to the predictions fragment from nearby / favorites,
        //we want to display predictions for the selected stop
        if (args.selectedNearbyStop != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                args.selectedNearbyStop?.stopId?.let {
                    byIdViewModel.getStopPredictions(it , null)
                }
            }
        }

        setListeners()
        setObservers()

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        predictionListView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        predictionAdapter = PredictionAdapter({ prediction ->
            byIdViewModel.setIsUpdated(true)
            MainActivityViewModel.mutableStatusMessage.value = "LOADING"
            MainActivity.prediction = prediction
            byIdViewModel.getBusLocation(prediction.vid!!)
        },{
            byIdViewModel.setIsUpdated(true)
            it.vid?.let { it1 -> byIdViewModel.getBusDetails(it1)
            }
        })
        predictionListView.adapter = predictionAdapter
    }


    private fun setListeners() {
        binding.searchByIdButton.setOnClickListener { search() }
        binding.addToFavoritesFab.setOnClickListener { byIdViewModel.addStopToFavorites() }
    }

    private fun setObservers() {
        byIdViewModel.stopPredictions.observe(
            viewLifecycleOwner
        ) { predictions: List<Prediction> ->
            predictionList = predictions
            predictionAdapter.submitList(predictionList)
            binding.busFlagIV.visibility = View.INVISIBLE
            try {
                updateTextHint(predictions[0].stpnm!!)
            } catch (e: IndexOutOfBoundsException) {
                e.printStackTrace()
            }
            MainActivityViewModel.mutableStatusMessage.value = "LOADED"
        }

        /*  we want to determine if we're going to take this bus and display its location
            on a map, or if we're going to take it and display detailed information about
            it to the user. the bus object for map display has minimal information so we can
            check if certain things are null/empty and determine where to go from there
         */
        byIdViewModel.bus.observe(viewLifecycleOwner) { bus ->
            if (byIdViewModel.isUpdated.value == true) {
                if (bus.length.isNullOrEmpty()) byIdViewModel.getWaypoints(MainActivity.prediction.rt!!)
                else {
                    BusDetailFragment(bus).show(childFragmentManager, "detail")
                }
            }
        }

        byIdViewModel.busRouteWaypoints.observe(viewLifecycleOwner) {
            if (byIdViewModel.isUpdated.value == true) {
                val action = ByIdFragmentDirections.actionByIdFragmentToMapsFragment("route")
                findNavController().navigate(action)
                byIdViewModel.setIsUpdated(false)
            }
        }

        byIdViewModel.alerts.observe(viewLifecycleOwner) { alerts ->
            val alertList = alerts.bustimeResponse?.sb
            if (!alertList.isNullOrEmpty() && byIdViewModel.alertShown.value == false) {
                val snackbar = Snackbar.make(binding.root,
                    resources.getQuantityString(R.plurals.snackbar_service_alerts, alertList.size, alertList.size),
                    Snackbar.LENGTH_LONG)

                snackbar.setAction("view") {
                    val action = ByIdFragmentDirections.actionByIdFragmentToServiceAlertFragment(alerts)
                    findNavController().navigate(action)
                    byIdViewModel.setAlertShown(true)
                }
                snackbar.show()
            }
        }
    }

    private fun updateTextHint(s: String) {
        binding.stopEntryEditText.text = null
        binding.stopEntryEditText.hint = s
        byIdViewModel.stop.value?.name = s
    }

    private fun search() {
        MainActivityViewModel.mutableStatusMessage.value = "LOADING"

        //allow users to re-click the search button to update currently displayed stop
        //if they haven't entered a new valid number, otherwise display newly entered stop
        //TODO replace this with swipe to refresh
        if (byIdViewModel.stopId.value != null && binding.stopEntryEditText.text.length != 5) {
            byIdViewModel.getStopPredictions(byIdViewModel.stopId.value!!, null)
        } else if (binding.stopEntryEditText.text.length == 5) {
            byIdViewModel.getStopPredictions(binding.stopEntryEditText.text.toString(), null)
        } else {
            MainActivityViewModel.mutableErrorMessage.value = "BAD_INPUT"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        byIdViewModel.stopPredictions.removeObservers(viewLifecycleOwner)
        byIdViewModel.bus.removeObservers(viewLifecycleOwner)
        predictionListView.adapter = null
        _binding = null
        _predictionListView = null
    }
}

