package com.example.semester2

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        val sharedPreferences = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        // 0 = Ikuti Sistem (Default), 1 = Terang, 2 = Gelap
        val themeMode = sharedPreferences.getInt("theme_mode", 0)
        
        when (themeMode) {
            1 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            2 -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            else -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}
