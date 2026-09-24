package com.petermathie.vibecheck.data.seed

import android.content.Context
import android.graphics.BitmapFactory
import com.petermathie.vibecheck.data.local.VibeDatabase
import java.io.File
import java.io.IOException
import kotlin.math.abs

internal object DemoProgressPhotos {
    private const val MARKER_DIRECTORY = "demo-progress-photo-ownership"
    private val palettes = listOf(
        0xFF263238.toInt() to 0xFF26A69A.toInt(),
        0xFF312A3D.toInt() to 0xFF7E57C2.toInt(),
        0xFF3D2A2A.toInt() to 0xFFEF5350.toInt(),
        0xFF243324.toInt() to 0xFF8BC34A.toInt(),
    )

    fun palette(week: Int): Pair<Int, Int> = palettes[(week / 17).coerceIn(palettes.indices)]

    fun markOwned(context: Context, file: File) {
        val markers = File(context.filesDir, MARKER_DIRECTORY)
        if (!markers.exists() && !markers.mkdirs()) {
            throw IOException("Could not create demo photo ownership directory")
        }
        val marker = File(markers, file.name)
        if (!marker.exists() && !marker.createNewFile()) {
            throw IOException("Could not record ownership for demo photo ${file.name}")
        }
    }

    fun claimLegacyPhotos(context: Context, database: VibeDatabase) {
        val photos = File(context.filesDir, "progress-photos")
        database.openHelper.readableDatabase.query(
            "SELECT id, recordedAt FROM body_measurements WHERE isDemo = 1 AND id LIKE 'demo-bodyweight-%'",
        ).use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getString(0)
                val week = id.substringAfterLast('-').toIntOrNull() ?: continue
                val file = File(photos, "${cursor.getLong(1)}.jpg")
                if (file.isFile && matchesGeneratedPhoto(file, week)) markOwned(context, file)
            }
        }
    }

    fun deleteOwned(context: Context): Int {
        val markers = File(context.filesDir, MARKER_DIRECTORY)
        val markerFiles = markers.listFiles().orEmpty()
        val photos = File(context.filesDir, "progress-photos")
        var deleted = 0
        markerFiles.forEach { marker ->
            require(marker.isFile && marker.name.endsWith(".jpg") && '/' !in marker.name && '\\' !in marker.name) {
                "Invalid demo photo ownership marker"
            }
            val photo = File(photos, marker.name)
            if (photo.exists() && !photo.delete()) {
                throw IOException("Could not remove generated demo photo ${photo.name}")
            }
            if (!marker.delete()) {
                throw IOException("Could not clear ownership marker for ${photo.name}")
            }
            deleted++
        }
        if (markers.exists() && markers.list().orEmpty().isEmpty() && !markers.delete()) {
            throw IOException("Could not remove empty demo photo ownership directory")
        }
        return deleted
    }

    private fun matchesGeneratedPhoto(file: File, week: Int): Boolean {
        val bitmap = BitmapFactory.decodeFile(file.path) ?: return false
        return try {
            if (bitmap.width != 720 || bitmap.height != 960) return false
            val (background, accent) = palette(week)
            coloursNear(bitmap.getPixel(20, 20), background) &&
                coloursNear(bitmap.getPixel(360, 230), accent)
        } finally {
            bitmap.recycle()
        }
    }

    private fun coloursNear(actual: Int, expected: Int): Boolean =
        abs(android.graphics.Color.red(actual) - android.graphics.Color.red(expected)) <= 24 &&
            abs(android.graphics.Color.green(actual) - android.graphics.Color.green(expected)) <= 24 &&
            abs(android.graphics.Color.blue(actual) - android.graphics.Color.blue(expected)) <= 24
}
