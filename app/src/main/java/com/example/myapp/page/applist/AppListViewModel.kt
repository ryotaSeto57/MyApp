package com.example.myapp.page.applist

import android.net.Uri
import android.view.View
import androidx.lifecycle.*
import com.example.myapp.database.*
import com.example.myapp.repository.AppListRepository
import com.example.myapp.repository.CreateUserListId
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject

private const val MAX_NUMBER_OF_APPS = 500
private const val MAX_NUMBER_OF_SCREENSHOT = 10

@HiltViewModel
class AppListViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val appListRepository: AppListRepository
) : ViewModel() {

    companion object{
        private const val URI_STRING = "URI_STRING_"
        private const val MAX_OF_ORIGINAL_INDEX = "MAX_OF_ORIGINAL_INDEX"
        private const val ADDED_APP_CARD_PACKAGE_NAME_OF_ORIGINAL_INDEX =
            "ADDED_APP_CARD_PACKAGE_NAME_OF_ORIGINAL_INDEX_"
        private const val NON_DISPLAYED_CARD_OF = "NON_DISPLAYED_CARD_OF_"
        private const val REVIEW_OF_ORIGINAL_INDEX = "REVIEW_OF_ORIGINAL_INDEX_"
        private const val EDITABLE_REVIEW_OF_ORIGINAL_INDEX = "EDITABLE_REVIEW_OF_ORIGINAL_INDEX_"
        private const val SCREEN_SHOT_DESCRIPTION = "SCREEN_SHOT_DESCRIPTION"
        private const val EDITABLE_DESCRIPTION = "EDITABLE_DESCRIPTION"
        const val LIST_ID = "listId"
        const val CREATE_NEW_LIST = "createNewList"
        const val UNDER_EDIT = "underEdit"
    }

    private val appListScope = viewModelScope

    private var listId =
        savedStateHandle.get<Long>(LIST_ID)

    private val createNewList =
        savedStateHandle.get<Boolean>(CREATE_NEW_LIST)

    private val underEdit =
        savedStateHandle.get<Boolean>(UNDER_EDIT)

    init {
        Timber.i("AppListViewModel is created")
        Timber.i(savedStateHandle.keys().joinToString())
        setUserAppList()
    }

    private val _imageUriStringList: MutableLiveData<MutableList<String>> =
        MutableLiveData(
            MutableList(MAX_NUMBER_OF_SCREENSHOT) { index ->
                savedStateHandle.get<String>(URI_STRING + index) ?: ""
            })

    val screenShotItemList: LiveData<MutableList<ScreenShotItem>> =
        _imageUriStringList.map { uriStringList ->
            MutableList(uriStringList.apply { removeAll(listOf("")) }.size) {
                ScreenShotItem(
                    id = 0,
                    listId = listId ?: 0L,
                    uriString = uriStringList[it],
                    index = it
                )
            }
        }

    private val _userAppCards = MutableLiveData<MutableList<AppCard>>()
    val userAppCards: LiveData<MutableList<AppCard>>
        get() = _userAppCards

    val userReviewList: List<MutableLiveData<String>> = List(MAX_NUMBER_OF_APPS) { index ->
        savedStateHandle.getLiveData(
            REVIEW_OF_ORIGINAL_INDEX+index,
            ""
        )
    }

    val descriptionEditable: MutableLiveData<Boolean> = savedStateHandle.getLiveData(
        EDITABLE_DESCRIPTION, false
    )

    val screenShotDescription: MutableLiveData<String> = savedStateHandle.getLiveData(
        SCREEN_SHOT_DESCRIPTION, ""
    )

    val reviewEditableList: List<MutableLiveData<Boolean>> = List(MAX_NUMBER_OF_APPS) { index ->
        savedStateHandle.getLiveData(
            EDITABLE_REVIEW_OF_ORIGINAL_INDEX + index,
            false
        )
    }

    private val _listOfAppName: LiveData<MutableList<String>> =
        userAppCards.map {
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

    private val _isUploading: MutableLiveData<Boolean?> = MutableLiveData(null)
    val isUploading:LiveData<Boolean?>
        get() = _isUploading



    private var userAppCardListStore: MutableList<AppCard> = mutableListOf()
    private var addUserAppNameListStore: MutableList<String> = mutableListOf()
    private var uriStringStore: MutableList<String> = mutableListOf()

    private var appCardsToDelete: MutableList<AppCard> = mutableListOf()

    private var numberOfAppCardsFromDatabase: Int = 0

    private var userListId: String = ""
    private var userListUrl: String = ""

    private fun setUserAppList() {
        appListScope.launch {
            if (createNewList != false) {
                appListRepository.createNewList()
                val appCardList = appListRepository.getLatestAppCardList()
                val appCards = getAppCardsFromDatabase(appCardList!!.id)
                savedStateHandle.set(LIST_ID, appCardList.id)
                savedStateHandle.set(CREATE_NEW_LIST, false)
                savedStateHandle.set(UNDER_EDIT, true)
                savedStateHandle.set(MAX_OF_ORIGINAL_INDEX,appCards.size-1)
                listId = appCardList.id
                numberOfAppCardsFromDatabase = appCards.size
                _userAppCards.value = appCards
            } else {
                val listId: Long = listId ?: 0L
                val appCards: MutableList<AppCard> = getAppCardsFromDatabase(listId)
                numberOfAppCardsFromDatabase = appCards.size
                val appCardsStore: CopyOnWriteArrayList<AppCard> = CopyOnWriteArrayList(appCards)
                if(underEdit == true) {
                    for (i in appCardsStore) {
                        if (savedStateHandle.contains(NON_DISPLAYED_CARD_OF+i.packageName)) {
                            appCardsToDelete.add(i)
                            appCardsStore.remove(i)
                        }
                    }
                    for(i in numberOfAppCardsFromDatabase .. savedStateHandle.get<Int>(
                        MAX_OF_ORIGINAL_INDEX
                    )!!) {
                        if (savedStateHandle.contains(
                                ADDED_APP_CARD_PACKAGE_NAME_OF_ORIGINAL_INDEX + i
                            )) {
                            appCardsStore.add(
                                AppCard(
                                    id = 0,
                                    listId = listId,
                                    originalIndex = i,
                                    index = i,
                                    packageName = savedStateHandle.get<String>(
                                        ADDED_APP_CARD_PACKAGE_NAME_OF_ORIGINAL_INDEX + i
                                    )!!
                                )
                            )
                        }
                    }
                }
                if (underEdit != true) {
                    for ((index, appCard) in appCards.withIndex()) {
                        appCard.originalIndex = index
                        savedStateHandle
                            .set(REVIEW_OF_ORIGINAL_INDEX+appCard.originalIndex, appCard.review)
                    }
                    val screenShotItems = getScreenShotItems(listId).sortedBy { it.index }
                    for ((index, i) in screenShotItems.withIndex()) {
                        savedStateHandle.set(URI_STRING+index, i.uriString)
                    }
                    val appCardList =
                        appListRepository.getAppCardList(listId)
                            ?: AppCardList(id = 0L,"")
                    savedStateHandle.set(
                        SCREEN_SHOT_DESCRIPTION,
                        appCardList.screenShotDescription
                    )
                    _imageUriStringList.value = screenShotItems.map { it.uriString }.toMutableList()
                }
                _userAppCards.value = appCardsStore.toMutableList()
                savedStateHandle.set(UNDER_EDIT, true)
            }
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

    private suspend fun createAddAppCards(packageNameList: MutableList<String>) {
        withContext(Dispatchers.IO) {
            val listId = _userAppCards.value!!.last().listId
            //TODO("alert if number Of Apps exceeds MAX NUMBER OF APPS")
            val listAddAppCards: MutableList<AppCard> = MutableList(packageNameList.size) { index ->
                AppCard(
                    id = 0,
                    listId = listId,
                    originalIndex = appCardsToDelete.size + _userAppCards.value!!.size + index,
                    index = _userAppCards.value!!.size + index,
                    packageName = packageNameList[index]
                )
            }
            for (i in listAddAppCards){
                savedStateHandle.set(
                    ADDED_APP_CARD_PACKAGE_NAME_OF_ORIGINAL_INDEX + i.originalIndex,i.packageName
                )
            }
            userAppCardListStore = _userAppCards.value!!
            userAppCardListStore.plusAssign(listAddAppCards)
            userAppCardListStore.sortBy { it.index }
            savedStateHandle.set(
                MAX_OF_ORIGINAL_INDEX, appCardsToDelete.size + userAppCardListStore.size - 1
            )
            _userAppCards.postValue(userAppCardListStore)
        }
    }

    private suspend fun saveAppCards() {
        withContext(Dispatchers.IO) {
            for ((index, appCard) in _userAppCards.value!!.withIndex()) {
                appCard.index = index
                appCard.review = userReviewList[appCard.originalIndex].value ?: ""
                appListRepository.save(appCard)
            }
        }
    }

    private suspend fun saveAppCardList(listUrl: String = "",uploadDate: Date? = null) {
        withContext(Dispatchers.IO) {
            appListRepository.saveAppCardList(
                AppCardList(
                    id = listId ?: 0L,
                    screenShotDescription = screenShotDescription.value ?: "",
                    listIdOnFirebase = userListId,
                    listUrl = listUrl,
                    sharedDate = uploadDate
                )
            )
        }
    }

    private suspend fun deleteAppCards() {
        for (i in appCardsToDelete) {
            appListRepository.deleteAppCard(i.id)
        }
    }

    private suspend fun uploadUserAppList(): String {
        return withContext(Dispatchers.IO) {
            return@withContext appListRepository.shareList(_userAppCards.value!!)
        }
    }

    private suspend fun uploadUserScreenShot(listUrl: String = ""): Date?{
        return withContext(Dispatchers.IO) {
            return@withContext appListRepository.shareScreenShot(
                screenShotItemList.value!!,
                userListId,
                listUrl,
                screenShotDescription.value ?: ""
            )
        }
    }

    private suspend fun saveScreenShotItem() {
        withContext(Dispatchers.IO) {
            if (screenShotItemList.value != null) {
                appListRepository.deleteScreenShotItem(listId!!)
                for (i in screenShotItemList.value!!) {
                    appListRepository.saveScreenShotItem(i.uriString, i.index, listId!!)
                }
            }
        }

    }

    fun removeAppCardFromList(index: Int) {
        appListScope.launch {
            _userAppCards.value!!.find { it.originalIndex == index }?.let {
                savedStateHandle.set(
                    NON_DISPLAYED_CARD_OF+it.packageName, true)
                appCardsToDelete.add(it)
                if (it.originalIndex >= numberOfAppCardsFromDatabase) {
                    savedStateHandle.remove<String>(
                        ADDED_APP_CARD_PACKAGE_NAME_OF_ORIGINAL_INDEX+it.originalIndex
                    )
                }
            }
            userAppCardListStore = _userAppCards.value!!
            userAppCardListStore.remove(userAppCardListStore.find { it.originalIndex == index })
            for ((i, appCard) in userAppCardListStore.withIndex()) {
                appCard.index = i
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

    fun getUserListUrl():String{
        return userListUrl
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

    fun changeEditability() {
        appListScope.launch {
            descriptionEditable.value = !descriptionEditable.value!!
        }
    }

    fun saveAction() {
        appListScope.launch {
            saveScreenShotItem()
            saveAppCardList()
            saveAppCards()
            deleteAppCards()
        }
    }

    fun shareAction() {
        appListScope.launch {
            if (_userAppCards.value!!.lastIndex < MAX_NUMBER_OF_APPS) {
                _isUploading.postValue(true)
                userListId = CreateUserListId().createId()
                saveAppCards()
                saveScreenShotItem()
                userListUrl = uploadUserAppList()
                val uploadDate: Date? = uploadUserScreenShot(userListUrl)
                saveAppCardList(userListUrl,uploadDate)
                deleteAppCards()
                _isUploading.postValue(false)
            }
        }
    }

    fun setImageUri(uriList: MutableList<Uri?>) {
        appListScope.launch {
//            TODO("to check if uri is of ScreenShot Image.")
            val imageUriStringList = _imageUriStringList.value?.apply { removeAll(listOf("")) }
            val uriStringList = uriList.map { it.toString() }.toMutableList()
            _imageUriStringList.value =
                imageUriStringList?.apply { plusAssign(uriStringList) } ?: uriStringList
            if (_imageUriStringList.value != null) {
                for ((index, i) in _imageUriStringList.value!!.withIndex()) {
                    savedStateHandle.set(
                        URI_STRING+index,
                        i
                    )
                }
            }
        }
    }

    fun removeScreenShotItem(screenShotItem: ScreenShotItem) {
        appListScope.launch {
            uriStringStore = _imageUriStringList.value!!
            savedStateHandle.remove<String>(
                URI_STRING + screenShotItem.index
            )
            uriStringStore.removeAt(screenShotItem.index)
            _imageUriStringList.value = uriStringStore
        }
    }
}