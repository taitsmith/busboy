package com.taitsmith.busboy.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.taitsmith.busboy.data.Bus
import com.taitsmith.busboy.databinding.FragmentBusDetailBinding

class BusDetailFragment(private val bus: Bus): DialogFragment() {
    private var _binding: FragmentBusDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBusDetailBinding.inflate(inflater, container, false)
        binding.bus = bus
        return binding.root
    }
}