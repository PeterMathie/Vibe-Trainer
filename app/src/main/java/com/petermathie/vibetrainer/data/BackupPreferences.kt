package com.petermathie.vibetrainer.data

import android.content.SharedPreferences
import org.json.JSONObject

/** Only durable profile and presentation choices belong in a backup, never running timers. */
object BackupPreferences {
    private val booleans = setOf("lb", "female", "autoRest", "haptic", "reducedMotion")
    private val colours = setOf("accent", "background", "surface")

    fun attach(json: String, preferences: SharedPreferences): String {
        val values = JSONObject()
        preferences.all.forEach { (key, value) ->
            if (key in booleans || key in colours || key == "palette") values.put(key, value)
        }
        return JSONObject(json).put("preferences", values).toString(2)
    }

    fun validate(json: String): JSONObject? {
        val values = JSONObject(json).optJSONObject("preferences") ?: return null
        values.keys().forEach { key ->
            val value = values.get(key)
            require(when (key) {
                in booleans -> value is Boolean
                in colours -> value is Number && value.toDouble() == value.toInt().toDouble()
                "palette" -> value is String
                else -> false
            }) { "Invalid preference: $key" }
        }
        return values
    }

    fun restore(values: JSONObject?, preferences: SharedPreferences) {
        if (values == null) return
        preferences.edit().apply {
            values.keys().forEach { key ->
                when (key) {
                    in booleans -> putBoolean(key, values.getBoolean(key))
                    in colours -> putInt(key, values.getInt(key))
                    "palette" -> putString(key, values.getString(key))
                }
            }
        }.apply()
    }
}
