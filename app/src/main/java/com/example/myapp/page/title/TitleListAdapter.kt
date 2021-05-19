package com.example.myapp.page.title

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import com.example.myapp.database.AppCardList
import com.example.myapp.databinding.TitleItemCardBinding
import com.example.myapp.databinding.TitleItemCreateButtonBinding
import com.example.myapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

private const val ITEM_VIEW_TYPE_ITEM = 0
private const val ITEM_VIEW_TYPE_CREATE_BUTTON = 1
class TitleListAdapter(
    private val viewLifecycleOwner: LifecycleOwner,
    private val titleViewModel: TitleViewModel
) : ListAdapter<TitleDataItem, RecyclerView.ViewHolder>(TitleDataDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_VIEW_TYPE_ITEM -> {
                ViewHolder.from(parent)
            }
            ITEM_VIEW_TYPE_CREATE_BUTTON -> {
                ButtonHolder.from(parent)
            }
            else -> throw ClassCastException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val item = getItem(position) as TitleDataItem.AppCardListItem
                holder.bind(viewLifecycleOwner, titleViewModel, item.appCardList)
            }
            is ButtonHolder ->{
                holder.bind(viewLifecycleOwner)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return  when(getItem(position)){
            is TitleDataItem.AppCardListItem -> ITEM_VIEW_TYPE_ITEM
            is TitleDataItem.CreateButton -> ITEM_VIEW_TYPE_CREATE_BUTTON
        }
    }

    fun submitAppCardList(list: MutableList<AppCardList>?) {
        adapterScope.launch {
            val items = when (list?.size ?:0) {
                0 -> listOf(TitleDataItem.CreateButton)
                1,2,3 -> list!!.map { TitleDataItem.AppCardListItem(it) } + listOf(TitleDataItem.CreateButton)
                else -> list!!.map { TitleDataItem.AppCardListItem(it)  }
            }
            withContext(Dispatchers.Main) {
                submitList(items)
            }
        }
    }
    class ButtonHolder private constructor(
        private val binding: TitleItemCreateButtonBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(viewLifecycleOwner: LifecycleOwner) {
            binding.run {
                lifecycleOwner = viewLifecycleOwner
                createListButton.setOnClickListener {view: View ->
                    val action = TitleFragmentDirections.actionTitleFragmentToAppListFragment(
                        createNewList =true,underEdit=false
                    )
                    view.findNavController().navigate(action)
                }
                executePendingBindings()
            }
        }

        companion object {
            fun from(parent: ViewGroup): ButtonHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = TitleItemCreateButtonBinding.inflate(layoutInflater, parent, false)
                return ButtonHolder(binding)
            }
        }
    }

    class ViewHolder private constructor(private val binding: TitleItemCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            viewLifecycleOwner: LifecycleOwner,
            titleViewModel: TitleViewModel,
            item: AppCardList
        ) {
            binding.run {
                appCardList = item
                appImageList.apply {
                    adapter = TitleAppImageAdapter(titleViewModel, viewLifecycleOwner).apply {
                        titleViewModel.userPastAppCards.observe(viewLifecycleOwner, { nullableList ->
                            nullableList?.let { list ->
                                submitList(list.filter { it.listId == item.id }.sortedBy { it.index })
                            }
                        })
                    }
                    layoutManager =
                        LinearLayoutManager(context, HORIZONTAL, false)
                }
                item.sharedDate?.let {
                    sharedDate.text = SimpleDateFormat("yyyy-MM-dd", Locale.JAPAN).format(it)
                    check.visibility = View.VISIBLE
                    titleListContainer.apply {
                        if(Build.VERSION.SDK_INT < 23){
                            @Suppress("DEPRECATION")
                            setBackgroundColor(this.resources.getColor(R.color.colorAccent))
                        }else {
                            setBackgroundColor(this.context.getColor(R.color.colorAccent))
                        }
                    }
                }
                lifecycleOwner = viewLifecycleOwner
                executePendingBindings()
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = TitleItemCardBinding.inflate(
                    layoutInflater, parent, false
                )
                return ViewHolder(binding)
            }
        }
    }
}

private class TitleDataDiffCallback : DiffUtil.ItemCallback<TitleDataItem>() {
    override fun areItemsTheSame(oldItem: TitleDataItem, newItem: TitleDataItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: TitleDataItem, newItem: TitleDataItem): Boolean {
        return oldItem == newItem
    }
}

sealed class TitleDataItem {
    data class AppCardListItem(val appCardList: AppCardList) : TitleDataItem() {
        override val id = appCardList.id
    }
    object CreateButton: TitleDataItem(){
        override val id: Long = Long.MAX_VALUE
    }
    abstract val id: Long
}