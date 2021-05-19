package com.on.smartphone.page.sharedapplist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.on.smartphone.database.ScreenShotItem
import com.on.smartphone.databinding.ListItemSharedScreenShotImageBinding

class SharedScreenShotAdapter(): ListAdapter<ScreenShotItem, SharedScreenShotAdapter.SharedScreenShotHolder>(SharedScreenShotDiffCallBack()) {

    override fun onBindViewHolder(holder: SharedScreenShotHolder, position: Int) {
       val item = getItem(position)
        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SharedScreenShotHolder{
        return SharedScreenShotHolder.from(parent)
    }

    class SharedScreenShotHolder private constructor(
        private val binding: ListItemSharedScreenShotImageBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ScreenShotItem) {
            binding.run {
                sharedScreenShotImage.setImageURI(item.uriString.toUri())
            }
        }

        companion object {
            fun from(parent: ViewGroup): SharedScreenShotHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemSharedScreenShotImageBinding.inflate(
                    layoutInflater, parent, false
                )
                return SharedScreenShotHolder(binding)
            }
        }
    }
}

class SharedScreenShotDiffCallBack(): DiffUtil.ItemCallback<ScreenShotItem>() {

    override fun areContentsTheSame(oldItem: ScreenShotItem, newItem: ScreenShotItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areItemsTheSame(oldItem: ScreenShotItem, newItem: ScreenShotItem): Boolean {
        return oldItem == newItem
    }
}