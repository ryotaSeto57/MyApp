package com.example.myapp.applist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.database.AppDatabase
import com.example.myapp.database.AppDatabaseDao
import com.example.myapp.databinding.FragmentAppListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.list_item_app.view.*

/**
 * A simple [Fragment] subclass.
 */
@AndroidEntryPoint
class AppListFragment : Fragment() {
    private lateinit var binding: FragmentAppListBinding
    private val viewModel: AppListViewModel by activityViewModels()

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

        viewModel.userAppCardList.observe(viewLifecycleOwner, Observer {
            it?.let {
                adapter.submitReviewList(it)
            }
        })

        return binding.root
    }

    private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            return makeMovementFlags(UP or DOWN, LEFT)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition
            viewModel.replaceAppData(fromPosition, toPosition)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val appId = viewHolder.itemView.textAppCardId.text.toString().toLong()
            viewModel.removeAppDataFromList(viewHolder.adapterPosition, appId)
        }
    })
}