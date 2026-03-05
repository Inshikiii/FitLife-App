package com.example.fitlife

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "fitlife.db"
        private const val DATABASE_VERSION = 2

        const val TABLE_USERS = "users"
        const val COL_ID = "id"
        const val COL_NAME = "name"
        const val COL_EMAIL = "email"
        const val COL_PASSWORD = "password"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_USERS (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME TEXT,
                $COL_EMAIL TEXT UNIQUE,
                $COL_PASSWORD TEXT
            )
        """
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // ✅ Insert user with name
    fun insertUser(name: String, email: String, password: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_NAME, name)
            put(COL_EMAIL, email)
            put(COL_PASSWORD, password)
        }
        val result = db.insert(TABLE_USERS, null, values)
        return result != -1L
    }

    // ✅ Validate login
    fun checkUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE $COL_EMAIL=? AND $COL_PASSWORD=?",
            arrayOf(email, password)
        )
        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // ✅ Get user name by email
    fun getUserName(email: String): String {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_NAME FROM $TABLE_USERS WHERE $COL_EMAIL=?",
            arrayOf(email)
        )

        var name = "User"
        if (cursor.moveToFirst()) {
            name = cursor.getString(0)
        }
        cursor.close()
        return name
    }
}
