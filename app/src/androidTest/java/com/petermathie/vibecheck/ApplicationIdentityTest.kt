package com.petermathie.vibecheck

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.xmlpull.v1.XmlPullParser

@RunWith(AndroidJUnit4::class)
class ApplicationIdentityTest {
    @Test
    fun packageLabelLauncherAndRoomSchemasUseVibeCheckIdentity() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("com.petermathie.vibecheck", context.packageName)
        assertEquals("Vibe Check", context.applicationInfo.loadLabel(context.packageManager).toString())
        assertTrue(context.applicationInfo.icon != 0)
        assertTrue(context.resources.getIdentifier("ic_launcher_round", "mipmap", context.packageName) != 0)

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        assertNotNull(launchIntent)
        assertEquals("com.petermathie.vibecheck.MainActivity", launchIntent?.component?.className)

        val schemaPath = "com.petermathie.vibecheck.data.local.VibeDatabase"
        val schemas = InstrumentationRegistry.getInstrumentation().context.assets.list(schemaPath).orEmpty().toSet()
        assertTrue((2..13).all { "$it.json" in schemas })
    }

    @Test
    fun launcherUsesBlueBackgroundWithRedVAndGreenC() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals(0xFF1565C0.toInt(), context.getColor(R.color.ic_launcher_background))

        val parser = context.resources.getXml(R.drawable.ic_launcher_foreground)
        val strokes = mutableListOf<String>()
        while (parser.eventType != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.START_TAG && parser.name == "path") {
                parser.getAttributeValue("http://schemas.android.com/apk/res/android", "strokeColor")
                    ?.let(strokes::add)
            }
            parser.next()
        }
        assertEquals(listOf("#ffe53935", "#ff43a047"), strokes)
    }
}
