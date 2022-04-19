package com.taitsmith.busboy.ui

import android.content.Context
import dagger.hilt.android.AndroidEntryPoint
import com.taitsmith.busboy.utils.NearbyAdapter
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.taitsmith.busboy.R
import com.taitsmith.busboy.databinding.NearbyFragmentBinding
import com.taitsmith.busboy.viewmodels.NearbyViewModel
import com.taitsmith.busboy.utils.OnItemClickListener
import com.taitsmith.busboy.utils.OnItemLongClickListener
import com.taitsmith.busboy.viewmodels.MainActivityViewModel
import java.lang.ClassCastException

@AndroidEntryPoint
class NearbyFragment : Fragment(), AdapterView.OnItemSelectedListener {
    private lateinit var listItemListener: OnItemClickListener
    private lateinit var listItemLongListener: OnItemLongClickListener
    private lateinit var nearbyStopListView: ListView
    private lateinit var binding: NearbyFragmentBinding
    private lateinit var nearbySearchButton: Button
    private lateinit var buslineSpinner: Spinner
    private lateinit var nearbyEditText: EditText

    private val nearbyViewModel: NearbyViewModel by viewModels()
    private var nearbyAdapter: NearbyAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = NearbyFragmentBinding.inflate(inflater, container, false)
        buslineSpinner = binding.buslineSpinner
        nearbyStopListView = binding.nearbyListView
        nearbySearchButton = binding.nearbySearchButton
        nearbyEditText = binding.nearbyEditText

        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.bus_lines, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        buslineSpinner.adapter = adapter
        buslineSpinner.onItemSelectedListener = this
        setListeners()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nearbyViewModel.checkLocationPerm()
        if (!MainActivity.enableNearbySearch) {
            nearbySearchButton.isEnabled = false
            nearbySearchButton.isClickable = false
        }
        setObservers()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listItemListener = context as OnItemClickListener
            listItemLongListener = context as OnItemLongClickListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                context.toString() +
                        "Must Implement OnListItemSelectedListener"
            )
        }
    }

    override fun onDestroyView() {
        nearbyViewModel.mutableNearbyStops.removeObservers(viewLifecycleOwner)
        binding.unbind()
        super.onDestroyView()
    }

    private fun setObservers() {
        nearbyViewModel.mutableNearbyStops.observe(viewLifecycleOwner) {
            nearbyAdapter = NearbyAdapter(it)
            nearbyStopListView.adapter = nearbyAdapter
            MainActivityViewModel.mutableStatusMessage.setValue("LOADED")
        }
    }

    private fun setListeners() {
        nearbyStopListView.onItemClickListener =
            AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, i: Int, _: Long ->
                listItemListener.onNearbyItemSelected(i)
            }

        nearbyStopListView.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { _: AdapterView<*>?, _: View?, i: Int, _: Long ->
                listItemLongListener.onNearbyLongClick(i)
                true
            }

        nearbySearchButton.setOnClickListener {
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

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val s = p0?.getItemAtPosition(p2).toString()
        if (s == "All lines") nearbyViewModel.rt = null
        else nearbyViewModel.rt = s
    }

    override fun onNothingSelected(adapterView: AdapterView<*>?) {}
}