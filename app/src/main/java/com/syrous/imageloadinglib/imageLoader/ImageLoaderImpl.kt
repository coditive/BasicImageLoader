package com.syrous.imageloadinglib.imageLoader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.unit.IntSize
import com.syrous.imageloadinglib.imageLoader.bitmap.LruBitmapPool
import com.syrous.imageloadinglib.imageLoader.bitmap.UrlBitmapManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import java.io.ByteArrayInputStream
import java.lang.ref.WeakReference
import java.util.LinkedList
import java.util.Queue

class ImageLoaderImpl private constructor(private val builder: ImageLoaderBuilder) : ImageLoader {

    private lateinit var lruBitmapPool: LruBitmapPool
    private lateinit var urlBitmapManager: UrlBitmapManager

    override val apiCallCountState: MutableStateFlow<Int> = MutableStateFlow(0)
    private var apiCallCount = 0


    private val imageRequestQueue: Queue<String> = LinkedList()
    private val onGoingRequest = mutableSetOf<String>()

    private var targetWidth: Int? = null
    private var targetHeight: Int? = null

    private val lock = Mutex()

    override fun with(context: Context): ImageLoader {
        lruBitmapPool = LruBitmapPool(context)
        urlBitmapManager = UrlBitmapManager(lruBitmapPool)
        return this
    }

    override fun load(url: String): ImageLoader {
        imageRequestQueue.add(url)
        return this
    }

    private fun scaleBitmapIfNeeded(bitmap: Bitmap?): Bitmap? {
        if (bitmap == null || targetWidth == null || targetHeight == null) return bitmap
        val aspectRatio = bitmap.width.toFloat() / bitmap.height.toFloat()

        val scaledWidth = targetWidth!!
        val scaledHeight = (targetWidth!! / aspectRatio).toInt()

        return Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)
    }

    override fun resolveSize(size: IntSize): ImageLoader {
        targetWidth = size.width
        targetHeight = size.height
        return this
    }

    /**
     *  This function considers following scenarios:
     *     1: When request is not made for that url, make api call to load
     *     2: When request is made for that url, get the cached result instead of making api call
     */
    override suspend fun getImageAsync(): Bitmap? {
        while (imageRequestQueue.isEmpty().not()) {
            val imageRequest = imageRequestQueue.poll() ?: continue

            // Check cache first
            urlBitmapManager.getBitmapForUrl(imageRequest)?.let { cachedBitmap ->
                return cachedBitmap // Return cached image if available
            }

            // Avoid duplicate API calls
            if (onGoingRequest.contains(imageRequest).not()) {
                onGoingRequest.add(imageRequest)
                try {
                    return withContext(Dispatchers.IO) {
                        getBitmapFromUrl(imageRequest)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    onGoingRequest.remove(imageRequest) // Remove request from ongoing list
                }
            }
        }
        return null
    }

    private suspend fun getBitmapFromUrl(url: String): Bitmap {
        val request = Request.Builder()
            .url(url)
            .build()

        try {
            lock.withLock {
                apiCallCount += 1
                apiCallCountState.emit(apiCallCount)

                // Execute the request
                val response = builder.okHttpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    throw IOException("Unexpected HTTP response code: ${response.code}")
                }
                // Decode the response body as a Bitmap
                val inputStream = response.body?.byteStream()
                val byteArray =
                    inputStream?.readBytes() // Read the stream into a byte array
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

                val reusableBitmap =
                    urlBitmapManager.getBitmapForUrl(url, targetWidth, targetHeight, config)

                // Step 3: Decode with reusable bitmap
                val bitmap = try {
                    options.apply {
                        inJustDecodeBounds = false
                        inMutable = true
                        inBitmap = reusableBitmap // Use only if reusable bitmap is valid
                    }
                    BitmapFactory.decodeStream(reusableStream, null, options)
                } catch (e: IllegalArgumentException) {
                    Log.w("ImageLoader", "Reusable bitmap failed: ${e.message}")
                    options.inBitmap = null // Retry without reusable bitmap
                    BitmapFactory.decodeStream(reusableStream, null, options)
                }

                val scaledBitmap = scaleBitmapIfNeeded(bitmap)
                if (scaledBitmap == null || scaledBitmap.width <= 0 || scaledBitmap.height <= 0) {
                    throw IllegalArgumentException("Failed to decode bitmap")
                }
                urlBitmapManager.putBitmapForUrl(url, scaledBitmap) // Add to pool after usage
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return urlBitmapManager.getBitmapForUrl(url)!!
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
                .cache(Cache(context.get()?.cacheDir!!, 50L * 1024 * 1024))
                .build()
            okHttpClient = client
            return ImageLoaderImpl(this)
        }
    }


}