package com.example.myapp.page.sharedapplist

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import com.example.myapp.R
import com.example.myapp.databinding.FragmentSharedAppListBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class SharedAppListFragment : Fragment() {
    private lateinit var binding: FragmentSharedAppListBinding

    private val viewModel: SharedAppListViewModel
        by navGraphViewModels(R.id.shared_app_navigation) { defaultViewModelProviderFactory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate<FragmentSharedAppListBinding>(
            inflater,
            R.layout.fragment_shared_app_list,
            container,
            false
        ).apply {
            sharedAppList.adapter = SharedAppListAdapter(viewModel, viewLifecycleOwner).apply {
                viewModel.userAppCards.observe(viewLifecycleOwner, {
                    it?.let {
                        submitSharedAppList(it)
                    }
                })
            }
        }

        return binding.root
    }


    override fun onDestroy() {
        Timber.i("SharedAppListFragment is destroyed.")
        super.onDestroy()
    }
}