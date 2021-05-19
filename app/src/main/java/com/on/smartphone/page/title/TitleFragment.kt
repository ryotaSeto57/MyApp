package com.on.smartphone.page.title

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.on.smartphone.R
import com.on.smartphone.databinding.FragmentTitleBinding
import com.on.smartphone.repository.AppListRepository
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TitleFragment : Fragment() {
    private lateinit var binding: FragmentTitleBinding
    private val viewModel: TitleViewModel by viewModels()

    @Inject
    lateinit var appListRepository: AppListRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Timber.i("Title fragment is created.")
        Timber.i(savedInstanceState?.keySet()?.joinToString() ?:"")

        val adapter = TitleListAdapter(viewLifecycleOwner,viewModel)

        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate<FragmentTitleBinding>(inflater,
            R.layout.fragment_title, container, false).apply {

            titleCardList.adapter = adapter

            titleViewModel = viewModel

            viewModel.userPastAppCardLists.observe(viewLifecycleOwner,{
                it?.let {
                    adapter.submitAppCardList(it)
                }
            })
        }
        return binding.root
    }

}
