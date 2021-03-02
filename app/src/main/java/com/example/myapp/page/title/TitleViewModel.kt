package com.example.myapp.page.title

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.database.AppCard
import com.example.myapp.database.AppCardList
import com.example.myapp.repository.AppListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TitleViewModel @Inject constructor(
    private val appListRepository: AppListRepository
):ViewModel() {

    private val titleScope = viewModelScope

    init {
        getPastAppCardLists()
        getPastAppCards()
        Timber.i("TitleViewModel is created.")
    }

    private val _userPastAppCardLists = MutableLiveData<MutableList<AppCardList>>()
    val userPastAppCardLists: LiveData<MutableList<AppCardList>>
        get() = _userPastAppCardLists

    private val _userPastAppCards = MutableLiveData<MutableList<AppCard>>()



    private fun getPastAppCardLists(){
        titleScope.launch {
           val appCardLists =  appListRepository.getAppCardLists()
            _userPastAppCardLists.value = appCardLists
        }
    }

    private fun getPastAppCards(){
        titleScope.launch {
            val appCards = appListRepository.getAllAppCards()
            _userPastAppCards.value = appCards
        }
    }

    fun getAppCards(listId: Long): List<AppCard>{
        return _userPastAppCards.value!!.filter { it.listId == listId }.sortedBy { it.index }
    }

}