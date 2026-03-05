package com.example.fitlife

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar

class DashboardActivity : AppCompatActivity(), SensorEventListener {

    private val weeklyGoalKm = 7.0
    private val STEP_TO_KM = 0.0008

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var initialSteps = -1f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)



        // ✅ Check if fitness setup is completed
        val prefs = getSharedPreferences("fitness_prefs", MODE_PRIVATE)
        val isSetupDone = prefs.getBoolean("is_fitness_setup_done", false)

        if (!isSetupDone) {
            startActivity(Intent(this, FitnessSetupActivity::class.java))
            finish()
            return
        }



        val tvUserName = findViewById<TextView>(R.id.tvUserName)
        val btnStart = findViewById<Button>(R.id.btnStart)

        val session = SessionManager(this)
        val db = DatabaseHelper(this)

        val email = session.getEmail()
        val name = if (email != null) db.getUserName(email) else "User"

        // ✅ Show user name
        tvUserName.text = "Hello, $name 👋"

        // ✅ Weekly reset check
        handleWeeklyReset()

        // ✅ Load saved distance
        val distance = prefs.getFloat("weekly_distance", 0f)

        // ✅ Update UI
        updateDistance(distance.toDouble())
        updateWeeklySummary(distance.toDouble())

        // ✅ Step sensor
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        // ✅ Start button → Home
        btnStart.setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    // 🦶 Step tracking
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {

            if (initialSteps < 0) {
                initialSteps = event.values[0]
            }

            val stepsSinceStart = event.values[0] - initialSteps
            val distanceKm = stepsSinceStart * STEP_TO_KM

            val prefs = getSharedPreferences("fitness_prefs", MODE_PRIVATE)
            prefs.edit()
                .putFloat("weekly_distance", distanceKm.toFloat())
                .apply()

            updateDistance(distanceKm)
            updateWeeklySummary(distanceKm)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // 🔵 Progress ring + number animation
    private fun updateDistance(distanceKm: Double) {
        val targetProgress = ((distanceKm / weeklyGoalKm) * 100)
            .toInt()
            .coerceIn(0, 100)

        val progressBar = findViewById<ProgressBar>(R.id.distanceProgress)
        val tvDistance = findViewById<TextView>(R.id.tvDistance)

        ObjectAnimator.ofInt(progressBar, "progress", 0, targetProgress).apply {
            duration = 1200
            interpolator = DecelerateInterpolator()
            start()
        }

        ValueAnimator.ofFloat(0f, distanceKm.toFloat()).apply {
            duration = 1200
            addUpdateListener {
                tvDistance.text = String.format("%.2f km", it.animatedValue as Float)
            }
            start()
        }
    }

    // 🔁 Weekly reset
    private fun handleWeeklyReset() {
        val prefs = getSharedPreferences("fitness_prefs", MODE_PRIVATE)

        val currentWeek = Calendar.getInstance().get(Calendar.WEEK_OF_YEAR)
        val savedWeek = prefs.getInt("saved_week", -1)

        if (savedWeek != currentWeek) {
            prefs.edit()
                .putInt("saved_week", currentWeek)
                .putFloat("weekly_distance", 0f)
                .apply()
        }
    }



    private fun updateWeeklySummary(distanceKm: Double) {
        val daysPassed = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1
        val safeDays = if (daysPassed <= 0) 1 else daysPassed

        val averagePerDay = distanceKm / safeDays
        val goalPercent = ((distanceKm / weeklyGoalKm) * 100).toInt()

        findViewById<TextView>(R.id.tvWeeklyTotal)
            .text = String.format("Total: %.2f km", distanceKm)

        findViewById<TextView>(R.id.tvWeeklyAverage)
            .text = String.format("Avg/day: %.2f km", averagePerDay)

        findViewById<TextView>(R.id.tvWeeklyGoal)
            .text = String.format("Goal: %d%% completed", goalPercent)
    }
}
