package com.example.myapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.myapp.databinding.FragmentTitleBinding

class TitleFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        val binding = DataBindingUtil.inflate<FragmentTitleBinding>(inflater, R.layout.fragment_title, container, false)

        binding.floatingActionButton.setOnClickListener{view: View ->
            val action = TitleFragmentDirections.actionTitleFragmentToAppListFragment(false)
            view.findNavController().navigate(action)
        }

        binding.createNewListButton.setOnClickListener{view: View ->
            val action = TitleFragmentDirections.actionTitleFragmentToAppListFragment(true)
            view.findNavController().navigate(action)
        }

        return binding.root
    }

}
