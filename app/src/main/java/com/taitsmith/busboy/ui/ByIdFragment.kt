package com.taitsmith.busboy.ui

import android.content.Context
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import com.taitsmith.busboy.di.DatabaseRepository
import com.taitsmith.busboy.viewmodels.ByIdViewModel
import com.taitsmith.busboy.obj.StopPredictionResponse.BustimeResponse
import android.widget.EditText
import com.taitsmith.busboy.utils.PredictionAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.taitsmith.busboy.databinding.ByIdFragmentBinding
import com.taitsmith.busboy.obj.Stop
import com.taitsmith.busboy.utils.OnItemClickListener
import com.taitsmith.busboy.utils.OnItemLongClickListener
import com.taitsmith.busboy.viewmodels.MainActivityViewModel
import java.lang.ClassCastException
import java.lang.IndexOutOfBoundsException
import java.lang.NullPointerException

@AndroidEntryPoint
class ByIdFragment : Fragment() {

    lateinit var byIdViewModel: ByIdViewModel
    lateinit var binding: ByIdFragmentBinding
    private lateinit var listItemListener: OnItemClickListener
    private lateinit var longClickListener: OnItemLongClickListener
    private lateinit var predictionListView: ListView

    @JvmField
    var predictionList: List<BustimeResponse.Prediction>? = null
    private lateinit var stopIdEditText: EditText
    private lateinit var predictionAdapter: PredictionAdapter
    var stop: Stop = Stop()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ByIdFragmentBinding.inflate(inflater, container, false)
        predictionListView = binding.predictionListView
        stopIdEditText = binding.stopEntryEditText
        if (arguments != null) {
            byIdViewModel.getStopPredictions(requireArguments()["BY_ID"].toString())
        }
        setListeners()
        return binding.root
    }

    private fun setListeners() {
        binding.searchByIdButton.setOnClickListener { view: View -> search(view) }
        binding.addToFavoritesFab.setOnClickListener { view: View -> addToFavorites(view) }
        predictionListView.onItemClickListener =
            AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, i: Int, _: Long ->
                listItemListener.onIdItemSelected(i)
            }
        predictionListView.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { _: AdapterView<*>?, _: View?, i: Int, _: Long ->
                longClickListener.onIdLongClick(i)
                true
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        byIdViewModel = ViewModelProvider(requireActivity()).get(ByIdViewModel::class.java)
        setObserver()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listItemListener = context as OnItemClickListener
            longClickListener = context as OnItemLongClickListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                context.toString() +
                        "Must Implement OnListItemSelectedListener"
            )
        }
    }

    private fun setObserver() {
        byIdViewModel.mutableStopPredictions.observe(
            viewLifecycleOwner
        ) { predictions: List<BustimeResponse.Prediction> ->
            predictionList = predictions
            predictionAdapter = PredictionAdapter(predictionList)
            predictionListView.adapter = predictionAdapter
            binding.busFlagIV.visibility = View.INVISIBLE
            try {
                binding.stopEntryEditText.text = null
                binding.stopEntryEditText.hint = predictions[0].stpnm
            } catch (e: NullPointerException) {
                e.printStackTrace()
            } catch (e: IndexOutOfBoundsException) {
                e.printStackTrace()
            }
            MainActivityViewModel.mutableStatusMessage.value = "LOADED"
        }
    }

    private fun search(view: View) {
        if (binding.stopEntryEditText.text.length == 5) {
            MainActivityViewModel.mutableStatusMessage.value = "LOADING"
            byIdViewModel.getStopPredictions(stopIdEditText.text.toString())
            stop.stopId = stopIdEditText.text.toString()
        } else {
            MainActivityViewModel.mutableErrorMessage.value = "BAD_INPUT"
        }
    }

    private fun addToFavorites(view: View) {}
    override fun onDetach() {
        super.onDetach()
        listItemListener
        longClickListener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        byIdViewModel.mutableStopPredictions.removeObservers(viewLifecycleOwner)
    }
}