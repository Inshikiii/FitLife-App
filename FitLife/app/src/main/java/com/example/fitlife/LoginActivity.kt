package com.example.fitlife

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)

        // ✅ ADD THIS
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)

        val db = DatabaseHelper(this)
        val session = SessionManager(this)

        // 🔹 Login button logic (UNCHANGED)
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (db.checkUser(email, password)) {
                session.login()
                session.saveEmail(email)
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
            }
            else {
                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }

        // 🔹 Sign-up text logic (SEPARATE)
        tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}
