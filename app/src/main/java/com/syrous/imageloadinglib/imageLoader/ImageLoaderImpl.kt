package com.syrous.imageloadinglib.imageLoader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.FileSystem
import okio.IOException
import java.lang.ref.WeakReference

class ImageLoaderImpl private constructor(private val builder: ImageLoaderBuilder) : ImageLoader {

    private lateinit var loadedUrl: MutableSet<String>
    private lateinit var bitmapPool: MemoryCache

    override fun with(context: Context): ImageLoader {
        val runTimeMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = runTimeMemory / 8
        bitmapPool = MemoryCache(cacheSize)
        loadedUrl = mutableSetOf()
        return this
    }

    override suspend fun load(url: String): ImageLoader {
        if (loadedUrl.contains(url).not()) {
            val request = Request.Builder()
                .url(url)
                .build()

            try {
                // Execute the request
                val response = builder.okHttpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    throw IOException("Unexpected HTTP response code: ${response.code}")
                }
                // Decode the response body as a Bitmap
                val inputStream = response.body?.byteStream()
                bitmapPool.put(url,BitmapFactory.decodeStream(inputStream))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return this
    }

    override fun get(url: String): Bitmap? {
        return bitmapPool.get(url)
    }


    companion object {
        fun Builder(context: Context) = ImageLoaderBuilder(context)
    }

    class ImageLoaderBuilder(appContext: Context) {
        val context: WeakReference<Context> = WeakReference(appContext)

        lateinit var okHttpClient: OkHttpClient
            private set


        fun build(): ImageLoaderImpl {
            val client = OkHttpClient.Builder()
                .cache(Cache(context.get()?.cacheDir!!, 10 * 1024))
                .build()
            okHttpClient = client
            return ImageLoaderImpl(this)
        }
    }


}