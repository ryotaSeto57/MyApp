package com.example.myapp.page.title

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.example.myapp.R
import com.example.myapp.databinding.FragmentTitleBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TitleFragment : Fragment() {
    private lateinit var binding: FragmentTitleBinding
    private val viewModel: TitleViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val adapter = TitleListAdapter(viewLifecycleOwner,viewModel)

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate<FragmentTitleBinding>(inflater,
            R.layout.fragment_title, container, false).apply {

            titleCardList.adapter = adapter

            createNewListButton.setOnClickListener { view: View ->
                val action = TitleFragmentDirections.actionTitleFragmentToAppListFragment()
                view.findNavController().navigate(action)
            }

            viewModel.userPastAppCardLists.observe(viewLifecycleOwner,{
                it?.let {
                    adapter.submitAppCardList(it)
                }
            })
        }
        return binding.root
    }

}
