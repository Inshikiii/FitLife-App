package com.example.fitlife

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import android.view.View
import android.app.AlertDialog


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val session = SessionManager(this)
        val prefs = getSharedPreferences("fitlife_prefs", MODE_PRIVATE)




        // ===== ABOUT APP =====
        findViewById<TextView>(R.id.btnAbout).setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        // ===== NOTIFICATIONS SWITCH =====
        val notificationSwitch =
            findViewById<SwitchMaterial>(R.id.switchNotifications)

        notificationSwitch.isChecked =
            prefs.getBoolean("notifications", true)

        notificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("notifications", isChecked).apply()
            Toast.makeText(
                this,
                "Notification setting updated",
                Toast.LENGTH_SHORT
            ).show()
        }
        // ===== PRIVACY & PERMISSIONS ACTIONS =====

// Data Usage
        findViewById<View>(R.id.btnDataUsage).setOnClickListener {
            Toast.makeText(
                this,
                "Your fitness data is stored securely on this device.",
                Toast.LENGTH_LONG
            ).show()
        }

// App Permissions
        findViewById<View>(R.id.btnAppPermissions).setOnClickListener {
            val intent = Intent(
                android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            )
            intent.data = android.net.Uri.parse("package:$packageName")
            startActivity(intent)
        }

// Clear App Data
        findViewById<View>(R.id.btnClearData).setOnClickListener {

            AlertDialog.Builder(this)
                .setTitle("Clear App Data")
                .setMessage("This will remove all your data and reset the app. Continue?")
                .setPositiveButton("Clear") { _, _ ->

                    // Clear SharedPreferences
                    getSharedPreferences("fitlife_prefs", MODE_PRIVATE)
                        .edit()
                        .clear()
                        .apply()

                    // Go to Login screen
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags =
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }


// Privacy Policy
        findViewById<View>(R.id.btnPrivacyPolicy).setOnClickListener {
            Toast.makeText(
                this,
                "Privacy Policy coming soon",
                Toast.LENGTH_SHORT
            ).show()
        }

// Session & Security
        findViewById<View>(R.id.btnSessionInfo).setOnClickListener {
            Toast.makeText(
                this,
                "You are logged in securely on this device",
                Toast.LENGTH_SHORT
            ).show()
        }




        // ===== LOGOUT (FIXED) =====
        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {

            session.logout() // ✅ clears logged_in flag only

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }
    }
}
