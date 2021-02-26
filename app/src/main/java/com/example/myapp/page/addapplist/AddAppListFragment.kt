package com.example.myapp.page.addapplist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapp.R
import com.example.myapp.databinding.FragmentAddAppListBinding
import com.example.myapp.page.addapplist.AddAppListAdapter
import com.example.myapp.page.AppListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddAppListFragment:Fragment() {
    private lateinit var binding: FragmentAddAppListBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewModel: AppListViewModel by activityViewModels()
        val adapter = AddAppListAdapter()
        binding = DataBindingUtil.inflate<FragmentAddAppListBinding>(
            inflater,
            R.layout.fragment_add_app_list,
            container,
            false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
            appListViewModel = viewModel
            addAppList.adapter = adapter
            addAppList.layoutManager = GridLayoutManager(
                context,5,GridLayoutManager.VERTICAL,false
            )
            addAppButton.setOnClickListener { view ->
                viewModel.registerAddAppName()
                val action =
                    AddAppListFragmentDirections.actionAddAppListFragmentToAppListFragment()
                view.findNavController().navigate(action)
            }
        }
        viewModel.addAppNameList.observe(viewLifecycleOwner, Observer{
            it?.let {
                adapter.submitList(it)
            }
        })
        return binding.root
    }
}