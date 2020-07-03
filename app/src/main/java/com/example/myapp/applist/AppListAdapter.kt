package com.example.myapp.applist

import android.content.pm.PackageManager
import android.content.pm.PackageManager.MATCH_UNINSTALLED_PACKAGES
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.database.AppCard
import com.example.myapp.databinding.ListItemAppBinding
import kotlinx.coroutines.CoroutineScope

private const val  ITEM_VIEW_TYPE_ITEM = 0
private const val  ITEM_VIEW_TYPE_BUTTON = 1

class AppListAdapter : ListAdapter<DataItem, RecyclerView.ViewHolder>(AppDataDiffCallback()) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is ViewHolder -> {
                val item = getItem(position) as DataItem.AppCardItem
                holder.bind(item.appCard)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType){
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
            ITEM_VIEW_TYPE_BUTTON -> ButtonViewHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)){
            is DataItem.AppCardItem -> ITEM_VIEW_TYPE_ITEM
            is DataItem.AddAppButton -> ITEM_VIEW_TYPE_BUTTON
        }
    }

    fun submitReviewList(list: List<AppCard>?){
        val items = when (list){
            null -> listOf(DataItem.AddAppButton)
            else -> list.map{DataItem.AppCardItem(it)} + listOf(DataItem.AddAppButton)
        }
        submitList(items)
    }

    class ButtonViewHolder(view: View): RecyclerView.ViewHolder(view){
        companion object{
            fun from(parent: ViewGroup): ButtonViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.add_app_button, parent, false)
                return ButtonViewHolder(view)
            }
        }
    }

    class ViewHolder private constructor(private val binding: ListItemAppBinding,private val pm :PackageManager) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AppCard){
            binding.appCard = item
            val appInfo = pm.getApplicationInfo(item.packageName,MATCH_UNINSTALLED_PACKAGES)
            binding.appImage.setImageDrawable(appInfo.loadIcon(pm))
            binding.appName.text = appInfo.loadLabel(pm).toString()
            binding.textAppCardId.text = item.id.toString()
            binding.executePendingBindings()
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
        override val id = Long.MAX_VALUE
    }

    abstract val id:Long
}