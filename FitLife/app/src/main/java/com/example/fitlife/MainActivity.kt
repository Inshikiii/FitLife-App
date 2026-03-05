package com.example.fitlife

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val session = SessionManager(this)

        if (session.isLoggedIn()) {
            // User already logged in → Dashboard
            startActivity(Intent(this, DashboardActivity::class.java))
        } else {
            // Not logged in → Welcome
            startActivity(Intent(this, WelcomeActivity::class.java))
        }

        finish() // prevent going back to MainActivity
    }
}
