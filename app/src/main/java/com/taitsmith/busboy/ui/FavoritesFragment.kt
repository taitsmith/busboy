package com.taitsmith.busboy.ui

import android.content.Context
import com.taitsmith.busboy.viewmodels.FavoritesViewModel
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.taitsmith.busboy.databinding.FavoritesFragmentBinding
import com.taitsmith.busboy.utils.NearbyAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FavoritesFragment : Fragment() {

    private val favoritesViewModel: FavoritesViewModel by viewModels()
    lateinit var binding: FavoritesFragmentBinding
    lateinit var favoritesListView: ListView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FavoritesFragmentBinding.inflate(inflater, container, false)
        favoritesListView = binding.favoritesListView

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        favoritesViewModel.getFavoriteStops()
        setObservers()
    }

    private fun setObservers() {
        favoritesViewModel.stopList.observe(viewLifecycleOwner) {
            val adapter = NearbyAdapter(it)
            favoritesListView.adapter = adapter
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    companion object {
        fun newInstance(): FavoritesFragment {
            return FavoritesFragment()
        }
    }
}