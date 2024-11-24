package com.syrous.imageloadinglib.data.okhttp

import com.syrous.imageloadinglib.data.response.Photo

interface OkHttpService {

    suspend fun getPhotoListByPage(page: Int, perPage: Int): List<Photo>

}