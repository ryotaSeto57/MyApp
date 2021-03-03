package com.example.myapp.page.applist

import android.content.pm.PackageManager
import android.content.pm.PackageManager.MATCH_UNINSTALLED_PACKAGES
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.database.AppCard
import com.example.myapp.databinding.AddAppButtonBinding
import com.example.myapp.databinding.ListItemAppBinding
import com.example.myapp.databinding.ShareButtonBinding

private const val  ITEM_VIEW_TYPE_ITEM = 0
private const val  ITEM_VIEW_TYPE_BUTTON = 1
private  const val ITEM_VIEW_TYPE_SHARE_BUTTON = 2

class AppListAdapter(
    private val viewLifecycleOwner: LifecycleOwner,
    private val viewModel: AppListViewModel
) : ListAdapter<DataItem, RecyclerView.ViewHolder>(AppDataDiffCallback()) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is ViewHolder -> {
                val item = getItem(position) as DataItem.AppCardItem
                holder.bind(item.appCard, viewLifecycleOwner, viewModel)
            }
            is ButtonHolder -> {
                holder.bind(viewLifecycleOwner, viewModel)
            }
            is ShareButtonHolder -> {
                holder.bind(viewModel)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType){
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
            ITEM_VIEW_TYPE_BUTTON -> ButtonHolder.from(parent)
            ITEM_VIEW_TYPE_SHARE_BUTTON -> ShareButtonHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)){
            is DataItem.AppCardItem -> ITEM_VIEW_TYPE_ITEM
            is DataItem.AddAppButton -> ITEM_VIEW_TYPE_BUTTON
            is DataItem.ShareButton -> ITEM_VIEW_TYPE_SHARE_BUTTON
        }
    }

    fun submitReviewList(list: MutableList<AppCard>?){
        val items = when (list){
            null -> listOf(DataItem.AddAppButton)
            else -> list.map{DataItem.AppCardItem(it)} +
                    listOf(DataItem.AddAppButton) +listOf(DataItem.ShareButton)
        }
        submitList(items)
    }

    class ButtonHolder private constructor(
        private val binding :AddAppButtonBinding
        ): RecyclerView.ViewHolder(binding.root){
        fun bind(viewLifecycleOwner: LifecycleOwner,viewModel: AppListViewModel){
            binding.run {
                appListViewModel = viewModel
                lifecycleOwner = viewLifecycleOwner
                addAppCardButton.setOnClickListener { button ->
                    viewModel.saveButtonPosition(adapterPosition)
                    button.findNavController().navigate(R.id.action_appListFragment_to_addAppListFragment)
                }
                executePendingBindings()
            }
        }
        companion object{
            fun from(parent: ViewGroup): ButtonHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = AddAppButtonBinding.inflate(layoutInflater, parent, false)
                return ButtonHolder(binding)
            }
        }
    }

    class ShareButtonHolder private constructor(
        private val binding : ShareButtonBinding
    ): RecyclerView.ViewHolder(binding.root){
        fun bind(viewModel: AppListViewModel){
            binding.run {
                this.appListViewModel = viewModel
            }
        }

        companion object{
            fun from(parent: ViewGroup): ShareButtonHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ShareButtonBinding.inflate(layoutInflater, parent, false)
                return ShareButtonHolder(binding)
            }
        }
    }

    class ViewHolder private constructor(val binding: ListItemAppBinding,
                                         private val pm: PackageManager)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AppCard, viewLifecycleOwner: LifecycleOwner, viewModel: AppListViewModel){
            binding.run {
                lifecycleOwner = viewLifecycleOwner
                appCard = item
                this.appListViewModel = viewModel
                val appInfo = pm.getApplicationInfo(item.packageName, MATCH_UNINSTALLED_PACKAGES)
                appImage.setImageDrawable(appInfo.loadIcon(pm))
                appName.text = appInfo.loadLabel(pm).toString()
                textAppCardId.text = item.id.toString()
                textAppCardIndex.text = item.index.toString()
                executePendingBindings()
            }
        }

        companion object{
            fun from(parent: ViewGroup) : ViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemAppBinding.inflate(layoutInflater, parent, false)
                val pm = parent.context.packageManager
                return ViewHolder(binding,pm)
            }
        }
    }
}

class AppDataDiffCallback : DiffUtil.ItemCallback<DataItem>(){
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}

sealed class DataItem{
    data class AppCardItem(val appCard: AppCard):DataItem() {
        override val id = appCard.id
    }

    object AddAppButton:DataItem(){
        override val id = Long.MAX_VALUE -1
    }

    object ShareButton:DataItem() {
        override val id = Long.MAX_VALUE
    }

    abstract val id:Long
}