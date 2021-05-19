package com.on.smartphone.page.sharedapplist

import androidx.lifecycle.*
import com.on.smartphone.database.AppCard
import com.on.smartphone.database.ScreenShotItem
import com.on.smartphone.page.applist.AppListViewModel
import com.on.smartphone.repository.AppListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SharedAppListViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val appListRepository: AppListRepository
): ViewModel() {

    private val sharedAppListScope = viewModelScope

    private val _userAppCards = MutableLiveData<MutableList<AppCard>>()
    val userAppCards: LiveData<MutableList<AppCard>>
        get() = _userAppCards

    private val _screenShotItemList = MutableLiveData<MutableList<ScreenShotItem>>()
    val screenShotItemList: LiveData<MutableList<ScreenShotItem>>
        get() = _screenShotItemList

    private val _screenShotDescription= MutableLiveData<String>()
    val screenShotDescription: LiveData<String>
        get() = _screenShotDescription

    private val _listUrl = MutableLiveData<String>()
    val listUrl: LiveData<String>
        get() = _listUrl

    private var listId =
        savedStateHandle.get<Long>(AppListViewModel.LIST_ID)

    init {
        sharedAppListScope.launch {
            val listId: Long = listId ?: 0L
            val appCards: MutableList<AppCard> = getAppCardsFromDatabase(listId)
            _userAppCards.value = appCards
            val appCardList = appListRepository.getAppCardList(listId)
            _listUrl.value = appCardList?.listUrl
            _screenShotDescription.value = appCardList?.screenShotDescription
            val screenShotItems = getScreenShotItems(listId).sortedBy { it.index }.toMutableList()
            _screenShotItemList.value = screenShotItems
            Timber.i("SharedAppListViewModel is created.")
        }
    }

    private suspend fun getAppCardsFromDatabase(listId: Long): MutableList<AppCard> {
        return withContext(Dispatchers.IO) {
            return@withContext appListRepository.getCardList(listId).sortedBy { it.index }
                .toMutableList()
        }
    }

    private suspend fun getScreenShotItems(listId: Long): MutableList<ScreenShotItem> {
        return withContext(Dispatchers.IO) {
            return@withContext appListRepository.getScreenShotItems(listId)
        }
    }

}