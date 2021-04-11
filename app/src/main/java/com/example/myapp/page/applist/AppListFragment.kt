package com.example.myapp.page.applist

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.databinding.FragmentAppListBinding
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Singleton

/**
 * A simple [Fragment] subclass.
 */
@AndroidEntryPoint
@Singleton
class AppListFragment : Fragment() {
    private lateinit var binding: FragmentAppListBinding
    lateinit var getContent :ActivityResultLauncher<String>

    private val viewModel: AppListViewModel
            by navGraphViewModels(R.id.app_navigation) { defaultViewModelProviderFactory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val adapter = AppListAdapter(
            viewLifecycleOwner,
            viewModel,
            this
        )

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
            speedDial.apply {
                addActionItem(
                    SpeedDialActionItem.Builder(
                        R.id.save_action, R.drawable.ic_baseline_save_18
                    ).create()
                )
                addActionItem(
                    SpeedDialActionItem.Builder(
                        R.id.share_action, R.drawable.ic_baseline_share_24
                    ).create()
                )
                setOnActionSelectedListener(SpeedDialView.OnActionSelectedListener { actionItem ->
                    when (actionItem.id) {
                        R.id.save_action -> {
                            viewModel.saveAction()
                            close()
                            return@OnActionSelectedListener true
                        }
                        R.id.share_action -> {
                            viewModel.shareAction()
                            close()
                            return@OnActionSelectedListener true
                        }
                    }
                    false
                })
            }
        }

        getContent =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                viewModel.setImageUri(uri)
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

    fun selectImage() {
        getContent.launch("image/*")
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

        override fun onMoved(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            fromPos: Int,
            target: RecyclerView.ViewHolder,
            toPos: Int,
            x: Int,
            y: Int
        ) {
            super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition
            val fromUserAppCard = fromPosition - 1
            val toUserAppCard = toPosition -1
            viewModel.replaceAppData(fromUserAppCard, toUserAppCard)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return when (target) {
                is AppListAdapter.ViewHolder -> {
                    true
                }
                else -> false
            }
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val appCardId = (viewHolder as AppListAdapter.ViewHolder).binding.appCard!!.id
            viewModel.removeAppDataFromList(appCardId)
        }
    })
}