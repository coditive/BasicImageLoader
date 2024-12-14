package com.syrous.imageloadinglib.imageLoader

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.ui.unit.IntSize
import com.syrous.imageloadinglib.imageLoader.bitmap.LruBitmapPool
import com.syrous.imageloadinglib.imageLoader.bitmap.UrlBitmapManager
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.IOException
import java.io.ByteArrayInputStream
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

class ImageLoaderImpl private constructor(private val builder: ImageLoaderBuilder) : ImageLoader {

    private lateinit var lruBitmapPool: LruBitmapPool
    private lateinit var urlBitmapManager: UrlBitmapManager
    private var onGoingRequest: MutableMap<String, Deferred<Unit>> = ConcurrentHashMap()

    private var targetWidth: Int? =  null
    private var targetHeight: Int? =  null

    override fun with(context: Context): ImageLoader {
        lruBitmapPool = LruBitmapPool(context)
        urlBitmapManager = UrlBitmapManager(lruBitmapPool)
        return this
    }

    override suspend fun load(url: String): ImageLoader {

        onGoingRequest[url]?.cancel()

        val job = coroutineScope {
            async(Dispatchers.IO) {
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

                    val reusableBitmap = urlBitmapManager.getBitmapForUrl(url, targetWidth, targetHeight, config)

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

                    val scaledBitmap = scaleBitmapIfNeeded(bitmap)
                    if (scaledBitmap == null || scaledBitmap.width <= 0 || scaledBitmap.height <= 0) {
                        throw IllegalArgumentException("Failed to decode bitmap")
                    }
                    urlBitmapManager.putBitmapForUrl(url, scaledBitmap) // Add to pool after usage
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    onGoingRequest.remove(url)
                }
            }
        }
        onGoingRequest[url] = job
        job.join()
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

    override suspend fun getAsync(url: String): Bitmap? = coroutineScope {
        onGoingRequest[url]?.join()
        urlBitmapManager.getBitmapForUrl(url)
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