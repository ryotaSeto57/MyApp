package com.example.myapp.page.title

import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.database.AppCard
import com.example.myapp.databinding.TitleItemAppImageBinding

class TitleAppImageAdapter(
    private val titleViewModel: TitleViewModel,
    private val viewLifecycleOwner: LifecycleOwner
) : ListAdapter<AppCard, RecyclerView.ViewHolder>(AppCardDiffCallback()) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val item = getItem(position) as AppCard
                return holder.bind(item, viewLifecycleOwner,titleViewModel)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(
        private val binding: TitleItemAppImageBinding,
        private val pm: PackageManager
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AppCard, viewLifecycleOwner: LifecycleOwner,titleViewModel: TitleViewModel) {
            binding.run {
                appCard = item
                val appInfo = try {
                    pm.getApplicationInfo(
                        item.packageName,
                        PackageManager.MATCH_UNINSTALLED_PACKAGES
                    )
                } catch (e: Exception) {
                    null
                }
                appImage.apply {
                    setOnClickListener { view: View ->
                        if(titleViewModel.userPastAppCardLists.value!!
                                .find { it.id == item.listId }?.listUrl ?:"" == "") {
                            val action =
                                TitleFragmentDirections.actionTitleFragmentToAppListFragment(
                                    item.listId,
                                    createNewList = false,
                                    underEdit = false
                                )
                            view.findNavController().navigate(action)
                        }else {
                            val action =
                                TitleFragmentDirections.actionTitleFragmentToSharedAppListFragment(
                                    listId = item.listId
                                )
                            view.findNavController().navigate(action)
                        }
                    }
                    setImageDrawable(appInfo?.loadIcon(pm)
                        ?: ResourcesCompat.getDrawable(context.resources, R.mipmap.ic_launcher,null))
                }
                lifecycleOwner = viewLifecycleOwner
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