package com.example.fitlife

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RoutineAdapter(
    private val routines: MutableList<Routine>,
    private val onDataChanged: () -> Unit
) : RecyclerView.Adapter<RoutineAdapter.RoutineViewHolder>() {

    inner class RoutineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvRoutineName)
        val tvInfo: TextView = view.findViewById(R.id.tvRoutineInfo)
        val btnDone: ImageButton = view.findViewById(R.id.btnDone)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
        val btnDelegate: ImageButton = view.findViewById(R.id.btnDelegate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_routine, parent, false)
        return RoutineViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoutineViewHolder, position: Int) {
        val routine = routines[position]

        // ===== DISPLAY ROUTINE =====
        holder.tvName.text = routine.name

        val exercisesText =
            if (routine.exercises.isNotEmpty()) {
                routine.exercises.joinToString("\n") {
                    "• ${it.name} (${it.equipment})"
                }
            } else {
                "• No exercises added"
            }

        holder.tvInfo.text =
            "${routine.info}\n\nExercises:\n$exercisesText"

        // ===== DONE STATE =====
        if (routine.completed) {
            holder.tvName.alpha = 0.5f
            holder.tvName.paintFlags =
                holder.tvName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.btnDone.setImageResource(android.R.drawable.checkbox_on_background)
        } else {
            holder.tvName.alpha = 1f
            holder.tvName.paintFlags =
                holder.tvName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.btnDone.setImageResource(android.R.drawable.checkbox_off_background)
        }

        // ===== TOGGLE DONE =====
        holder.btnDone.setOnClickListener {
            routine.completed = !routine.completed
            notifyItemChanged(position)
            onDataChanged()
        }

        // ===== EDIT ROUTINE + EXERCISES =====
        holder.btnEdit.setOnClickListener {
            val context = holder.itemView.context

            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(48, 24, 48, 0)
            }

            val etName = EditText(context).apply {
                hint = "Routine name"
                setText(routine.name)
            }

            val etInfo = EditText(context).apply {
                hint = "Routine info"
                setText(routine.info)
            }

            layout.addView(etName)
            layout.addView(etInfo)

            val exerciseInputs = mutableListOf<Pair<EditText, EditText>>()

            routine.exercises.forEach { exercise ->
                val etExercise = EditText(context).apply {
                    hint = "Exercise name"
                    setText(exercise.name)
                }

                val etEquipment = EditText(context).apply {
                    hint = "Equipment"
                    setText(exercise.equipment)
                }

                layout.addView(etExercise)
                layout.addView(etEquipment)

                exerciseInputs.add(etExercise to etEquipment)
            }

            AlertDialog.Builder(context)
                .setTitle("Edit Routine & Exercises")
                .setView(layout)
                .setPositiveButton("Save") { _, _ ->
                    routine.name = etName.text.toString()
                    routine.info = etInfo.text.toString()

                    exerciseInputs.forEachIndexed { index, pair ->
                        routine.exercises[index].name = pair.first.text.toString()
                        routine.exercises[index].equipment = pair.second.text.toString()
                    }

                    notifyItemChanged(position)
                    onDataChanged()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // ===== DELETE WITH UNDO =====
        holder.btnDelete.setOnClickListener {
            val context = holder.itemView.context
            if (context is HomeActivity) {
                context.recentlyDeletedRoutine = routine
                context.recentlyDeletedPosition = position
            }

            routines.removeAt(position)
            notifyItemRemoved(position)
            onDataChanged()

            if (context is HomeActivity) {
                context.showUndoSnackbar()
            }
        }

        // ===== 📤 SHARE VIA WHATSAPP / TELEGRAM / SMS / EMAIL =====
        holder.btnDelegate.setOnClickListener {
            val context = holder.itemView.context

            val exercisesForShare =
                if (routine.exercises.isNotEmpty()) {
                    routine.exercises.joinToString("\n") {
                        "- ${it.name} (${it.equipment})"
                    }
                } else {
                    "- No exercises added"
                }

            val message = """
🏋️ FitLife Workout Routine

Routine: ${routine.name}
Details: ${routine.info}

Exercises:
$exercisesForShare
            """.trimIndent()

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }

            context.startActivity(
                Intent.createChooser(
                    shareIntent,
                    "Share routine via"
                )
            )
        }
    }

    override fun getItemCount(): Int = routines.size
}
