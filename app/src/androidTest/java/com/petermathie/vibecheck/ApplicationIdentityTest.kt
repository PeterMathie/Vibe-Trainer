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

@RunWith(AndroidJUnit4::class)
class ApplicationIdentityTest {
    @Test
    fun packageLabelLauncherAndRoomSchemasUseVibeCheckIdentity() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assertEquals("com.petermathie.vibecheck", context.packageName)
        assertEquals("Vibe Check", context.applicationInfo.loadLabel(context.packageManager).toString())
        assertTrue(context.applicationInfo.icon != 0)
        assertTrue(context.applicationInfo.roundIcon != 0)

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        assertNotNull(launchIntent)
        assertEquals("com.petermathie.vibecheck.MainActivity", launchIntent?.component?.className)

        val schemaPath = "com.petermathie.vibecheck.data.local.VibeDatabase"
        val schemas = InstrumentationRegistry.getInstrumentation().context.assets.list(schemaPath).orEmpty().toSet()
        assertTrue((2..12).all { "$it.json" in schemas })
    }
}
