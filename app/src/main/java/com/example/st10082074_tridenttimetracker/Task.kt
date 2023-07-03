package com.example.st10082074_tridenttimetracker

import java.sql.Time
import java.util.*
data class Task(
    val description: String,
    val taskName: String,
    val projectID: String,
    val startDate: Date,
    val startTime: Time,
    val endTime: Time,
    val loginID: String,
    val photoUrl: String?,
    val hasPhoto: Boolean = false

){
    // Empty constructor required for Firestore deserialization
    constructor() : this(
        "", "", "", Date(), Time(0), Time(0), "", null, false
    )
}
