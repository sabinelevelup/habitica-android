package com.habitrpg.android.habitica.widget.glance.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.habitrpg.common.habitica.extensions.DataBindingUtils
import com.habitrpg.common.habitica.helpers.SpriteSubstitutionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object WidgetBackgroundCache {
    private const val FILENAME = "widget_today_user_bg.png"
    private const val HASH_FILENAME = "widget_today_user_bg.hash"
    private const val MAX_EDGE = 1024

    fun cachedBitmap(context: Context): Bitmap? {
        val file = file(context)
        if (!file.exists()) return null
        return runCatching { BitmapFactory.decodeFile(file.absolutePath) }.getOrNull()
    }

    fun clearCache(context: Context) {
        runCatching { file(context).delete() }
        runCatching { hashFile(context).delete() }
    }

    suspend fun refreshIfNeeded(context: Context, backgroundKey: String?) {
        val appContext = context.applicationContext
        if (backgroundKey.isNullOrBlank()) {
            clearCache(appContext)
            return
        }
        val imageName = SpriteSubstitutionManager.substitute("background_$backgroundKey", "backgrounds")
        if (readHash(appContext) == imageName && file(appContext).exists()) return
        val bitmap = withContext(Dispatchers.IO) { download(imageName) } ?: return
        withContext(Dispatchers.IO) {
            runCatching {
                FileOutputStream(file(appContext)).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
                hashFile(appContext).writeText(imageName)
            }
        }
    }

    private fun download(imageName: String): Bitmap? {
        val filename = DataBindingUtils.getFullFilename(imageName, "png", disableAnimations = true)
        val connection = (URL(DataBindingUtils.BASE_IMAGE_URL + filename).openConnection() as HttpURLConnection).apply {
            connectTimeout = 10_000
            readTimeout = 15_000
            instanceFollowRedirects = true
        }
        return try {
            if (connection.responseCode !in 200..299) return null
            val decoded = connection.inputStream.use { BitmapFactory.decodeStream(it) } ?: return null
            scaleDown(decoded)
        } catch (_: Exception) {
            null
        } finally {
            connection.disconnect()
        }
    }

    private fun scaleDown(src: Bitmap): Bitmap {
        val max = maxOf(src.width, src.height)
        if (max <= MAX_EDGE) return src
        val scale = MAX_EDGE.toFloat() / max
        return Bitmap.createScaledBitmap(
            src,
            (src.width * scale).toInt().coerceAtLeast(1),
            (src.height * scale).toInt().coerceAtLeast(1),
            false,
        )
    }

    private fun file(context: Context) = File(context.filesDir, FILENAME)

    private fun hashFile(context: Context) = File(context.filesDir, HASH_FILENAME)

    private fun readHash(context: Context): String? =
        runCatching { hashFile(context).takeIf { it.exists() }?.readText() }.getOrNull()
}
