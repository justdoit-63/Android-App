package com.example.fitness.data

import android.content.Context
import android.content.SharedPreferences

class WeightPrefs(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("weight_prefs", Context.MODE_PRIVATE)

    var isBulking: Boolean
        get() = prefs.getBoolean("is_bulking", true)
        set(value) = prefs.edit().putBoolean("is_bulking", value).apply()
}
