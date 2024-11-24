package com.syrous.imageloadinglib.data

import com.syrous.imageloadinglib.data.response.Photo

interface ApiService {

    suspend fun getPhotoListByPage(page: Int, perPage: Int): List<Photo>

    suspend fun getRandomPhoto()

}