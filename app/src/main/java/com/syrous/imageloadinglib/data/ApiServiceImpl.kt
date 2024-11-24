package com.syrous.imageloadinglib.data

import com.syrous.imageloadinglib.data.okhttp.OkHttpService
import com.syrous.imageloadinglib.data.response.Photo

class ApiServiceImpl(private val okHttpService: OkHttpService): ApiService {

    override suspend fun getPhotoListByPage(page: Int, perPage: Int): List<Photo> {
        return okHttpService.getPhotoListByPage(page, perPage)
    }

    override suspend fun getRandomPhoto() {
        TODO("Not yet implemented")
    }
}