package com.example.myapp.page.title

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.myapp.R
import com.example.myapp.databinding.FragmentTitleBinding
import com.example.myapp.repository.AppListRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TitleFragment : Fragment() {
    private lateinit var binding: FragmentTitleBinding
    private val viewModel: TitleViewModel by viewModels()

    @Inject
    lateinit var appListRepository: AppListRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


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
                val action = TitleFragmentDirections.actionTitleFragmentToAppListFragment(createNewList =true)
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
