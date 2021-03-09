package com.example.myapp.page.title

import android.view.LayoutInflater
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TitleListAdapter(
    private val viewLifecycleOwner: LifecycleOwner,
    private val titleViewModel: TitleViewModel
) : ListAdapter<TitleDataItem, RecyclerView.ViewHolder>(TitleDataDiffCallback()) {

    private val adapterScope = CoroutineScope(Dispatchers.Default)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ViewHolder -> {
                val item = getItem(position) as TitleDataItem.AppCardListItem
                return holder.bind(viewLifecycleOwner, titleViewModel, item.appCardList)
            }
        }
    }

    fun submitAppCardList(list: MutableList<AppCardList>) {
        adapterScope.launch {
            val items = when (list) {
                null -> listOf(TitleDataItem.AppCardListItem(AppCardList(id = 1000L)))
                else -> list.map { TitleDataItem.AppCardListItem(it) }
            }
            withContext(Dispatchers.Main) {
                submitList(items)
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
                                submitList(list.filter { it.listId ==item.id }.sortedBy { it.index })
                            }
                        })
                    }
                    layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
                }

                titleListContainer.setOnClickListener { view ->
                    val action =
                        TitleFragmentDirections.actionTitleFragmentToAppListFragment(item.id)
                    view.findNavController().navigate(action)
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

    abstract val id: Long
}