package com.taitsmith.busboy.ui

import dagger.hilt.android.AndroidEntryPoint
import com.taitsmith.busboy.viewmodels.ByIdViewModel
import com.taitsmith.busboy.utils.PredictionAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.taitsmith.busboy.data.Prediction
import com.taitsmith.busboy.databinding.ByIdFragmentBinding
import com.taitsmith.busboy.ui.MainActivity.Companion.mainActivityViewModel
import com.taitsmith.busboy.viewmodels.MainActivityViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.IndexOutOfBoundsException

@AndroidEntryPoint
class ByIdFragment : Fragment() {

    private val byIdViewModel: ByIdViewModel by activityViewModels()
    private val args: ByIdFragmentArgs by navArgs()

    private var _binding: ByIdFragmentBinding? = null
    private var _predictionListView: RecyclerView? = null

    private val predictionListView get() = _predictionListView!!
    private val binding get() = _binding!!

    private lateinit var predictionAdapter: PredictionAdapter

    private var predictionList: List<Prediction>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ByIdFragmentBinding.inflate(inflater, container, false)
        _predictionListView = binding.predictionListView
        if (args.selectedNearbyStop != null) {
            lifecycleScope.launch(Dispatchers.IO) {
                byIdViewModel.getStopPredictions(args.selectedNearbyStop!!.stopId!!, null)
            }
        }
        setListeners()
        return binding.root
    }

    private fun setListeners() {
        binding.searchByIdButton.setOnClickListener { search() }
        binding.addToFavoritesFab.setOnClickListener { byIdViewModel.addStopToFavorites() }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        predictionListView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        predictionAdapter = PredictionAdapter({
            prediction ->
            MainActivityViewModel.mutableStatusMessage.value = "LOADING"
            MainActivity.prediction = prediction
            byIdViewModel.getBusLocation(prediction.vid!!)
        },{
            byIdViewModel.getBusDetails(it.vid!!)
        })
        predictionListView.adapter = predictionAdapter

        setObservers()
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
            if (bus.length.isNullOrEmpty()) mainActivityViewModel!!.getWaypoints(MainActivity.prediction.rt!!)
            else {
                val action = ByIdFragmentDirections.actionByIdFragmentToBusDetailFragment(bus)
                findNavController().navigate(action)
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
        predictionListView.adapter = null
        binding.unbind()
        _binding = null
        _predictionListView = null
    }
}

