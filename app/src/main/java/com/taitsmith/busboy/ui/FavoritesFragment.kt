package com.taitsmith.busboy.ui

import android.content.Context
import com.taitsmith.busboy.viewmodels.FavoritesViewModel
import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.taitsmith.busboy.databinding.FavoritesFragmentBinding
import com.taitsmith.busboy.utils.NearbyAdapter
import com.taitsmith.busboy.utils.OnItemClickListener
import com.taitsmith.busboy.utils.OnItemLongClickListener
import dagger.hilt.android.AndroidEntryPoint
import java.lang.ClassCastException

@AndroidEntryPoint
class FavoritesFragment : Fragment() {

    private val favoritesViewModel: FavoritesViewModel by viewModels()
    lateinit var favoritesListView: ListView
    private lateinit var adapter: NearbyAdapter

    private var _listItemListener: OnItemClickListener? = null
    private var _listItemLongClickListener: OnItemLongClickListener? = null
    private var _binding: FavoritesFragmentBinding? = null

    private val binding get() = _binding!!
    private val listItemListener get() = _listItemListener!!
    private val listItemLongListener get() = _listItemLongClickListener!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FavoritesFragmentBinding.inflate(inflater, container, false)
        favoritesListView = binding.favoritesListView
        favoritesListView.onItemClickListener =
            AdapterView.OnItemClickListener { _: AdapterView<*>?, _: View?, i: Int, _: Long ->
                listItemListener.onFavoriteItemSelected(i)
            }
        favoritesListView.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { _, _, i, _ ->
                listItemLongListener.onFavoriteLongClick(i)
                true
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
            adapter = NearbyAdapter(it)
            favoritesListView.adapter = adapter
            FavoritesViewModel.favoriteStops.addAll(it)

        }
        FavoritesViewModel.stopToDelete.observe(viewLifecycleOwner) {
            stop -> favoritesViewModel.deleteStop(stop)
            favoritesViewModel.getFavoriteStops()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _listItemListener = null
        _listItemLongClickListener = null
        favoritesListView.adapter = null
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            _listItemListener = context as OnItemClickListener
            _listItemLongClickListener = context as OnItemLongClickListener
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