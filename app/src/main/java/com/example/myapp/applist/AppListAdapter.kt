package com.example.myapp.applist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.database.AppData
import com.example.myapp.databinding.ListItemAppBinding

class AppListAdapter : ListAdapter<AppData, AppListAdapter.ViewHolder>(
    AppDataDiffCallback()
) {


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = getItem(position)

        holder.bind(item)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: ListItemAppBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AppData){
            binding.app = item
            binding.executePendingBindings()
        }

        companion object{
            fun from(parent: ViewGroup) : ViewHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemAppBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class AppDataDiffCallback : DiffUtil.ItemCallback<AppData>(){
    override fun areItemsTheSame(oldItem: AppData, newItem: AppData): Boolean {
        return oldItem.pubSouDirAsUid == newItem.pubSouDirAsUid
    }

    override fun areContentsTheSame(oldItem: AppData, newItem: AppData): Boolean {
        return oldItem == newItem
    }

}