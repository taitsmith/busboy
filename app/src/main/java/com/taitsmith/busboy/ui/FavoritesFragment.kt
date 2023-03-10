package com.taitsmith.busboy.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.taitsmith.busboy.databinding.FavoritesFragmentBinding
import com.taitsmith.busboy.utils.NearbyAdapter
import com.taitsmith.busboy.viewmodels.FavoritesViewModel
import com.taitsmith.busboy.viewmodels.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoritesFragment : Fragment() {

    private val favoritesViewModel: FavoritesViewModel by viewModels()
    lateinit var favoritesListView: RecyclerView
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
        favoritesListView = binding.favoritesListView
        favoritesListView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        nearbyAdapter = NearbyAdapter ({
            MainActivityViewModel.mutableStatusMessage.value = "LOADING"
            val action = FavoritesFragmentDirections
                .actionFavoritesFragmentToByIdFragment(it)
            view.findNavController().navigate(action)
        }, {
            favoritesViewModel.deleteStop(it)
        })
        favoritesListView.adapter = nearbyAdapter

        lifecycle.coroutineScope.launch {
            favoritesViewModel.getFavoriteStops().collect {
                nearbyAdapter.submitList(it)
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        favoritesListView.removeAllViews()
        _binding = null
        favoritesListView.adapter = null
        nearbyAdapter.submitList(null)
    }
}