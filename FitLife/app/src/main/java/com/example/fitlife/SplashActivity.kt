package com.example.fitlife

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        // ===== APPLY DARK MODE GLOBALLY (BEFORE UI LOADS) =====
        val prefs = getSharedPreferences("fitlife_prefs", MODE_PRIVATE)
        val darkModeEnabled = prefs.getBoolean("dark_mode", true)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val session = SessionManager(this)

        Handler(Looper.getMainLooper()).postDelayed({
            if (session.isLoggedIn()) {
                startActivity(Intent(this, DashboardActivity::class.java))
            } else {
                startActivity(Intent(this, WelcomeActivity::class.java))
            }
            finish()
        }, 2000)
    }
}
