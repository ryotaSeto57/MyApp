package com.example.myapp.page.title

import androidx.lifecycle.*
import com.example.myapp.database.AppCard
import com.example.myapp.database.AppCardList
import com.example.myapp.repository.AppListRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TitleViewModel @Inject constructor(
    private val appListRepository: AppListRepository,
    private val savedStateHandle: SavedStateHandle
):ViewModel() {

    init {
        Timber.i("TitleViewModel is created.")
        Timber.i(savedStateHandle.keys().joinToString())
    }

    val userPastAppCardLists: LiveData<MutableList<AppCardList>> = liveData {
        val appAppCardLists = appListRepository.getAppCardLists()
        emitSource(appAppCardLists)
    }

    val userPastAppCards:LiveData<MutableList<AppCard>> = liveData {
        val allAppCards = appListRepository.getAllAppCards()
        emitSource(allAppCards)
    }

}