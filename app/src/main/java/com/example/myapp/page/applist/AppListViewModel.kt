package com.example.myapp.page.applist

import android.net.Uri
import android.view.View
import androidx.lifecycle.*
import com.example.myapp.database.AppCard
import com.example.myapp.database.AddAppName
import com.example.myapp.repository.AppListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

private const val MAX_NUMBER_OF_APPS = 500
private const val MAX_NUMBER_OF_SCREENSHOT = 10

@HiltViewModel
class AppListViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val appListRepository: AppListRepository
) : ViewModel() {

    private val appListScope = viewModelScope

    private var listId =
        savedStateHandle.get<Long>("listId")

    private val createNewList =
        savedStateHandle.get<Boolean>("createNewList")

    private val underEdit =
        savedStateHandle.get<Boolean>("underEdit")

    init {
        Timber.i("AppListViewModel created")
        Timber.i(savedStateHandle.keys().joinToString())
        setUserAppList()
    }

    private val _imageUriStringList: MutableLiveData<MutableList<String>> =
        MutableLiveData(
            MutableList(MAX_NUMBER_OF_SCREENSHOT) { index ->
                savedStateHandle.get<String>("uriString$index") ?:""
            })
    val imageUriList: LiveData<MutableList<Uri>> =
        Transformations.map(_imageUriStringList) { list ->
            MutableList(list.apply{ removeAll(listOf("")) }.size) {
                Uri.parse(list[it])
            }
        }

    private val _userAppCards = MutableLiveData<MutableList<AppCard>>()
    val userAppCards: LiveData<MutableList<AppCard>>
        get() = _userAppCards

    val userReviewList: List<MutableLiveData<String>> = List(MAX_NUMBER_OF_APPS) { index ->
        savedStateHandle.getLiveData(
            "REVIEW_OF_ORIGINAL_INDEX$index",
            ""
        )
    }

    val reviewEditableList: List<MutableLiveData<Boolean>> = List(MAX_NUMBER_OF_APPS) { index ->
        savedStateHandle.getLiveData(
            "EDITABLE_REVIEW_OF_ORIGINAL_INDEX$index",
            false
        )
    }

    private val _listOfAppName: LiveData<MutableList<String>> =
        Transformations.map(userAppCards) {
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
                AddAppName(index.toLong(), it[index])
            }
        }

    private val _topViewHolderPosition = MutableLiveData<Int>()
    val topViewHolderPosition: LiveData<Int>
        get() = _topViewHolderPosition

    private var userAppCardListStore: MutableList<AppCard> = mutableListOf()
    private var addUserAppNameListStore: MutableList<String> = mutableListOf()
    private var deleteAppCardId: MutableList<Long> = mutableListOf()

    private fun setUserAppList() {
        appListScope.launch {
            if (createNewList != false) {
                appListRepository.createNewList()
                val appCardList = appListRepository.getLatestAppCardList()
                val appCards = getAppCardsFromDatabase(appCardList!!.id)
                savedStateHandle.set("listId", appCardList.id)
                listId = appCardList.id
                _userAppCards.value = appCards
                appListRepository.plusNumberOfAppsInTotal(appCardList.id, appCards.size)
            } else {
                val listId: Long = listId ?: 0L
                val appCards = getAppCardsFromDatabase(listId)
                val uriStrings = getImageUriStringList(listId)
                _userAppCards.value = appCards
                _imageUriStringList.value = uriStrings
                for ((index,i) in uriStrings.withIndex()){
                    savedStateHandle.set("uriString$index",i)
                }
                if (underEdit != true) {
                    for (appCard in appCards) {
                        savedStateHandle
                            .set("REVIEW_OF_ORIGINAL_INDEX${appCard.originalIndex}", appCard.review)
                    }
                }
                savedStateHandle.set("underEdit", true)
            }
        }
    }

    private suspend fun getAppCardsFromDatabase(listId: Long): MutableList<AppCard> {
        return withContext(Dispatchers.IO) {
            return@withContext appListRepository.getCardList(listId).sortedBy { it.index }.toMutableList()
        }
    }

    private suspend fun getImageUriStringList(listId: Long):MutableList<String>{
        return withContext(Dispatchers.IO){
            return@withContext appListRepository.getImageUriStrings(listId)
        }
    }

    private suspend fun createAddAppCards(packageNameList: MutableList<String>) {
        withContext(Dispatchers.IO) {
            val listId = _userAppCards.value!!.last().listId
            val numberOfPastAppCardsInTotal =
                appListRepository.getNumberOfPastAppCardsInTotal(listId)
            val listAddAppCards: MutableList<AppCard> = MutableList(packageNameList.size) { index ->
                AppCard(
                    id = 0,
                    listId = listId,
                    originalIndex = numberOfPastAppCardsInTotal + index,
                    index = _userAppCards.value!!.size + index,
                    packageName = packageNameList[index]
                )
            }
            val addAppCardsFromDatabase = appListRepository.addAppCards(listAddAppCards)
            appListRepository.plusNumberOfAppsInTotal(listId, packageNameList.size)
            userAppCardListStore = _userAppCards.value!!
            userAppCardListStore.plusAssign(addAppCardsFromDatabase)
            userAppCardListStore.sortBy { it.index }
            _userAppCards.postValue(userAppCardListStore)
        }
    }

    private suspend fun saveAppCards() {
        for ((index, appCard) in _userAppCards.value!!.withIndex()) {
            appCard.index = index
            appCard.review = userReviewList[appCard.originalIndex].value ?: ""
            appListRepository.save(appCard)
        }
    }

    private suspend fun deleteAppCards() {
        for (id in deleteAppCardId) {
            appListRepository.deleteAppCard(id)
        }
    }

    private suspend fun uploadUserAppList() {
        if (_userAppCards.value!!.lastIndex < MAX_NUMBER_OF_APPS) {
            appListRepository.shareList(_userAppCards.value!!)
        }
    }

    private suspend fun saveScreenShotItem(){
        if (imageUriList.value !=null) {
            appListRepository.deleteScreenShotItem(listId!!)
            for ((index,i) in imageUriList.value!!.withIndex()) {
                appListRepository.saveScreenShotItem(i,index,listId!!)
            }
        }
    }

    fun removeAppDataFromList(appCardId: Long) {
        appListScope.launch {
            deleteAppCardId.add(appCardId)
            userAppCardListStore = _userAppCards.value!!
            userAppCardListStore.remove(userAppCardListStore.find { it.id == appCardId })
            for ((index, appCard) in userAppCardListStore.withIndex()) {
                appCard.index = index
            }
            _userAppCards.postValue(userAppCardListStore)
        }
    }

    fun alignListIndex() {
        appListScope.launch {
            for ((index, appCard) in _userAppCards.value!!.withIndex()) {
                appCard.index = index
            }
        }
    }

    fun replaceAppData(indexOfFrom: Int, indexOfTo: Int) {
        appListScope.launch {
            withContext(Dispatchers.IO) {
                if (indexOfFrom == indexOfTo + 1 || indexOfFrom == indexOfTo - 1) {
                    userAppCardListStore = _userAppCards.value!!
                    userAppCardListStore[indexOfFrom].index = userAppCardListStore[indexOfTo].index
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
                    _userAppCards.postValue(userAppCardListStore)
                }
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
        appListScope.launch {
            _topViewHolderPosition.value = adapterPosition
        }
    }

    fun changeEditability(appCard: AppCard) {
        appListScope.launch {
            reviewEditableList[appCard.originalIndex].value =
                !reviewEditableList[appCard.originalIndex].value!!
        }
    }

    fun saveAction() {
        appListScope.launch {
            saveScreenShotItem()
            saveAppCards()
            deleteAppCards()
        }
    }

    fun shareAction() {
        appListScope.launch {
            saveAppCards()
            deleteAppCards()
            uploadUserAppList()
        }
    }

    fun setImageUri(uriList: MutableList<Uri?>) {
        appListScope.launch {
//            TODO("to add alert if uriList exceeds MAX_NUMBER_OF_SCREENSHOT")
            if(uriList.size <= MAX_NUMBER_OF_SCREENSHOT) {
                _imageUriStringList.value = uriList.map{it.toString()}.toMutableList()
                for ((index,i) in uriList.withIndex()){
                    savedStateHandle.set("uriString$index",i.toString())
                }
            }
        }
    }

}