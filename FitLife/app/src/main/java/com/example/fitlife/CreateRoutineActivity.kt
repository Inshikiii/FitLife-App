package com.example.fitlife

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONObject

class CreateRoutineActivity : AppCompatActivity() {

    private val exercises = mutableListOf<Exercise>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_routine)

        // ===== ROUTINE FIELDS =====
        val etRoutineName = findViewById<EditText>(R.id.etRoutineName)
        val etRoutineInfo = findViewById<EditText>(R.id.etRoutineInfo)
        val btnSaveRoutine = findViewById<Button>(R.id.btnSaveRoutine)

        // ===== EXERCISE FIELDS =====
        val etExerciseName = findViewById<EditText>(R.id.etExerciseName)
        val etEquipment = findViewById<EditText>(R.id.etEquipment)
        val etInstructions = findViewById<EditText>(R.id.etInstructions)
        val btnAddExercise = findViewById<Button>(R.id.btnAddExercise)

        // ===== ADD EXERCISE =====
        btnAddExercise.setOnClickListener {
            val name = etExerciseName.text.toString().trim()
            val equipment = etEquipment.text.toString().trim()
            val instructions = etInstructions.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Exercise name required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            exercises.add(
                Exercise(name, equipment, instructions)
            )

            etExerciseName.text.clear()
            etEquipment.text.clear()
            etInstructions.text.clear()

            Toast.makeText(this, "Exercise added", Toast.LENGTH_SHORT).show()
        }

        // ===== SAVE ROUTINE =====
        btnSaveRoutine.setOnClickListener {
            val routineName = etRoutineName.text.toString().trim()
            val routineInfo = etRoutineInfo.text.toString().trim()

            if (routineName.isEmpty() || routineInfo.isEmpty()) {
                Toast.makeText(this, "Fill all routine details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Convert exercises to JSON
            val exerciseArray = JSONArray()
            for (exercise in exercises) {
                val obj = JSONObject()
                obj.put("name", exercise.name)
                obj.put("equipment", exercise.equipment)
                obj.put("instructions", exercise.instructions)
                exerciseArray.put(obj)
            }

            val resultIntent = Intent()
            resultIntent.putExtra("name", routineName)
            resultIntent.putExtra("info", routineInfo)
            resultIntent.putExtra("exercises", exerciseArray.toString())

            setResult(RESULT_OK, resultIntent)
            finish()
        }
    }
}
