package com.example.ui.util

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import java.util.concurrent.ConcurrentHashMap

/**
 * High-performance, low-RAM icon cache for application icons.
 * Prevents expensive on-the-fly Drawable.toBitmap() conversions during LazyColumn / LazyRow
 * and Pager scrolls, eliminating GC pauses and dropping CPU & RAM consumption.
 */
object AppIconCache {
    private val iconCache = ConcurrentHashMap<String, ImageBitmap>()

    fun getIcon(packageName: String, drawable: Drawable?, sizePx: Int = 96): ImageBitmap? {
        if (drawable == null) return null
        val key = "$packageName-$sizePx"
        val cached = iconCache[key]
        if (cached != null) return cached

        return try {
            val bmp = drawable.toBitmap(width = sizePx, height = sizePx, config = Bitmap.Config.ARGB_8888)
            val imgBmp = bmp.asImageBitmap()
            iconCache[key] = imgBmp
            imgBmp
        } catch (_: Throwable) {
            try {
                val fallbackBmp = drawable.toBitmap().asImageBitmap()
                iconCache[key] = fallbackBmp
                fallbackBmp
            } catch (_: Throwable) {
                null
            }
        }
    }

    fun clear() {
        iconCache.clear()
    }
}
