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
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.database.AppDatabase
import com.example.myapp.databinding.FragmentAppListBinding

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
        viewModelFactory = AppListViewModelFactory(dataSource,application)
        viewModel = ViewModelProvider(this,viewModelFactory).get(AppListViewModel::class.java)
        Log.i("AppListFragment", "Called ViewModelProviders.of")
        val adapter = AppListAdapter()

        binding.lifecycleOwner = viewLifecycleOwner

        binding.appList.adapter = adapter

        binding.appListViewModel = viewModel

        viewModel.appDataList.observe(viewLifecycleOwner, Observer{
            it?.let {
                adapter.submitList(it)
            }
        })

        val itemTouchHelper = ItemTouchHelper(object :ItemTouchHelper.SimpleCallback
                (ItemTouchHelper.UP or ItemTouchHelper.DOWN, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition

                viewModel.replaceAppData(fromPosition, toPosition)
                adapter.notifyItemMoved(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                viewModel.removeAppDataFromList(viewHolder.adapterPosition)
                adapter.notifyItemRemoved(viewHolder.adapterPosition)
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.appList)

        return binding.root
    }
}