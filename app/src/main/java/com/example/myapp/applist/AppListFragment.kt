package com.example.myapp.applist

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.database.AppDatabase
import com.example.myapp.databinding.FragmentAppListBinding
import kotlinx.android.synthetic.main.list_item_app.view.*

/**
 * A simple [Fragment] subclass.
 */
class AppListFragment : Fragment() {

    private lateinit var binding: FragmentAppListBinding
    private lateinit var viewModel: AppListViewModel
    private lateinit var viewModelFactory: AppListViewModelFactory

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        binding = DataBindingUtil.inflate<FragmentAppListBinding>(
            inflater,
            R.layout.fragment_app_list,
            container,
            false
        )

        val application = requireNotNull(this.activity).application
        val dataSource = AppDatabase.getInstance(application).appDatabaseDao
        viewModelFactory = AppListViewModelFactory(dataSource,application,AppListFragmentArgs.fromBundle(requireArguments()).createNewList)
        viewModel = ViewModelProvider(this,viewModelFactory).get(AppListViewModel::class.java)
        Log.i("AppListFragment", "Called ViewModelProviders.of")
        val adapter = AppListAdapter(viewLifecycleOwner,this@AppListFragment.viewModel)

        binding.lifecycleOwner = viewLifecycleOwner

        binding.appList.adapter = adapter

        binding.appListViewModel = viewModel

        viewModel.userAppReviewList.observe(viewLifecycleOwner, Observer{
            it?.let {
//                viewModel.updateUserAppReviewList(it)
                adapter.submitReviewList(it)
            }
        })
//        adapter.submitReviewList(viewModel.userAppReviewList)

        val itemTouchHelper = ItemTouchHelper(object :ItemTouchHelper.Callback() {

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
//                adapter.submitReviewList(viewModel.userAppReviewList.value)
//                viewModel.sortUserAppReviewList()
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val appId =viewHolder.itemView.textAppCardId.text.toString().toLong()
                viewModel.removeAppDataFromList(viewHolder.adapterPosition,appId)
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
//                viewModel.replaceAppData(fromPos,toPos)
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.appList)

        return binding.root
    }
}