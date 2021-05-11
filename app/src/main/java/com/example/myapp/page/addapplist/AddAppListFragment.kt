package com.example.myapp.page.addapplist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.myapp.R
import com.example.myapp.databinding.FragmentAddAppListBinding
import com.example.myapp.page.applist.AppListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddAppListFragment : Fragment() {
    private lateinit var binding: FragmentAddAppListBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewModel: AppListViewModel
                by navGraphViewModels(R.id.app_navigation) { defaultViewModelProviderFactory }
        val adapter = AddAppListAdapter(viewModel)
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
                context, 5, GridLayoutManager.VERTICAL, false
            ).apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        if (adapter.getItemViewType(position) == ITEM_VIEW_TYPE_ADD_BUTTON) {
                            return 5
                        }
                        return 1
                    }
                }
            }
        }
        viewModel.addAppNameList.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitAddAppList(it)
            }
        })
        return binding.root
    }

}