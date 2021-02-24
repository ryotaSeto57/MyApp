package com.example.myapp.applist

import android.view.View
import androidx.lifecycle.*
import com.example.myapp.database.AppCard
import com.example.myapp.database.AddAppName
import com.example.myapp.database.AppCardList
import com.example.myapp.repository.AppListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AppListViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val appListRepository: AppListRepository
) : ViewModel() {

    private val appListScope = viewModelScope

    private val createNewList =
        savedStateHandle.getLiveData<Boolean>("createNewList", true)

    init {
        Timber.i("AppListViewModel created")
        setUserAppList()
    }

    private val _userAppCardList = MutableLiveData<MutableList<AppCard>>()
    val userAppCardList: LiveData<MutableList<AppCard>>
        get() = _userAppCardList

    val userReviewList: List<MutableLiveData<String>> = List(500){
        savedStateHandle.getLiveData<String>("REVIEW_OF_ORIGINAL_INDEX$it",
            "" )}

    private val _listOfAppName: LiveData<MutableList<String>> =
        Transformations.map(userAppCardList) {
            MutableList(it.size) { index ->
                it[index].packageName
            }
        }

    private val _listOfAddAppName: LiveData<MutableList<String>> =
        Transformations.map(_listOfAppName) { listOfAppName ->
            val allUsersAppNameList = appListRepository.getUserAppInfo()
            for (packageName in listOfAppName) {
                allUsersAppNameList.removeAll { it.packageName == packageName }
            }
            allUsersAppNameList.map { it.packageName }.toMutableList()
        }

    val addButtonVisible: LiveData<Int> = Transformations.map(_listOfAddAppName) {
        if (it.size == 0) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    val addAppNameList: LiveData<MutableList<AddAppName>> =
        Transformations.map(_listOfAddAppName) {
            MutableList(it.size) { index ->
                AddAppName(it[index])
            }
        }

    private val _topViewHolderPosition = MutableLiveData<Int>()
    val topViewHolderPosition: LiveData<Int>
        get() = _topViewHolderPosition

    private var userAppCardListStore: MutableList<AppCard> = mutableListOf()
    private var addUserAppNameListStore: MutableList<String> = mutableListOf()


    private fun setUserAppList() {
        appListScope.launch {
            if (createNewList.value!!) {
                appListRepository.createNewList()
                createNewList.postValue(false)
            }
            val appCardList = getAppCardsFromDatabase()
            _userAppCardList.postValue(appCardList)
        }
    }

    private suspend fun getAppCardsFromDatabase(): MutableList<AppCard> {
        return withContext(Dispatchers.IO) {
            val list: AppCardList? = appListRepository.getAppCardList()
            val listId: Long = list?.id ?:0L
            val appCards = appListRepository.getList(listId).sortedBy { it.index }.toMutableList()
            appCards
        }
    }

    private suspend fun createAddAppCards(packageNameList: MutableList<String>) {
        withContext(Dispatchers.IO) {
            val listAddAppCards: MutableList<AppCard> = MutableList(packageNameList.size) { index ->
                AppCard(
                    id = 0,
                    listId = _userAppCardList.value!!.last().listId,
                    originalIndex = userAppCardList.value!!.size + index,
                    index = _userAppCardList.value!!.last().index + index + 1,
                    packageName = packageNameList[index]
                )
            }
            for (appCard in listAddAppCards) {
                appListRepository.add(appCard)
            }
            userAppCardListStore = _userAppCardList.value!!
            userAppCardListStore.plusAssign(listAddAppCards)
            _userAppCardList.postValue(userAppCardListStore)
        }
    }

    private suspend fun saveAppCards() {
        for ((index, appCard) in _userAppCardList.value!!.withIndex()) {
            appCard.index = index
            appCard.review = userReviewList[appCard.originalIndex].value!!
            this.appListRepository.save(appCard)
        }
    }

    fun uploadUserAppList() {
        appListScope.launch {
            if (_userAppCardList.value!!.lastIndex < 300) {
                saveAppCards()
                appListRepository.shareList(_userAppCardList.value!!)
            }
        }
    }

    fun removeAppDataFromList(index: Int, appCardId: Long) {
        appListScope.launch {
            appListRepository.deleteAppCard(appCardId)
            userAppCardListStore = _userAppCardList.value!!
            userAppCardListStore.removeAt(index)
            for (i in index until _userAppCardList.value!!.size) {
                userAppCardListStore[i].index = i
            }
            _userAppCardList.value = userAppCardListStore
        }
    }

    fun replaceAppData(indexOfFrom: Int, indexOfTo: Int) {
        appListScope.launch {
            withContext(Dispatchers.IO) {
                userAppCardListStore = _userAppCardList.value!!
                userAppCardListStore[indexOfFrom].index = indexOfTo
                if (indexOfFrom < indexOfTo) {
                    for (i in indexOfFrom until indexOfTo) {
                        userAppCardListStore[i + 1].index -= 1
                    }
                } else if (indexOfFrom > indexOfTo) {
                    for (i in indexOfTo until indexOfFrom) {
                        userAppCardListStore[i].index += 1
                    }
                }
                userAppCardListStore.sortBy { it.index }
                _userAppCardList.postValue(userAppCardListStore)
            }
        }
    }

    fun registerAddAppName() {
        appListScope.launch {
            addUserAppNameListStore =
                addAppNameList.value!!.filter { it.addOrNot.value!! }
                    .map { it.packageName }.toMutableList()
            createAddAppCards(addUserAppNameListStore)
        }
    }

    fun saveButtonPosition(adapterPosition: Int) {
        _topViewHolderPosition.value = adapterPosition
    }

}