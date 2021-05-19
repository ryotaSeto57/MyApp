package com.on.smartphone.page.applist

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.on.smartphone.database.ScreenShotItem
import com.on.smartphone.databinding.ListItemScreenShotAddButtonBinding
import com.on.smartphone.databinding.ListItemScreenShotImageBinding
import com.on.smartphone.databinding.ListItemScreenShotStartAddingButtonBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ClassCastException

private const val ITEM_VIEW_TYPE_SCREEN_SHOT = 0
private const val ITEM_VIEW_TYPE_ADD_BUTTON = 1
private const val ITEM_VIEW_TYPE_START_ADDING_BUTTON = 2

class ScreenShotAdapter(
    private val viewLifecycleOwner: LifecycleOwner,
    private val fragment: AppListFragment,
    private val viewModel: AppListViewModel
) : ListAdapter<ImageItem, RecyclerView.ViewHolder>(ImageDiffCallBack()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder) {
            is ScreenShotImageHolder -> {
                val item = getItem(position) as ImageItem.ScreenShotImageItem
                holder.bind(item.screenShotItem, viewLifecycleOwner,viewModel)
            }
            is AddButtonHolder ->{
                holder.bind(fragment)
            }
            is StartAddingButtonHolder -> {
                holder.bind(fragment)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return  when(viewType) {
            ITEM_VIEW_TYPE_SCREEN_SHOT -> ScreenShotImageHolder.from(parent)
            ITEM_VIEW_TYPE_ADD_BUTTON -> AddButtonHolder.from(parent)
            ITEM_VIEW_TYPE_START_ADDING_BUTTON -> StartAddingButtonHolder.from(parent)
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)){
            is ImageItem.ScreenShotImageItem -> ITEM_VIEW_TYPE_SCREEN_SHOT
            is ImageItem.AddImageButton -> ITEM_VIEW_TYPE_ADD_BUTTON
            is ImageItem.StartAddImageButton -> ITEM_VIEW_TYPE_START_ADDING_BUTTON
        }
    }

    fun submitImageItemList(list: MutableList<ScreenShotItem>?){
        adapterScope.launch {
            val items = when(list?.size ?:0){
                0 -> listOf(ImageItem.StartAddImageButton)
                else -> list!!.map{ImageItem.ScreenShotImageItem(it)} +
                        listOf(ImageItem.AddImageButton)
            }
            withContext(Dispatchers.Main){
                submitList(items)
            }
        }
    }

    class StartAddingButtonHolder private constructor(
        private val binding: ListItemScreenShotStartAddingButtonBinding
    ):RecyclerView.ViewHolder(binding.root){

        fun bind(fragment: AppListFragment){
            binding.run {
                startAddScreenShotButton.setOnClickListener{
                    fragment.selectImage()
                }
            }
        }

        companion object{
            fun from(parent: ViewGroup): StartAddingButtonHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemScreenShotStartAddingButtonBinding.inflate(
                    layoutInflater, parent, false
                )
                return StartAddingButtonHolder(binding)
            }
        }
    }

    class AddButtonHolder private constructor(
        private val binding: ListItemScreenShotAddButtonBinding,
    ): RecyclerView.ViewHolder(binding.root){

        fun bind(fragment: AppListFragment){
            binding.run {
                screenShotButton.setOnClickListener {
                    fragment.selectImage()
                }
            }
        }

        companion object{
            fun from(parent: ViewGroup): AddButtonHolder{
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemScreenShotAddButtonBinding.inflate(
                    layoutInflater, parent, false
                )
                return AddButtonHolder(binding)
            }
        }
    }

    class ScreenShotImageHolder private constructor(
        private val binding: ListItemScreenShotImageBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ScreenShotItem,viewLifecycleOwner: LifecycleOwner,viewModel: AppListViewModel) {
            binding.run {
                screenShotItem = item
                screenShotImage.setImageURI(item.uriString.toUri())
                appListViewModel = viewModel
                lifecycleOwner = viewLifecycleOwner
                executePendingBindings()
            }
        }

        companion object {
            fun from(parent: ViewGroup): ScreenShotImageHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemScreenShotImageBinding.inflate(
                    layoutInflater, parent, false
                )
                return ScreenShotImageHolder(binding,parent.context)
            }
        }
    }
}

class ImageDiffCallBack() : DiffUtil.ItemCallback<ImageItem>() {
    override fun areContentsTheSame(oldItem: ImageItem, newItem: ImageItem): Boolean {
        if (oldItem == ImageItem.AddImageButton || newItem == ImageItem.AddImageButton){
            return false
        }
        return (oldItem as ImageItem.ScreenShotImageItem).screenShotItem.uriString ==
                (newItem as ImageItem.ScreenShotImageItem).screenShotItem.uriString
    }

    override fun areItemsTheSame(oldItem: ImageItem, newItem: ImageItem): Boolean {
        return oldItem == newItem
    }
}

sealed class ImageItem{
    data class ScreenShotImageItem(val screenShotItem: ScreenShotItem): ImageItem(){
        override val id = screenShotItem.id
    }

    object AddImageButton: ImageItem(){
        override val id = Long.MAX_VALUE
    }

    object StartAddImageButton: ImageItem(){
        override val id = Long.MIN_VALUE
    }

    abstract val id: Long
}