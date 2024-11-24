package com.syrous.imageloadinglib.data.okhttp

import android.util.Log
import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.syrous.imageloadinglib.data.response.Photo
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okio.IOException

class OkHttpServiceImpl private constructor(
    private val okHttpServiceBuilder: OkHttpServiceBuilder
) : OkHttpService {

    private val TAG = "OkHttpService"

    override suspend fun getPhotoListByPage(page: Int, perPage: Int): List<Photo> {
        val url = okHttpServiceBuilder.baseUrl + "photos/?client_id=32j6quR38rEBMnQ-yI9h1XbEW3PZb9NG0AaXKEYytDk&page=$page&per_page=$perPage"
        val request = Request.Builder()
            .url(url)
            .build()

        var photoResponse = emptyList<Photo>()
        try {
            val response: Response = okHttpServiceBuilder.okHttpClient.newCall(request).execute()
            if (response.isSuccessful) {
                // Get response body as a string
                photoResponse = response.body?.let { responseBody ->
                    val type = Types.newParameterizedType(List::class.java, Photo::class.java)
                    val adapter = okHttpServiceBuilder.moshi.adapter<List<Photo>>(type)
                    val jsonReader = JsonReader.of(responseBody.source())
                    jsonReader.isLenient = true
                    adapter.fromJson(jsonReader)
                }!!
                Log.d(TAG, photoResponse.toString())
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }

        return photoResponse
    }

    companion object {
        fun Builder(): OkHttpServiceBuilder = OkHttpServiceBuilder()
    }

    class OkHttpServiceBuilder {

        lateinit var okHttpClient: OkHttpClient
            private set
        lateinit var baseUrl: String
            private set
        lateinit var moshi: Moshi
            private set

        fun addOkHttpClient(client: OkHttpClient): OkHttpServiceBuilder {
            okHttpClient = client
            return this
        }

        fun addBaseUrl(url: String): OkHttpServiceBuilder {
            baseUrl = url
            return this
        }

        fun addMoshi(moshi: Moshi): OkHttpServiceBuilder {
            this.moshi = moshi
            return this
        }

        fun build(): OkHttpServiceImpl {
            return OkHttpServiceImpl(this)
        }
    }

}