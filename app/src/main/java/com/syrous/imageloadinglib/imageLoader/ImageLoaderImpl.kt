package com.syrous.imageloadinglib.imageLoader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import java.io.ByteArrayInputStream
import java.lang.ref.WeakReference

class ImageLoaderImpl private constructor(private val builder: ImageLoaderBuilder) : ImageLoader {

    private lateinit var loadedUrl: MutableSet<String>
    private lateinit var memoryCache: MemoryCache
    private lateinit var bitmapPool: BitmapPool

    override fun with(context: Context): ImageLoader {
        val runTimeMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = runTimeMemory / 8
        memoryCache = MemoryCache(cacheSize)
        loadedUrl = mutableSetOf()
        bitmapPool = BitmapPool(10)
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
                val byteArray = inputStream?.readBytes() // Read the stream into a byte array
                val reusableStream = ByteArrayInputStream(byteArray)
                // Step 1: Decode the dimensions
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeStream(reusableStream, null, options)

                if (options.outWidth <= 0 || options.outHeight <= 0) {
                    throw IllegalArgumentException("Invalid image dimensions: width=${options.outWidth}, height=${options.outHeight}")
                }
                reusableStream.reset()
                // Step 2: Fetch reusable bitmap
                val targetWidth = options.outWidth
                val targetHeight = options.outHeight
                val config = Bitmap.Config.ARGB_8888 // Or dynamically fetch

                val reusableBitmap = bitmapPool.getBitmap(targetWidth, targetHeight, config)

                // Step 3: Decode with reusable bitmap
                val bitmap = try {
                    options.apply {
                        inJustDecodeBounds = false
                        inMutable = true
                        inSampleSize = inSampleSize
                        inBitmap = reusableBitmap // Use only if reusable bitmap is valid
                    }
                    BitmapFactory.decodeStream(reusableStream, null, options)
                } catch (e: IllegalArgumentException) {
                    Log.w("ImageLoader", "Reusable bitmap failed: ${e.message}")
                    options.inBitmap = null // Retry without reusable bitmap
                    BitmapFactory.decodeStream(reusableStream, null, options)
                }

                if (bitmap == null || bitmap.width <= 0 || bitmap.height <= 0) {
                    throw IllegalArgumentException("Failed to decode bitmap")
                }

                bitmapPool.putBitmap(bitmap) // Add to pool after usage
                memoryCache.put(url, bitmap)
                loadedUrl.add(url)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return this
    }

    override fun get(url: String): Bitmap? {
        return memoryCache.get(url)
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