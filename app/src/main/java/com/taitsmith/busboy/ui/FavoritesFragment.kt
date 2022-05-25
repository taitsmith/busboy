package com.taitsmith.busboy.ui

import android.content.Context
import com.taitsmith.busboy.viewmodels.FavoritesViewModel
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.taitsmith.busboy.data.Stop
import com.taitsmith.busboy.databinding.FavoritesFragmentBinding
import com.taitsmith.busboy.utils.NearbyAdapter
import com.taitsmith.busboy.utils.OnItemClickListener
import com.taitsmith.busboy.utils.OnItemLongClickListener
import dagger.hilt.android.AndroidEntryPoint
import java.lang.ClassCastException

@AndroidEntryPoint
class FavoritesFragment : Fragment() {

    private val favoritesViewModel: FavoritesViewModel by viewModels()
    lateinit var binding: FavoritesFragmentBinding
    lateinit var favoritesListView: ListView

    private lateinit var listItemListener: OnItemClickListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FavoritesFragmentBinding.inflate(inflater, container, false)
        favoritesListView = binding.favoritesListView
        favoritesListView.onItemClickListener =
            AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, i: Int, _: Long ->
                listItemListener.onFavoriteItemSelected(i)
            }

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
            FavoritesViewModel.favoriteStops.addAll(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.unbind()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            listItemListener = context as OnItemClickListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                context.toString() +
                        "Must Implement OnListItemSelectedListener"
            )
        }
    }

    companion object {
        fun newInstance(): FavoritesFragment {
            return FavoritesFragment()
        }
    }
}