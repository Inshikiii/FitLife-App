package com.example.fitlife

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import org.json.JSONArray
import org.json.JSONObject

class HomeActivity : AppCompatActivity() {

    private lateinit var routineList: MutableList<Routine>
    private lateinit var routineAdapter: RoutineAdapter
    private lateinit var recyclerView: RecyclerView

    // ===== UNDO SUPPORT =====
    var recentlyDeletedRoutine: Routine? = null
    var recentlyDeletedPosition: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        routineList = mutableListOf()

        recyclerView = findViewById(R.id.rvRoutines)
        recyclerView.layoutManager = LinearLayoutManager(this)

        routineAdapter = RoutineAdapter(routineList) {
            saveRoutines()
            updateEmptyState()
        }
        recyclerView.adapter = routineAdapter

        loadRoutines()

        // ================= ADD ROUTINE =================
        findViewById<FloatingActionButton>(R.id.fabAddRoutine).setOnClickListener {
            startActivityForResult(
                Intent(this, CreateRoutineActivity::class.java),
                100
            )
        }

        // ================= SWIPE =================
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (position == RecyclerView.NO_POSITION) return

                if (direction == ItemTouchHelper.LEFT) {
                    recentlyDeletedRoutine = routineList[position]
                    recentlyDeletedPosition = position

                    routineList.removeAt(position)
                    routineAdapter.notifyItemRemoved(position)
                    saveRoutines()
                    updateEmptyState()

                    showUndoSnackbar()
                } else {
                    routineList[position].completed = true
                    routineAdapter.notifyItemChanged(position)
                    saveRoutines()
                }
            }
        }

        ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView)

        // ================= BOTTOM NAV =================
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_home

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
                R.id.nav_checklist -> {
                    startActivity(Intent(this, ChecklistActivity::class.java))
                    true
                }
                R.id.nav_map -> {
                    startActivity(Intent(this, MapActivity::class.java))
                    true
                }
                R.id.nav_settings -> {
                    startActivity(Intent(this, SettingsActivity::class.java))
                    true
                }
                else -> false
            }
        }

        updateEmptyState()
    }

    // ================= UNDO =================
    fun showUndoSnackbar() {
        Snackbar.make(
            findViewById(android.R.id.content),
            "Routine deleted",
            Snackbar.LENGTH_LONG
        ).setAction("UNDO") {
            recentlyDeletedRoutine?.let {
                routineList.add(recentlyDeletedPosition, it)
                routineAdapter.notifyItemInserted(recentlyDeletedPosition)
                saveRoutines()
                updateEmptyState()
            }
        }.show()
    }

    // ================= RECEIVE NEW ROUTINE =================
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            val name = data.getStringExtra("name") ?: return
            val info = data.getStringExtra("info") ?: return

            // Exercises passed as JSON string
            val exerciseJson = data.getStringExtra("exercises") ?: "[]"
            val exerciseArray = JSONArray(exerciseJson)
            val exercises = mutableListOf<Exercise>()

            for (i in 0 until exerciseArray.length()) {
                val obj = exerciseArray.getJSONObject(i)
                exercises.add(
                    Exercise(
                        obj.getString("name"),
                        obj.getString("equipment"),
                        obj.getString("instructions")
                    )
                )
            }

            routineList.add(Routine(name, info, exercises))
            routineAdapter.notifyItemInserted(routineList.size - 1)
            saveRoutines()
            updateEmptyState()
        }
    }

    // ================= SAVE =================
    private fun saveRoutines() {
        val prefs = getSharedPreferences("fitlife_prefs", MODE_PRIVATE)
        val array = JSONArray()

        for (routine in routineList) {
            val obj = JSONObject()
            obj.put("name", routine.name)
            obj.put("info", routine.info)
            obj.put("completed", routine.completed)

            val exerciseArray = JSONArray()
            for (exercise in routine.exercises) {
                val exObj = JSONObject()
                exObj.put("name", exercise.name)
                exObj.put("equipment", exercise.equipment)
                exObj.put("instructions", exercise.instructions)
                exerciseArray.put(exObj)
            }

            obj.put("exercises", exerciseArray)
            array.put(obj)
        }

        prefs.edit().putString("routines", array.toString()).apply()
    }

    // ================= LOAD =================
    private fun loadRoutines() {
        routineList.clear()

        val prefs = getSharedPreferences("fitlife_prefs", MODE_PRIVATE)
        val array = JSONArray(prefs.getString("routines", "[]"))

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)

            val exerciseArray = obj.getJSONArray("exercises")
            val exercises = mutableListOf<Exercise>()

            for (j in 0 until exerciseArray.length()) {
                val ex = exerciseArray.getJSONObject(j)
                exercises.add(
                    Exercise(
                        ex.getString("name"),
                        ex.getString("equipment"),
                        ex.getString("instructions")
                    )
                )
            }

            routineList.add(
                Routine(
                    obj.getString("name"),
                    obj.getString("info"),
                    exercises,
                    obj.getBoolean("completed")
                )
            )
        }

        routineAdapter.notifyDataSetChanged()
    }

    // ================= EMPTY STATE =================
    private fun updateEmptyState() {
        val emptyText = findViewById<TextView>(R.id.tvEmptyState)

        if (routineList.isEmpty()) {
            emptyText.visibility = View.VISIBLE
            recyclerView.visibility = View.GONE
        } else {
            emptyText.visibility = View.GONE
            recyclerView.visibility = View.VISIBLE
        }
    }
}
