package com.example.st10082074_tridenttimetracker

import java.sql.Time
import java.util.*
data class Task(
    val description: String,
    val taskName: String,
    val projectName: String,
    val startDate: Date,
    val startTime: Time,
    val endTime: Time,
    val loginId: String,
    val photoUrl: String?,
    val hasPhoto: Boolean = false
)
