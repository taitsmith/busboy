package com.taitsmith.busboy.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.taitsmith.busboy.databinding.FragmentBusDetailBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BusDetailFragment: Fragment() {
    private val args: BusDetailFragmentArgs by navArgs()
    private val bus by lazy {
        args.selectedBus
    }
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}