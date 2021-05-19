package com.on.smartphone.page.sharedapplist

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.navGraphViewModels
import com.on.smartphone.R
import com.on.smartphone.databinding.FragmentSharedAppListBinding
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
            goToWebButton.setOnClickListener { view:View ->
                val url = viewModel.listUrl.value
                val intent = Intent(Intent.ACTION_VIEW,(getString(R.string.web_site)+url).toUri() )
                startActivity(intent)
            }
        }

        return binding.root
    }


    override fun onDestroy() {
        Timber.i("SharedAppListFragment is destroyed.")
        super.onDestroy()
    }
}