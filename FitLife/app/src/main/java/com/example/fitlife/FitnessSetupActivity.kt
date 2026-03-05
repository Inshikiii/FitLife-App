package com.example.fitlife

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class FitnessSetupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fitness_setup)

        val etAge = findViewById<EditText>(R.id.etAge)
        val etWeight = findViewById<EditText>(R.id.etWeight)
        val etHeight = findViewById<EditText>(R.id.etHeight)
        val spGoal = findViewById<Spinner>(R.id.spGoal)
        val btnSave = findViewById<Button>(R.id.btnSave)

        // ===== SPINNER DATA =====
        val goals = arrayOf("Lose Weight", "Maintain", "Gain Muscle")
        spGoal.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            goals
        )

        btnSave.setOnClickListener {

            val ageText = etAge.text.toString().trim()
            val weightText = etWeight.text.toString().trim()
            val heightText = etHeight.text.toString().trim()
            val goal = spGoal.selectedItem.toString()

            // ===== EMPTY VALIDATION =====
            if (ageText.isEmpty()) {
                etAge.error = "Age is required"
                return@setOnClickListener
            }

            if (weightText.isEmpty()) {
                etWeight.error = "Weight is required"
                return@setOnClickListener
            }

            if (heightText.isEmpty()) {
                etHeight.error = "Height is required"
                return@setOnClickListener
            }

            val age = ageText.toIntOrNull()
            val weight = weightText.toFloatOrNull()
            val height = heightText.toIntOrNull()

            // ===== RANGE VALIDATION =====
            if (age == null || age <= 0 || age > 120) {
                etAge.error = "Enter a valid age (1–120)"
                return@setOnClickListener
            }

            if (weight == null || weight <= 0 || weight > 300) {
                etWeight.error = "Enter a valid weight"
                return@setOnClickListener
            }

            if (height == null || height <= 0 || height > 250) {
                etHeight.error = "Enter a valid height"
                return@setOnClickListener
            }

            // ===== SAVE USER FITNESS DATA =====
            val prefs = getSharedPreferences("fitness_prefs", MODE_PRIVATE)
            prefs.edit()
                .putInt("age", age)
                .putFloat("weight", weight)
                .putInt("height", height)
                .putString("goal", goal)
                .putBoolean("is_fitness_setup_done", true)
                .apply()

            Toast.makeText(this, "Fitness setup saved", Toast.LENGTH_SHORT).show()

            // Go to Home
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
    }
}
