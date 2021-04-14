package com.example.myapp.page.applist

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.databinding.ListItemScreenShotImageBinding

class ScreenShotAdapter(private val viewLifecycleOwner: LifecycleOwner) : ListAdapter<Uri, RecyclerView.ViewHolder>(ImageDiffCallBack()) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        return (holder as ScreenShotImageHolder).bind(item,viewLifecycleOwner)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return  ScreenShotImageHolder.from(parent)
    }

    class ScreenShotImageHolder private constructor(
        private val binding: ListItemScreenShotImageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Uri,viewLifecycleOwner: LifecycleOwner) {
            binding.run {
                screenShotImage.setImageURI(item)
                lifecycleOwner = viewLifecycleOwner
            }
        }

        companion object {
            fun from(parent: ViewGroup): ScreenShotImageHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemScreenShotImageBinding.inflate(
                    layoutInflater, parent, false
                )
                return ScreenShotImageHolder(binding)
            }
        }
    }
}

class ImageDiffCallBack() : DiffUtil.ItemCallback<Uri>() {
    override fun areContentsTheSame(oldItem: Uri, newItem: Uri): Boolean {
        return oldItem == newItem
    }

    override fun areItemsTheSame(oldItem: Uri, newItem: Uri): Boolean {
        return oldItem == newItem
    }
}