package com.example.swapease.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class ThemePreferenceUtil(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)
    private val editor: SharedPreferences.Editor = prefs.edit()

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
    }

    fun setThemeMode(themeMode: Int) {
        editor.putInt(KEY_THEME_MODE, themeMode)
        editor.apply()
    }

    fun getThemeMode(): Int {
        return prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_NO)
    }
}