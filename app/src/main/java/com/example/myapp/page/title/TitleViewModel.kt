package com.example.myapp.page.title

import androidx.lifecycle.*
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
        Timber.i("TitleViewModel is created.")
    }

    private val _userPastAppCardLists = MutableLiveData<MutableList<AppCardList>>()
    val userPastAppCardLists: LiveData<MutableList<AppCardList>>
        get() = _userPastAppCardLists

    val userPastAppCards:LiveData<MutableList<AppCard>> = liveData {
        val allAppCards = appListRepository.getAllAppCards()
        emitSource(allAppCards)
    }


    private fun getPastAppCardLists(){
        titleScope.launch {
           val appCardLists =  appListRepository.getAppCardLists()
            _userPastAppCardLists.value = appCardLists
        }
    }

}