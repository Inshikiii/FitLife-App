package com.example.fitlife

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

class FitLifeApp : Application() {

    override fun onCreate() {
        super.onCreate()

        val prefs: SharedPreferences =
            getSharedPreferences("fitlife_prefs", MODE_PRIVATE)

        val isDarkMode = prefs.getBoolean("dark_mode", true)

        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode)
                AppCompatDelegate.MODE_NIGHT_YES
            else
                AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
