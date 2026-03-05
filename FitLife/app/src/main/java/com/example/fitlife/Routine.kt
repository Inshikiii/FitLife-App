package com.example.fitlife

data class Routine(
    var name: String,
    var info: String,
    var exercises: MutableList<Exercise> = mutableListOf(),
    var completed: Boolean = false
)
