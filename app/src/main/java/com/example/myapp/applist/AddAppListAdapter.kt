package com.example.myapp.applist

import android.content.pm.PackageManager
import android.content.pm.PackageManager.MATCH_UNINSTALLED_PACKAGES
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.database.AddAppName
import com.example.myapp.databinding.ListItemAddAppBinding

class AddAppListAdapter: ListAdapter<AddAppName, AddAppListAdapter.AddAppListHolder>(AppNameDiffCallback()) {

    class AddAppListHolder(private val binding: ListItemAddAppBinding, private val pm: PackageManager): RecyclerView.ViewHolder(binding.root){
        fun bind(item:AddAppName){
            val appInfo  = pm.getApplicationInfo(item.packageName,MATCH_UNINSTALLED_PACKAGES)
            binding.run {
                addAppImage.setImageDrawable(appInfo.loadIcon(pm))
                addAppName = item
                executePendingBindings()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddAppListHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ListItemAddAppBinding.inflate(layoutInflater,parent,false)
        val pm = parent.context.packageManager
        return AddAppListHolder(binding,pm)
    }

    override fun onBindViewHolder(holder: AddAppListHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class AppNameDiffCallback : DiffUtil.ItemCallback<AddAppName>(){
    override fun areItemsTheSame(oldItem: AddAppName, newItem: AddAppName): Boolean {
        return oldItem.packageName == newItem.packageName
    }

    override fun areContentsTheSame(oldItem: AddAppName, newItem: AddAppName): Boolean {
        return oldItem.addOrNot.value == newItem.addOrNot.value
    }

}