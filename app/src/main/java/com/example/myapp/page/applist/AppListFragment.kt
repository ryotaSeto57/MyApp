package com.example.myapp.page.applist

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.databinding.FragmentAppListBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 */
@AndroidEntryPoint
class AppListFragment : Fragment() {
    private lateinit var binding: FragmentAppListBinding
    private val viewModel: AppListViewModel
            by navGraphViewModels(R.id.app_navigation) { defaultViewModelProviderFactory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val adapter = AppListAdapter(viewLifecycleOwner, viewModel)

        binding = DataBindingUtil.inflate<FragmentAppListBinding>(
            inflater,
            R.layout.fragment_app_list,
            container,
            false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
            appList.adapter = adapter.apply {
                registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        appList.scrollToPosition(
                            viewModel.topViewHolderPosition.value
                                ?: 0
                        )
                    }
                })
            }
            appListViewModel = viewModel
            itemTouchHelper.attachToRecyclerView(appList)
        }

        viewModel.userAppCards.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitReviewList(it)
            }
        })

        return binding.root
    }

    override fun onDestroy() {
        Timber.i("AppListFragment is destroyed.")
        super.onDestroy()
    }


    private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            return when (viewHolder) {
                is AppListAdapter.ViewHolder -> makeMovementFlags(UP or DOWN, LEFT)
                else -> ACTION_STATE_IDLE
            }
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return when (target) {
                is AppListAdapter.ViewHolder -> {
                    val fromPosition = viewHolder.adapterPosition
                    val toPosition = target.adapterPosition
                    viewModel.replaceAppData(fromPosition, toPosition)
                    true
                }
                else -> false
            }
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val appId = (viewHolder as AppListAdapter.ViewHolder).binding.appCard!!.id
            viewModel.removeAppDataFromList(viewHolder.adapterPosition, appId)
        }
    })
}