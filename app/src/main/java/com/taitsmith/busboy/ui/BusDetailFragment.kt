package com.taitsmith.busboy.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.taitsmith.busboy.data.Bus
import com.taitsmith.busboy.databinding.FragmentBusDetailBinding

class BusDetailFragment : DialogFragment() {
    private var _binding: FragmentBusDetailBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBusDetailBinding.inflate(inflater, container, false)
        binding.bus = requireArguments().getSerializable(ARG_BUS) as Bus
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_BUS = "bus"

        //pass the Bus through the arguments Bundle (Bus is Serializable) rather than a constructor
        //parameter, so the dialog survives recreation (rotation / process death) instead of the
        //FragmentManager failing to re-instantiate a fragment that has no no-arg constructor.
        fun newInstance(bus: Bus): BusDetailFragment = BusDetailFragment().apply {
            arguments = Bundle().apply { putSerializable(ARG_BUS, bus) }
        }
    }
}
