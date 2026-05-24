package com.example.semester2

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // Load theme from SharedPreferences and apply
        val sharedPreferences = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean("is_dark_mode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}
