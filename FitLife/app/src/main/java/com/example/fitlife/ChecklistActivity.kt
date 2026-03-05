package com.example.fitlife

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONArray
import java.util.Calendar
import kotlin.math.abs

class ChecklistActivity : AppCompatActivity() {

    private lateinit var checklistContainer: LinearLayout
    private lateinit var tvProgress: TextView
    private val prefs by lazy { getSharedPreferences("checklist_prefs", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checklist)

        checklistContainer = findViewById(R.id.checklistContainer)
        tvProgress = findViewById(R.id.tvChecklistProgress)
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAddChecklist)
        val btnDelegate = findViewById<Button>(R.id.btnDelegateSms)

        handleDailyReset()
        loadChecklist()

        fabAdd.setOnClickListener {
            showAddChecklistDialog()
        }

        // 📤 DELEGATE CHECKLIST VIA SMS
        btnDelegate.setOnClickListener {
            val checklistText = StringBuilder("Workout Checklist:\n")

            getChecklistItems().forEach {
                checklistText.append("• ").append(it).append("\n")
            }

            val intent = Intent(this, DelegateSmsActivity::class.java)
            intent.putExtra("sms_text", checklistText.toString())
            startActivity(intent)
        }
    }

    // 🔁 DAILY RESET (only uncheck, keep items)
    private fun handleDailyReset() {
        val today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        val savedDay = prefs.getInt("saved_day", -1)

        if (savedDay != today) {
            prefs.edit()
                .remove("states")
                .putInt("saved_day", today)
                .apply()
        }
    }

    // 📥 LOAD ITEMS + STATES
    private fun loadChecklist() {
        checklistContainer.removeAllViews()

        val items = getChecklistItems()
        val states = getChecklistStates()

        items.forEachIndexed { index, text ->
            val checked = states.getOrNull(index) ?: false
            addCheckBox(text, checked)
        }

        updateProgress()
    }

    // ➕ ADD CHECKBOX WITH SWIPE DELETE
    private fun addCheckBox(text: String, checked: Boolean) {
        val cb = MaterialCheckBox(this)
        cb.text = text
        cb.textSize = 16f
        cb.setTextColor(getColor(android.R.color.white))
        cb.isChecked = checked
        cb.setPadding(12, 12, 12, 12)

        cb.setOnCheckedChangeListener { _, _ ->
            saveChecklistState()
            updateProgress()
        }

        // 👉 SWIPE TO DELETE
        cb.setOnTouchListener(object : View.OnTouchListener {
            var startX = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> startX = event.x
                    MotionEvent.ACTION_UP -> {
                        val deltaX = event.x - startX
                        if (abs(deltaX) > 200) {
                            deleteChecklistItem(cb)
                            return true
                        }
                    }
                }
                return false
            }
        })

        checklistContainer.addView(cb)
    }

    // 🗑 DELETE ITEM (UI + STORAGE)
    private fun deleteChecklistItem(cb: MaterialCheckBox) {
        val index = checklistContainer.indexOfChild(cb)
        if (index == -1) return

        checklistContainer.removeView(cb)

        val items = getChecklistItems().toMutableList()
        if (index < items.size) {
            items.removeAt(index)
            saveChecklistItems(items)
        }

        saveChecklistState()
        updateProgress()

        Toast.makeText(this, "Checklist item deleted", Toast.LENGTH_SHORT).show()
    }

    // 📝 ADD NEW ITEM
    private fun showAddChecklistDialog() {
        val input = android.widget.EditText(this)

        AlertDialog.Builder(this)
            .setTitle("Add Checklist Item")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val text = input.text.toString().trim()
                if (text.isNotEmpty()) {
                    val items = getChecklistItems().toMutableList()
                    items.add(text)
                    saveChecklistItems(items)

                    addCheckBox(text, false)
                    saveChecklistState()
                    updateProgress()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // 💾 SAVE STATES
    private fun saveChecklistState() {
        val states = JSONArray()
        for (i in 0 until checklistContainer.childCount) {
            val cb = checklistContainer.getChildAt(i) as MaterialCheckBox
            states.put(cb.isChecked)
        }
        prefs.edit().putString("states", states.toString()).apply()
    }

    // 📊 UPDATE PROGRESS
    private fun updateProgress() {
        var completed = 0
        val total = checklistContainer.childCount

        for (i in 0 until total) {
            val cb = checklistContainer.getChildAt(i) as MaterialCheckBox
            if (cb.isChecked) completed++
        }

        tvProgress.text = "Completed $completed / $total today"

        if (completed == total && total > 0) {
            Toast.makeText(this, "All tasks completed 🎉", Toast.LENGTH_SHORT).show()
        }
    }

    // 🗂 ITEMS
    private fun getChecklistItems(): List<String> {
        val json = prefs.getString("items", null)

        if (json != null) {
            val arr = JSONArray(json)
            return List(arr.length()) { arr.getString(it) }
        }

        val defaults = listOf(
            "Walk at least 5,000 steps",
            "Stretch for 10 minutes",
            "Drink enough water",
            "Sleep at least 7 hours"
        )

        saveChecklistItems(defaults)
        return defaults
    }

    private fun saveChecklistItems(items: List<String>) {
        val arr = JSONArray()
        items.forEach { arr.put(it) }
        prefs.edit().putString("items", arr.toString()).apply()
    }

    private fun getChecklistStates(): List<Boolean> {
        val json = prefs.getString("states", null) ?: return emptyList()
        val arr = JSONArray(json)
        return List(arr.length()) { arr.getBoolean(it) }
    }
}
