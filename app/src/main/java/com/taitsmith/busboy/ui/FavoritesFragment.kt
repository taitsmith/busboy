package com.taitsmith.busboy.ui

import com.taitsmith.busboy.viewmodels.FavoritesViewModel
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.coroutineScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.taitsmith.busboy.databinding.FavoritesFragmentBinding
import com.taitsmith.busboy.utils.NearbyAdapter
import com.taitsmith.busboy.utils.RecyclerDivider
import com.taitsmith.busboy.viewmodels.MainActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoritesFragment : Fragment() {

    private val favoritesViewModel: FavoritesViewModel by viewModels()
    lateinit var favoritesListView: RecyclerView

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
        val adapter = NearbyAdapter ({
            MainActivityViewModel.mutableStatusMessage.value = "LOADING"
            val action = FavoritesFragmentDirections
                .actionFavoritesFragmentToByIdFragment(it)
            view.findNavController().navigate(action)
        }, {
            favoritesViewModel.deleteStop(it)
        })
        favoritesListView.addItemDecoration(RecyclerDivider(requireContext()))
        favoritesListView.adapter = adapter

        lifecycle.coroutineScope.launch {
            favoritesViewModel.getFavoriteStops().collect {
                adapter.submitList(it)
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        favoritesListView.adapter = null
    }
}