package com.on.smartphone.page.applist

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.MATCH_UNINSTALLED_PACKAGES
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.on.smartphone.R
import com.on.smartphone.database.AppCard
import com.on.smartphone.databinding.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


private const val ITEM_VIEW_TYPE_SCREEN_SHOT = 0
private const val ITEM_VIEW_TYPE_ITEM = 1
private const val ITEM_VIEW_TYPE_BUTTON = 2
private const val ERROR_MESSAGE_OF_APP_NAME = "削除されました"

class AppListAdapter(
    private val viewLifecycleOwner: LifecycleOwner,
    private val viewModel: AppListViewModel,
    private val fragment: AppListFragment
) : ListAdapter<DataItem, RecyclerView.ViewHolder>(AppDataDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val item = getItem(position) as DataItem.AppCardItem
                holder.bind(item.appCard, viewLifecycleOwner, viewModel)
            }
            is ButtonHolder -> {
                holder.bind(viewLifecycleOwner, viewModel)
            }
            is ScreenShotHolder -> {
                holder.bind(viewModel, fragment, viewLifecycleOwner)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_ITEM -> ViewHolder.from(parent)
            ITEM_VIEW_TYPE_BUTTON -> ButtonHolder.from(parent)
            ITEM_VIEW_TYPE_SCREEN_SHOT -> ScreenShotHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DataItem.AppCardItem -> ITEM_VIEW_TYPE_ITEM
            is DataItem.AddAppButton -> ITEM_VIEW_TYPE_BUTTON
            is DataItem.ScreenShot -> ITEM_VIEW_TYPE_SCREEN_SHOT
        }
    }

    fun submitReviewList(list: MutableList<AppCard>?) {
        adapterScope.launch {
            val items = when (list) {
                null -> listOf(DataItem.AddAppButton)
                else -> listOf(DataItem.ScreenShot) +
                        list.map { DataItem.AppCardItem(it) } +
                        listOf(DataItem.AddAppButton)
            }
            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }

    class ButtonHolder private constructor(
        private val binding: AddAppButtonBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(viewLifecycleOwner: LifecycleOwner, viewModel: AppListViewModel) {
            binding.run {
                appListViewModel = viewModel
                lifecycleOwner = viewLifecycleOwner
                addAppCardButton.setOnClickListener { button ->
                    viewModel.saveButtonPosition(adapterPosition)
                    viewModel.alignListIndex()
                    button.findNavController()
                        .navigate(R.id.action_appListFragment_to_addAppListFragment)
                }
                executePendingBindings()
            }
        }

        companion object {
            fun from(parent: ViewGroup): ButtonHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = AddAppButtonBinding.inflate(layoutInflater, parent, false)
                return ButtonHolder(binding)
            }
        }
    }

    class ScreenShotHolder private constructor(
        val binding: ListItemScreenShotBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            viewModel: AppListViewModel,
            fragment: AppListFragment,
            viewLifecycleOwner: LifecycleOwner
        ) {
            binding.run {
                this.appListViewModel = viewModel
                lifecycleOwner = viewLifecycleOwner
                screenShotView.apply{
                    layoutManager = LinearLayoutManager(context,HORIZONTAL,false)
                    adapter = ScreenShotAdapter(viewLifecycleOwner,fragment,viewModel).apply {
                        viewModel.screenShotItemList.observe(viewLifecycleOwner,{ uriList ->
                            uriList?.let {
                                submitImageItemList(it)
                            }
                        })
                    }
                }
                imageButton.setOnClickListener { view: View ->
                    viewModel.changeEditability()
                    val inputMethodManager: InputMethodManager =
                        view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    if (viewModel.descriptionEditable.value == true) {
                        Handler(Looper.getMainLooper()).postDelayed({
                            screenShotDescription.requestFocus()
                            inputMethodManager.showSoftInput(screenShotDescription, 0)
                        }, 50)
                    }else{
                        inputMethodManager.hideSoftInputFromWindow(view.windowToken,0)
                    }
                }

                executePendingBindings()
            }
        }

        companion object {
            fun from(parent: ViewGroup): ScreenShotHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemScreenShotBinding.inflate(layoutInflater, parent, false)
                return ScreenShotHolder(binding)
            }
        }
    }

    class ViewHolder private constructor(
        val binding: ListItemAppBinding,
        private val pm: PackageManager
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AppCard, viewLifecycleOwner: LifecycleOwner, viewModel: AppListViewModel) {
            binding.run {
                lifecycleOwner = viewLifecycleOwner
                appCard = item
                this.appListViewModel = viewModel
                val appInfo: ApplicationInfo? =
                    try {
                        pm.getApplicationInfo(item.packageName, MATCH_UNINSTALLED_PACKAGES)
                    } catch (e: Exception) {
                        null
                    }
                appImage.apply {
                    setImageDrawable(
                        appInfo?.loadIcon(pm) ?: ResourcesCompat.getDrawable(
                            context.resources,
                            R.mipmap.ic_launcher,
                            null
                        )
                    )
                }
                imageButton.setOnClickListener { view: View ->
                    viewModel.changeEditability(item)
                    val inputMethodManager: InputMethodManager =
                        view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    if (viewModel.reviewEditableList[item.originalIndex].value == true) {
                        Handler(Looper.getMainLooper()).postDelayed({
                        appReviewText.requestFocus()
                        inputMethodManager.showSoftInput(appReviewText, 0)
                        }, 50)
                    }else{
                        inputMethodManager.hideSoftInputFromWindow(view.windowToken,0)
                    }
                }
                appName.text = appInfo?.loadLabel(pm)?.toString() ?: ERROR_MESSAGE_OF_APP_NAME
                executePendingBindings()
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemAppBinding.inflate(layoutInflater, parent, false)
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

    object AddAppButton : DataItem() {
        override val id = Long.MAX_VALUE
    }

    object ScreenShot : DataItem() {
        override val id = Long.MIN_VALUE
    }

    abstract val id: Long
}