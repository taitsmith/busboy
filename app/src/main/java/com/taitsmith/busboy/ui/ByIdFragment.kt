package com.taitsmith.busboy.ui

import android.content.Context
import dagger.hilt.android.AndroidEntryPoint
import com.taitsmith.busboy.viewmodels.ByIdViewModel
import com.taitsmith.busboy.api.StopPredictionResponse.BustimeResponse
import android.widget.EditText
import com.taitsmith.busboy.utils.PredictionAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.taitsmith.busboy.databinding.ByIdFragmentBinding
import com.taitsmith.busboy.utils.OnItemClickListener
import com.taitsmith.busboy.utils.OnItemLongClickListener
import com.taitsmith.busboy.viewmodels.MainActivityViewModel
import java.lang.ClassCastException
import java.lang.IndexOutOfBoundsException

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
        binding.searchByIdButton.setOnClickListener { search() }
        binding.addToFavoritesFab.setOnClickListener { byIdViewModel.addStopToFavorites() }
        predictionListView.onItemClickListener =
            AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, i: Int, _: Long ->
                listItemListener.onIdItemSelected(i)
            }
        predictionListView.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { _: AdapterView<*>?, _: View?, i: Int, _: Long ->
                longClickListener.onIdLongClick(i)
                true
            }

        predictionListView.setOnScrollListener(object: AbsListView.OnScrollListener {
            override fun onScrollStateChanged(p0: AbsListView?, p1: Int) {}
            //hide the fab when we get to the bottom of the list, unless the whole list is visible
            override fun onScroll(p0: AbsListView?, p1: Int, p2: Int, p3: Int) {
                if (p1 == (p3 - p2) && (p2 != p3)) binding.addToFavoritesFab.visibility = View.INVISIBLE
                else binding.addToFavoritesFab.visibility = View.VISIBLE
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        byIdViewModel = ViewModelProvider(requireActivity())[ByIdViewModel::class.java]
        setObservers()
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

    private fun setObservers() {
        byIdViewModel.mutableStopPredictions.observe(
            viewLifecycleOwner
        ) { predictions: List<BustimeResponse.Prediction> ->
            predictionList = predictions
            predictionAdapter = PredictionAdapter(predictionList!!)
            predictionListView.adapter = predictionAdapter
            binding.busFlagIV.visibility = View.INVISIBLE
            try {
                val s = predictions[0].stpnm
                binding.stopEntryEditText.text = null
                binding.stopEntryEditText.hint = s
                byIdViewModel.stop.name = s
            } catch (e: IndexOutOfBoundsException) {
                e.printStackTrace()
            }
            MainActivityViewModel.mutableStatusMessage.value = "LOADED"
        }
    }

    private fun search() {
        if (binding.stopEntryEditText.text.length == 5) {
            MainActivityViewModel.mutableStatusMessage.value = "LOADING"
            byIdViewModel.getStopPredictions(stopIdEditText.text.toString())
        } else {
            MainActivityViewModel.mutableErrorMessage.value = "BAD_INPUT"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        byIdViewModel.mutableStopPredictions.removeObservers(viewLifecycleOwner)
    }
}

