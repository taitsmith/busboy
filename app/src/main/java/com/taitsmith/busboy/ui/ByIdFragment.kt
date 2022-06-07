package com.taitsmith.busboy.ui

import dagger.hilt.android.AndroidEntryPoint
import com.taitsmith.busboy.viewmodels.ByIdViewModel
import com.taitsmith.busboy.utils.PredictionAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
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

    private val byIdViewModel: ByIdViewModel by viewModels()
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
            byIdViewModel.stop = args.selectedNearbyStop
            lifecycleScope.launch(Dispatchers.IO) {
                byIdViewModel.getStopPredictions(args.selectedNearbyStop!!.stopId!!)
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
            mainActivityViewModel!!.getBusLocation(prediction.vid!!)
        },{
            byIdViewModel.getBusDetails(it.vid!!)
        })
        predictionListView.adapter = predictionAdapter

        setObservers()
    }


    private fun setObservers() {
        byIdViewModel.mutableStopPredictions.observe(
            viewLifecycleOwner
        ) { predictions: List<Prediction> ->
            predictionList = predictions
            predictionAdapter.submitList(predictionList)
            binding.busFlagIV.visibility = View.INVISIBLE
            try {
                val s = predictions[0].stpnm
                binding.stopEntryEditText.text = null
                binding.stopEntryEditText.hint = s
                byIdViewModel.stop!!.name = s
            } catch (e: IndexOutOfBoundsException) {
                e.printStackTrace()
            }
            MainActivityViewModel.mutableStatusMessage.value = "LOADED"
        }
    }

    private fun search() {
        if (binding.stopEntryEditText.text.length == 5) {
            MainActivityViewModel.mutableStatusMessage.value = "LOADING"
            byIdViewModel.getStopPredictions(binding.stopEntryEditText.text.toString())
        } else {
            MainActivityViewModel.mutableErrorMessage.value = "BAD_INPUT"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        byIdViewModel.mutableStopPredictions.removeObservers(viewLifecycleOwner)
        predictionListView.adapter = null
        binding.unbind()
        _binding = null
        _predictionListView = null
    }
}

