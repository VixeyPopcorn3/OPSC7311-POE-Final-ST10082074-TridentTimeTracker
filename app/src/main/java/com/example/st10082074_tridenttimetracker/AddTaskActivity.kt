package com.example.st10082074_tridenttimetracker

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AddTaskActivity : AppCompatActivity() {
    private lateinit var editTextTaskName: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var editTextProjectName: EditText
    private lateinit var editTextStartDate: EditText
    private lateinit var editTextStartTime: EditText
    private lateinit var editTextEndTime: EditText

    private lateinit var buttonAddTask: Button
    private lateinit var buttonBack: Button

    private lateinit var firestore: FirebaseFirestore
    private lateinit var loginId: String

    private val calendar: Calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        firestore = FirebaseFirestore.getInstance()

        // Retrieve the loginId from the previous activity
        loginId = intent.getStringExtra("loginId") ?: ""

        // Initialize views
        editTextTaskName = findViewById(R.id.editTextTaskName)
        editTextDescription = findViewById(R.id.editTextDescription)
        editTextProjectName = findViewById(R.id.editTextProjectName)
        editTextStartDate = findViewById(R.id.editTextStartDate)
        editTextStartTime = findViewById(R.id.editTextStartTime)
        editTextEndTime = findViewById(R.id.editTextEndTime)

        buttonAddTask = findViewById(R.id.buttonAddTask)
        buttonBack = findViewById(R.id.buttonBack)

        // Set click listeners
        buttonAddTask.setOnClickListener { addTask() }
        buttonBack.setOnClickListener { backPressed() }

        // Set start date click listener
        editTextStartDate.setOnClickListener {
            showDatePickerDialog()
        }

        // Set start time click listener
        editTextStartTime.setOnClickListener {
            showTimePickerDialog(editTextStartTime)
        }

        // Set end time click listener
        editTextEndTime.setOnClickListener {
            showTimePickerDialog(editTextEndTime)
        }
    }

    private fun showDatePickerDialog() {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                calendar.set(year, monthOfYear, dayOfMonth)
                val selectedDate = calendar.time
                val formattedDate = dateFormatter.format(selectedDate)
                editTextStartDate.setText(formattedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun showTimePickerDialog(editText: EditText) {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _: TimePicker, hourOfDay: Int, minute: Int ->
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                calendar.set(Calendar.MINUTE, minute)
                val selectedTime = calendar.time
                val formattedTime = timeFormatter.format(selectedTime)
                editText.setText(formattedTime)
            },
            hour,
            minute,
            false
        )
        timePickerDialog.show()
    }

    private fun addTask() {
        val taskName = editTextTaskName.text.toString().trim()
        val description = editTextDescription.text.toString().trim()
        val projectName = editTextProjectName.text.toString().trim()
        val startDate = editTextStartDate.text.toString().trim()
        val startTime = editTextStartTime.text.toString().trim()
        val endTime = editTextEndTime.text.toString().trim()

        if (taskName.isEmpty() || description.isEmpty() || projectName.isEmpty() || startDate.isEmpty() ||
            startTime.isEmpty() || endTime.isEmpty()
        ) {
            // Display an error message if any field is empty
            showError("Please fill in all fields.")
            return
        }

        // Validate start and end time
        val startTimeMillis = timeFormatter.parse(startTime)?.time ?: 0L
        val endTimeMillis = timeFormatter.parse(endTime)?.time ?: 0L

        if (endTimeMillis <= startTimeMillis) {
            showError("End time should be after start time.")
            return
        }
        // Check if the project name exists in the Projects collection
        val projectsRef = firestore.collection("Projects")
        projectsRef.whereEqualTo("ProjectName", projectName)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    // Project name does not exist
                    showAddProjectOption()
                } else {
                    // Project name exists
                    val projectId = querySnapshot.documents[0].id
                    saveTaskToFirestore(projectId)
                }
            }
            .addOnFailureListener { exception ->
                showError("Failed to check project name: ${exception.message}")
            }
    }

    private fun showAddProjectOption() {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Project Not Found")
        alertDialog.setMessage("The project name doesn't exist. Do you want to add a new project?")
        alertDialog.setPositiveButton("Yes") { dialog, _ ->
            // Navigate to the AddProjectActivity
            val intent = Intent(this, AddProjectActivity::class.java)
            intent.putExtra("loginId", loginId)
            startActivity(intent)
            dialog.dismiss()
        }
        alertDialog.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        alertDialog.show()
    }

    private fun saveTaskToFirestore(projectId: String) {
        val taskName = editTextTaskName.text.toString().trim()
        val description = editTextDescription.text.toString().trim()
        val startDate = editTextStartDate.text.toString().trim()
        val startTime = editTextStartTime.text.toString().trim()
        val endTime = editTextEndTime.text.toString().trim()

        // Generate a random document ID
        val taskId = firestore.collection("Tasks").document().id

        // Create a task object
        val task = hashMapOf(
            "LoginID" to loginId,
            "TaskName" to taskName,
            "Description" to description,
            "ProjectID" to projectId,  // Use the project ID instead of project name
            "StartDate" to startDate,
            "StartTime" to startTime,
            "EndTime" to endTime
        )

        // Add the task to the Firestore database
        firestore.collection("Tasks").document(taskId)
            .set(task)
            .addOnSuccessListener {
                // Task added successfully
                backPressed() // Go back to the previous activity
            }
            .addOnFailureListener { exception ->
                // Error occurred while adding the task
                showError("Failed to add task: ${exception.message}")
            }
    }

    private fun showError(errorMessage: String) {
        // Display the error message
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }
    private fun backPressed() {
        // Handle the back button press

        startActivity(Intent(this@AddTaskActivity, MainPage::class.java).apply {
            putExtra("loginId", loginId)
        })
    }
}