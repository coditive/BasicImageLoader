package com.syrous.imageloadinglib.imageLoader.bitmap

import android.app.ActivityManager
import android.content.Context
import android.graphics.Bitmap
import java.lang.ref.WeakReference

class LruBitmapPool(context: Context): BitmapPool {

    private val pool: LinkedHashMap<BitmapKey, WeakReference<Bitmap>>
    private val evictionObserver = mutableListOf<(BitmapKey) -> Unit>()
    private var hit = 0
    private var miss = 0

    init {
        val poolSize = calculatePoolSize(context)
        pool = object: LinkedHashMap<BitmapKey, WeakReference<Bitmap>>(poolSize, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<BitmapKey, WeakReference<Bitmap>>?): Boolean {
                if (size > poolSize && eldest != null) {
                    evictionObserver.forEach { it(eldest.key) }
                    return true
                }
                return false
            }
        }
    }

    private fun calculatePoolSize(context: Context): Int {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryClass = activityManager.memoryClass * 1024 * 1024
        return memoryClass / 4
    }

    override fun getCurrentMaxSize(): Int = pool.size

    override fun getBitmap(width: Int, height: Int, config: Bitmap.Config): Bitmap {
        val key = BitmapKey(width, height, config)
         return pool[key]?.get().also {
             hit++
         } ?: run {
             val bitmap = Bitmap.createBitmap(width, height, config)
             pool[key] = WeakReference(bitmap)
             miss++
             bitmap
        }
    }

    override fun getBitmap(key: BitmapKey): Bitmap {
        return pool[key]!!.get()!!
    }

    override fun putBitmap(bitmap: Bitmap) {
        if(bitmap.isMutable && !bitmap.isRecycled) {
            val key = BitmapKey(bitmap.width, bitmap.height, bitmap.config!!)
            pool[key] = WeakReference(bitmap)
        }
    }

    override fun addEvictObserver(observer: (BitmapKey) -> Unit) {
        evictionObserver.add(observer)
    }

    override fun removeEvictObserver(observer: (BitmapKey) -> Unit) {
        evictionObserver.remove(observer)
    }
}