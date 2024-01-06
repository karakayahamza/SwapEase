package com.example.swapease.ui.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.swapease.R
import com.example.swapease.databinding.ActivityLoginBinding
import com.example.swapease.utils.ThemePreferenceUtil

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val themePreferenceUtil by lazy { ThemePreferenceUtil(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val themeMode = themePreferenceUtil.getThemeMode()

        if (themeMode == AppCompatDelegate.MODE_NIGHT_YES) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}