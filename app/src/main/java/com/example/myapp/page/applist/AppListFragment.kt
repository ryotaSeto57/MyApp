package com.example.myapp.page.applist

import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.navigation.navGraphViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.example.myapp.R
import com.example.myapp.databinding.FragmentAppListBinding
import com.example.myapp.page.dialog.ExceedsMaxOfScreenShotItemsDialog
import com.leinardi.android.speeddial.SpeedDialActionItem
import com.leinardi.android.speeddial.SpeedDialView
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Singleton

/**
 * A simple [Fragment] subclass.
 */
@AndroidEntryPoint
@Singleton
class AppListFragment : Fragment() {
    private lateinit var binding: FragmentAppListBinding
    private lateinit var getContent: ActivityResultLauncher<String>

    private val viewModel: AppListViewModel
            by navGraphViewModels(R.id.app_navigation) { defaultViewModelProviderFactory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val adapter = AppListAdapter(
            viewLifecycleOwner,
            viewModel,
            this
        )

        binding = DataBindingUtil.inflate<FragmentAppListBinding>(
            inflater,
            R.layout.fragment_app_list,
            container,
            false
        ).apply {
            lifecycleOwner = viewLifecycleOwner
            appList.adapter = adapter.apply {
                registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                        appList.scrollToPosition(
                            viewModel.topViewHolderPosition.value
                                ?: 0
                        )
                    }
                })
            }
            itemTouchHelper.attachToRecyclerView(appList)
            speedDial.apply {
                addActionItem(
                    SpeedDialActionItem.Builder(
                        R.id.save_action, R.drawable.ic_baseline_save_18
                    ).create()
                )
                addActionItem(
                    SpeedDialActionItem.Builder(
                        R.id.share_action, R.drawable.ic_baseline_share_24
                    ).create()
                )
                setOnActionSelectedListener(SpeedDialView.OnActionSelectedListener { actionItem ->
                    when (actionItem.id) {
                        R.id.save_action -> {
                            viewModel.saveAction()
                            close()
                            return@OnActionSelectedListener true
                        }
                        R.id.share_action -> {
                            viewModel.shareAction()
                            close()
                            return@OnActionSelectedListener true
                        }
                    }
                    false
                })
            }
        }

        getContent = registerForActivityResult(MultipleImageContract(activity)){ uriList ->
            uriList?.let {
                val totalNumberOfUris = (viewModel.screenShotItemList.value?.size ?:0)+ uriList.size
                if(totalNumberOfUris <= 10) {
                    viewModel.setImageUri(it)
                }else{
                    alertMessage()
                    viewModel.setImageUri(it.dropLast(totalNumberOfUris - 10).toMutableList())
                }
            }
        }

        viewModel.userAppCards.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitReviewList(it)
            }
        })

        viewModel.isUploadCompleted.observe(viewLifecycleOwner, {
            if (it == true){
                shareUrl(viewModel.getUserListUrl())
            }
        })
        return binding.root
    }

    override fun onDestroy() {
        Timber.i("AppListFragment is destroyed.")
        super.onDestroy()
    }

    private fun alertMessage(){
        val exceedsMaxOfScreenShotItemsDialog = ExceedsMaxOfScreenShotItemsDialog()
        exceedsMaxOfScreenShotItemsDialog.show(childFragmentManager,"alert")
    }

    fun selectImage() {
        getContent.launch("image/*")
    }

    private fun shareUrl(listUrl: String){
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "https://my-app-6154a.web.app/$listUrl")
            type="text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    private val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            return when (viewHolder) {
                is AppListAdapter.ViewHolder -> makeMovementFlags(UP or DOWN, LEFT)
                else -> ACTION_STATE_IDLE
            }
        }

        override fun onMoved(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            fromPos: Int,
            target: RecyclerView.ViewHolder,
            toPos: Int,
            x: Int,
            y: Int
        ) {
            super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y)
            val fromPosition = viewHolder.adapterPosition
            val toPosition = target.adapterPosition
            val fromUserAppCard = fromPosition - 1
            val toUserAppCard = toPosition - 1
            viewModel.replaceAppData(fromUserAppCard, toUserAppCard)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return when (target) {
                is AppListAdapter.ViewHolder -> {
                    true
                }
                else -> false
            }
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val appCardOriginalIndex = (viewHolder as AppListAdapter.ViewHolder).binding.appCard!!.originalIndex
            viewModel.removeAppCardFromList(appCardOriginalIndex)
        }
    })
}

class MultipleImageContract(private val activity: FragmentActivity?)
    : ActivityResultContract<String, MutableList<Uri?>?>() {
    override fun createIntent(context: Context, input: String?): Intent {
        return Intent(Intent.ACTION_OPEN_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .setType(input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): MutableList<Uri?>? {
        if (intent == null || resultCode != Activity.RESULT_OK) return null
        val fileUris = mutableListOf<Uri?>()
        if (intent.data != null) {
            activity?.contentResolver?.takePersistableUriPermission(
                intent.data!!,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            fileUris.add(intent.data)
        } else {
            val clipData: ClipData = intent.clipData!!
            if (clipData.itemCount <= 10) {
                for (i in 0 until clipData.itemCount) {
                    activity?.contentResolver?.takePersistableUriPermission(
                        clipData.getItemAt(i).uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    fileUris.add(clipData.getItemAt(i).uri)
                }
            } else {
                for (i in 0 until 10) {
                    activity?.contentResolver?.takePersistableUriPermission(
                        clipData.getItemAt(i).uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                    fileUris.add(clipData.getItemAt(i).uri)
                }
            }
        }
        return fileUris
    }
}