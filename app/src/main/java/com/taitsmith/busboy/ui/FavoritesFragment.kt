package com.taitsmith.busboy.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.taitsmith.busboy.databinding.FavoritesFragmentBinding
import com.taitsmith.busboy.utils.NearbyAdapter
import com.taitsmith.busboy.viewmodels.FavoritesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoritesFragment : Fragment() {

    private val favoritesViewModel: FavoritesViewModel by viewModels()
    private lateinit var nearbyAdapter: NearbyAdapter

    private var _binding: FavoritesFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FavoritesFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.favoritesListView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        nearbyAdapter = NearbyAdapter ({
            val action = FavoritesFragmentDirections
                .actionFavoritesFragmentToByIdFragment(it)
            findNavController().navigate(action)
        }, {
            favoritesViewModel.deleteStop(it)
        })
        binding.favoritesListView.adapter = nearbyAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                favoritesViewModel.getFavoriteStops().collect {
                    nearbyAdapter.submitList(it)
                }
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        binding.favoritesListView.adapter = null
        nearbyAdapter.submitList(null)
        _binding = null
    }
}