package com.syrous.imageloadinglib

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.syrous.imageloadinglib.data.ApiService
import com.syrous.imageloadinglib.data.response.Photo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(private val apiService: ApiService): ViewModel() {

    private val _photoListStream = MutableStateFlow(emptyList<Photo>())
    val photoListStream: StateFlow<List<Photo>> = _photoListStream

    fun getPhotoList() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val combinedList = mutableListOf<Photo>()
                combinedList.addAll(apiService.getPhotoListByPage(1, 100))
                combinedList.addAll(apiService.getPhotoListByPage(2, 100))
                combinedList.addAll(apiService.getPhotoListByPage(3, 100))
                _photoListStream.emit(combinedList)
            }
        }
    }
}