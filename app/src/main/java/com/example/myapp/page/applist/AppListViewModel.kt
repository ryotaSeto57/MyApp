package com.example.myapp.page.applist

import android.net.Uri
import android.view.View
import androidx.lifecycle.*
import com.example.myapp.database.AppCard
import com.example.myapp.database.AddAppName
import com.example.myapp.database.ScreenShotItem
import com.example.myapp.page.dialog.ExceedsMaxOfScreenShotItemsDialog
import com.example.myapp.repository.AppListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.concurrent.CopyOnWriteArrayList
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

    val screenShotItemList: LiveData<MutableList<ScreenShotItem>> =
        _imageUriStringList.map{ uriStringList ->
            MutableList(uriStringList.apply{ removeAll(listOf("")) }.size){
                ScreenShotItem(id = 0,listId = listId ?: 0L,uriString = uriStringList[it],index = it)
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

    val descriptionEditable: MutableLiveData<Boolean> = savedStateHandle.getLiveData(
        "EDITABLE_DESCRIPTION",false
    )

    val reviewEditableList: List<MutableLiveData<Boolean>> = List(MAX_NUMBER_OF_APPS) { index ->
        savedStateHandle.getLiveData(
            "EDITABLE_REVIEW_OF_ORIGINAL_INDEX$index",
            false
        )
    }

    private val _listOfAppName: LiveData<MutableList<String>> =
        userAppCards.map{
            MutableList(it.size) { index ->
                it[index].packageName
            }
        }

    private val _listOfAddAppName: LiveData<MutableList<String>> =
        _listOfAppName.map { listOfAppName ->
            val allUsersAppNameList = appListRepository.getUserAppInfo()
            for (packageName in listOfAppName) {
                allUsersAppNameList.removeAll { it.packageName == packageName }
            }
            allUsersAppNameList.map { it.packageName }.toMutableList()
        }

    val addButtonVisible: LiveData<Int> = _listOfAddAppName.map {
        if (it.size == 0) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    val addAppNameList: LiveData<MutableList<AddAppName>> =
        _listOfAddAppName.map {
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
    private var uriStringStore: MutableList<String> = mutableListOf()

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
                val appCards: MutableList<AppCard> = getAppCardsFromDatabase(listId)
                val appCardsStore :CopyOnWriteArrayList<AppCard> = CopyOnWriteArrayList(appCards)
                    for (i in appCardsStore) {
                        if (savedStateHandle.contains("NON_DISPLAYED_CARD_OF_ID_${i.id}")) {
                            appCardsStore.remove(i)
                        }
                    }
                _userAppCards.value = appCardsStore.toMutableList()
                if (underEdit != true) {
                    for (appCard in appCards) {
                        savedStateHandle
                            .set("REVIEW_OF_ORIGINAL_INDEX${appCard.originalIndex}", appCard.review)
                    }
                    val screenShotItems = getScreenShotItems(listId).sortedBy { it.index }
                    for ((index,i) in screenShotItems.withIndex()){
                        savedStateHandle.set("uriString$index",i.uriString)
                    }
                    _imageUriStringList.value = screenShotItems.map { it.uriString }.toMutableList()
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

    private suspend fun getScreenShotItems(listId: Long):MutableList<ScreenShotItem>{
        return withContext(Dispatchers.IO){
            return@withContext appListRepository.getScreenShotItems(listId)
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
        if (screenShotItemList.value !=null) {
            appListRepository.deleteScreenShotItem(listId!!)
            for (i in screenShotItemList.value!!) {
                appListRepository.saveScreenShotItem(i.uriString,i.index,listId!!)
            }
        }
    }

    fun removeAppDataFromList(appCardId: Long) {
        appListScope.launch {
            savedStateHandle.set("NON_DISPLAYED_CARD_OF_ID_$appCardId",true)
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

    fun changeEditability(){
        appListScope.launch {
            descriptionEditable.value = !descriptionEditable.value!!
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
//            TODO("to check if uri is of ScreenShot Image.")
//            TODO("to add alert if uriList exceeds MAX_NUMBER_OF_SCREENSHOT")
                val imageUriStringList = _imageUriStringList.value
                val uriStringList = uriList.map { it.toString() }.toMutableList()
                _imageUriStringList.value =
                    imageUriStringList?.apply {plusAssign( uriStringList)} ?: uriStringList
            if(_imageUriStringList.value != null) {
                for ((index, i) in _imageUriStringList.value!!.withIndex()) {
                    savedStateHandle.set(
                        "uriString${index}",
                        i
                    )
                }
            }
        }
    }
    fun removeScreenShotItem(screenShotItem: ScreenShotItem){
        appListScope.launch {
            uriStringStore = _imageUriStringList.value!!
            val uriStringToRemove = uriStringStore.find{it == screenShotItem.uriString}
            savedStateHandle.remove<String>("REVIEW_OF_ORIGINAL_INDEX${uriStringStore.indexOf(uriStringToRemove)}")
            uriStringStore.remove(uriStringToRemove)
            _imageUriStringList.value = uriStringStore

        }
    }
}