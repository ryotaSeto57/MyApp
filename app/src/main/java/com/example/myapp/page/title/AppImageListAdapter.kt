package com.example.myapp.page.title

import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.database.AppCard
import com.example.myapp.databinding.TitleItemAppImageBinding

class AppImageListAdapter(
    private val titleViewModel: TitleViewModel
) : ListAdapter<AppCard, RecyclerView.ViewHolder>(AppCardDiffCallback()) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val item = getItem(position) as AppCard
                return holder.bind(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder.from(parent)
    }

    fun submitAppCardList(listId: Long) {
        val list = titleViewModel.getAppCards(listId)
        submitList(list)
    }

    class ViewHolder private constructor(
        private val binding: TitleItemAppImageBinding,
        private val pm: PackageManager
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AppCard) {
            binding.run {
                appCard = item
                val appInfo = pm.getApplicationInfo(
                    item.packageName,
                    PackageManager.MATCH_UNINSTALLED_PACKAGES
                )
                appImage.setImageDrawable(appInfo.loadIcon(pm))
                executePendingBindings()
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = TitleItemAppImageBinding
                    .inflate(layoutInflater, parent, false)
                val pm = parent.context.packageManager
                return ViewHolder(binding, pm)
            }
        }
    }
}

private class AppCardDiffCallback() : DiffUtil.ItemCallback<AppCard>() {
    override fun areContentsTheSame(p0: AppCard, p1: AppCard): Boolean {
        return p0 == p1
    }

    override fun areItemsTheSame(p0: AppCard, p1: AppCard): Boolean {
        return p0.id == p1.id
    }
}