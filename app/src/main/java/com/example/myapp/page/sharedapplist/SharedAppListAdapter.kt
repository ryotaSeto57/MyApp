package com.example.myapp.page.sharedapplist

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.database.AppCard
import com.example.myapp.databinding.ListItemScreenShotBinding
import com.example.myapp.databinding.ListItemSharedAppBinding
import com.example.myapp.databinding.ListItemSharedScreenShotBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception

private const val ITEM_VIEW_TYPE_SCREEN_SHOT = 0
private const val ITEM_VIEW_TYPE_ITEM = 1
private const val ERROR_MESSAGE_OF_APP_NAME = "削除されました"

class SharedAppListAdapter (
    private val viewModel: SharedAppListViewModel,
    private val viewLifecycleOwner: LifecycleOwner
) : ListAdapter<DataItem, RecyclerView.ViewHolder>(AppDataDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val item = getItem(position) as DataItem.AppCardItem
                holder.bind(item.appCard)
            }
            is ScreenShotHolder -> {
                holder.bind(viewModel, viewLifecycleOwner)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
            ITEM_VIEW_TYPE_SCREEN_SHOT -> ScreenShotHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.AppCardItem -> ITEM_VIEW_TYPE_ITEM
            is DataItem.ScreenShot -> ITEM_VIEW_TYPE_SCREEN_SHOT
        }
    }

    fun submitSharedAppList(list: MutableList<AppCard>) {
        adapterScope.launch {
            withContext(Dispatchers.Main) {
                submitList(listOf(DataItem.ScreenShot) + list.map { DataItem.AppCardItem(it) })
            }
        }
    }

    class ScreenShotHolder private constructor(
        val binding: ListItemSharedScreenShotBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(viewModel: SharedAppListViewModel, viewLifecycleOwner: LifecycleOwner) {
            binding.run {
                sharedScreenShotView.apply {
                    layoutManager =
                        LinearLayoutManager(
                            context,
                            LinearLayoutManager.HORIZONTAL,
                            false
                        )
                    adapter = SharedScreenShotAdapter().apply {
                        viewModel.screenShotItemList.observe(viewLifecycleOwner, {
                            it?.let {
                                submitList(it)
                            }
                        })
                    }
                }
                sharedAppListViewModel = viewModel
                executePendingBindings()
            }
        }

        companion object {
            fun from(parent: ViewGroup): ScreenShotHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemSharedScreenShotBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return ScreenShotHolder(binding)
            }
        }
    }

    class ViewHolder private constructor(
        val binding: ListItemSharedAppBinding,
        private val pm: PackageManager
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AppCard) {
            binding.run {
                appCard = item
                val appInfo: ApplicationInfo? =
                    try {
                        pm.getApplicationInfo(
                            item.packageName,
                            PackageManager.MATCH_UNINSTALLED_PACKAGES
                        )
                    } catch (e: Exception) {
                        null
                    }
                sharedAppImage.apply {
                    setImageDrawable(
                        appInfo?.loadIcon(pm) ?: ResourcesCompat.getDrawable(
                            context.resources,
                            R.mipmap.ic_launcher,
                            null
                        )
                    )
                }
                sharedAppName.text = appInfo?.loadLabel(pm)?.toString() ?: ERROR_MESSAGE_OF_APP_NAME
                executePendingBindings()
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemSharedAppBinding.inflate(layoutInflater, parent, false)
                val pm = parent.context.packageManager
                return ViewHolder(binding, pm)
            }
        }
    }
}

class AppDataDiffCallback : DiffUtil.ItemCallback<DataItem>() {
    override fun areItemsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: DataItem, newItem: DataItem): Boolean {
        return oldItem == newItem
    }
}

sealed class DataItem {
    data class AppCardItem(val appCard: AppCard) : DataItem() {
        override val id = appCard.originalIndex.toLong()
    }

    object ScreenShot : DataItem() {
        override val id = Long.MIN_VALUE
    }

    abstract val id: Long
}