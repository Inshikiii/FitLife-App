package com.example.fitlife

import android.content.Context
import android.content.SharedPreferences


class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("FitLife_prefs", Context.MODE_PRIVATE)

    // Save user credentials (Sign Up)
    fun saveUser(name: String, email: String, password: String) {
        prefs.edit()
            .putString("name", name)
            .putString("email", email)
            .putString("password", password)
            .apply()
    }

    // Validate login (Login)
    fun isValidLogin(email: String, password: String): Boolean {
        val savedEmail = prefs.getString("email", null)
        val savedPassword = prefs.getString("password", null)
        return email == savedEmail && password == savedPassword
    }

    // Login state
    fun login() {
        prefs.edit().putBoolean("logged_in", true).apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("logged_in", false)
    }

    fun logout() {
        prefs.edit().putBoolean("logged_in", false).apply()
    }

    fun saveEmail(email: String) {
        prefs.edit().putString("user_email", email).apply()
    }

    fun getEmail(): String? {
        return prefs.getString("user_email", null)
    }
}
