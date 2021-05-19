package com.on.smartphone.page.addapplist

import android.content.pm.PackageManager
import android.content.pm.PackageManager.MATCH_UNINSTALLED_PACKAGES
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.on.smartphone.database.AddAppName
import com.on.smartphone.databinding.AddListItemAppBinding
import com.on.smartphone.databinding.AddListItemButtonBinding
import com.on.smartphone.page.applist.AppListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

const val ITEM_VIEW_TYPE_APP_ITEM = 0
const val ITEM_VIEW_TYPE_ADD_BUTTON = 1

class AddAppListAdapter(
    private val appListViewModel: AppListViewModel
) : ListAdapter<AddDataItem, RecyclerView.ViewHolder>(AppNameDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    class AddAppHolder(
        private val binding: AddListItemAppBinding,
        private val pm: PackageManager
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AddAppName) {
            val appInfo = pm.getApplicationInfo(item.packageName, MATCH_UNINSTALLED_PACKAGES)
            binding.run {
                addAppImage.setImageDrawable(appInfo.loadIcon(pm))
                addAppName = item
                executePendingBindings()
            }
        }

        companion object {
            fun from(parent: ViewGroup): AddAppHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = AddListItemAppBinding.inflate(layoutInflater, parent, false)
                val pm = parent.context.packageManager
                return AddAppHolder(binding, pm)
            }
        }
    }

    class AddButtonHolder(
        private val binding: AddListItemButtonBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(viewModel: AppListViewModel) {
            binding.run {
                appListViewModel = viewModel
                addAppButton.setOnClickListener { view ->
                    viewModel.registerAddAppName()
                    val action =
                        AddAppListFragmentDirections.actionAddAppListFragmentToAppListFragment()
                    view.findNavController().navigate(action)
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): AddButtonHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = AddListItemButtonBinding.inflate(layoutInflater, parent, false)
                return AddButtonHolder(binding)
            }
        }
    }

    fun submitAddAppList(list: MutableList<AddAppName>) {
        adapterScope.launch {
            val items = when (list) {
                null -> listOf(AddDataItem.AddAppButton)
                else -> list.map { AddDataItem.AppItem(it) } + listOf(AddDataItem.AddAppButton)
            }
            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

override fun getItemViewType(position: Int): Int {
    return when (getItem(position)) {
        is AddDataItem.AppItem -> ITEM_VIEW_TYPE_APP_ITEM
        is AddDataItem.AddAppButton -> ITEM_VIEW_TYPE_ADD_BUTTON
    }
}

override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
    return when (viewType) {
        ITEM_VIEW_TYPE_APP_ITEM -> AddAppHolder.from(parent)
        ITEM_VIEW_TYPE_ADD_BUTTON -> AddButtonHolder.from(parent)
        else -> throw ClassCastException("Unknown viewType $viewType")
    }
}

override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
    when (holder) {
        is AddAppHolder -> {
            val item = getItem(position) as AddDataItem.AppItem
            holder.bind(item.addAppName)
        }
        is AddButtonHolder -> holder.bind(appListViewModel)
    }
}
}

class AppNameDiffCallback : DiffUtil.ItemCallback<AddDataItem>() {
    override fun areItemsTheSame(oldItem: AddDataItem, newItem: AddDataItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: AddDataItem, newItem: AddDataItem): Boolean {
        return oldItem.id == newItem.id
    }

}

sealed class AddDataItem {

    data class AppItem(val addAppName: AddAppName) : AddDataItem() {
        override val id: Long = addAppName.id
    }

    object AddAppButton : AddDataItem() {
        override val id: Long = Long.MAX_VALUE
    }

    abstract val id: Long
}