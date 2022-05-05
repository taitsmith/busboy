package com.taitsmith.busboy.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.taitsmith.busboy.databinding.FragmentMainActivityBinding

class MainActivityFragment : Fragment() {

    private lateinit var binding: FragmentMainActivityBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainActivityBinding.inflate(inflater, container, false)
        return binding.root
    }

}