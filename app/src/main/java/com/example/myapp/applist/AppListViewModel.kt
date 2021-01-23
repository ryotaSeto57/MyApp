package com.example.myapp.applist

import android.app.Application
import android.util.Log
import android.view.View
import androidx.lifecycle.*
import com.example.myapp.database.AppCard
import com.example.myapp.database.AppDatabaseDao
import com.example.myapp.database.AddAppName
import com.example.myapp.repository.AppListRepository
import kotlinx.coroutines.*

class AppListViewModel(
    private val database: AppDatabaseDao,
    private val myapplication: Application,
    private val createNewList: Boolean) : ViewModel() {

    private val repository: AppListRepository
    private val appListScope = viewModelScope
    private val pm = myapplication.packageManager

    init {
        Log.i("AppListViewModel", "AppListViewModel created")
        repository = AppListRepository(database,pm)
        setUserAppList()
    }

    private val _userAppReviewList = MutableLiveData<MutableList<AppCard>>()
    val userAppReviewList: LiveData<MutableList<AppCard>>
        get() = _userAppReviewList

    private val _listOfAppName: LiveData<MutableList<String>> =
        Transformations.map(userAppReviewList) {
            MutableList(it.size) { index ->
                it[index].packageName
            }
        }

    private val _listOfAddAppName: LiveData<MutableList<String>> =
        Transformations.map(_listOfAppName) { listOfAppName ->
            val allUsersAppNameList = repository.getUserAppInfo()
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

    val addAppNameList :LiveData<MutableList<AddAppName>> =
        Transformations.map(_listOfAddAppName){
            MutableList(it.size){index ->
                AddAppName(it[index])
            }
        }

    private val _topViewHolderPosition = MutableLiveData<Int>()
    val topViewHolderPosition :LiveData<Int>
        get() = _topViewHolderPosition

    private var userAppCardListStore: MutableList<AppCard> = mutableListOf()
    private var addUserAppNameListStore: MutableList<String> = mutableListOf()



    private fun setUserAppList() {
        appListScope.launch {
            if (createNewList) {
                repository.createNewList()
            }
            _userAppReviewList.value = getAppCardsFromDatabase()
        }
    }

    private suspend fun getAppCardsFromDatabase(): MutableList<AppCard> {
        return withContext(Dispatchers.IO) {
            val listId = repository.getLatestReviewList()!!.id
            val appCards = repository.getList(listId).sortedBy { it.index }.toMutableList()
            appCards
        }
    }

    private suspend fun createAddAppCards(packageNameList: MutableList<String>) {
        withContext(Dispatchers.IO) {
            val listAddAppCards: MutableList<AppCard> = MutableList(packageNameList.size) { index ->
                AppCard(
                    id = 0,
                    listId = _userAppReviewList.value!!.last().listId,
                    index = _userAppReviewList.value!!.last().index + index + 1,
                    packageName = packageNameList[index]
                )
            }
            for (appCard in listAddAppCards) {
                repository.add(appCard)
            }
            userAppCardListStore = _userAppReviewList.value!!
            userAppCardListStore.plusAssign(listAddAppCards)
            _userAppReviewList.postValue(userAppCardListStore)
        }
    }

    private suspend fun saveAppCards() {
        for ((index, appCard) in _userAppReviewList.value!!.withIndex()) {
            appCard.index = index
            repository.save(appCard)
        }
    }

    fun uploadUserAppList() {
        appListScope.launch {
            if (_userAppReviewList.value!!.lastIndex < 300) {
                saveAppCards()
                repository.shareList(_userAppReviewList.value!!)
            }
        }
    }

    fun removeAppDataFromList(index: Int, appCardId: Long) {
        appListScope.launch {
            repository.deleteAppCard(appCardId)
            userAppCardListStore = _userAppReviewList.value!!
            userAppCardListStore.removeAt(index)
            for (i in index until _userAppReviewList.value!!.size) {
                userAppCardListStore[i].index = i
            }
            _userAppReviewList.value = userAppCardListStore
        }
    }

    fun replaceAppData(indexOfFrom: Int, indexOfTo: Int) {
        appListScope.launch {
            withContext(Dispatchers.IO) {
                userAppCardListStore = _userAppReviewList.value!!
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
                _userAppReviewList.postValue(userAppCardListStore)
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

    fun saveButtonPosition(adapterPosition:Int){
            _topViewHolderPosition.value = adapterPosition
    }

}