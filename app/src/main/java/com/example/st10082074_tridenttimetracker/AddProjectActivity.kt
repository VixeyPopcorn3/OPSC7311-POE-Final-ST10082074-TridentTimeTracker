package com.example.st10082074_tridenttimetracker

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class AddProjectActivity : AppCompatActivity() {

    private lateinit var editTextDescription: EditText
    private lateinit var editTextClient: EditText
    private lateinit var editTextStartDate: EditText
    private lateinit var editTextEndDate: EditText
    private lateinit var editTextName:EditText

    private lateinit var buttonAddProject: Button
    private lateinit var buttonBack: Button

    private lateinit var firestore: FirebaseFirestore
    private lateinit var loginId: String

    private val calendar: Calendar = Calendar.getInstance()
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_project)

        firestore = FirebaseFirestore.getInstance()

        // Retrieve the loginId from the previous activity
        loginId = intent.getStringExtra("loginId") ?: ""

        // Initialize views
        editTextName = findViewById(R.id.editTextName)
        editTextDescription = findViewById(R.id.editTextDescription)
        editTextClient = findViewById(R.id.editTextClient)
        editTextStartDate = findViewById(R.id.editTextStartDate)
        editTextEndDate = findViewById(R.id.editTextEndDate)

        buttonAddProject = findViewById(R.id.buttonAddProject)
        buttonBack = findViewById(R.id.buttonBack)

        // Set click listeners
        buttonAddProject.setOnClickListener { addProject() }
        buttonBack.setOnClickListener { backPressed() }

        // Set start date click listener
        editTextStartDate.setOnClickListener {
            showDatePickerDialog(editTextStartDate)
        }

        // Set end date click listener
        editTextEndDate.setOnClickListener {
            showDatePickerDialog(editTextEndDate)
        }
    }

    private fun showDatePickerDialog(editText: EditText) {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                calendar.set(year, monthOfYear, dayOfMonth)
                val selectedDate = calendar.time
                val formattedDate = dateFormatter.format(selectedDate)
                editText.setText(formattedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun addProject() {
        val name = editTextName.text.toString().trim()
        val description = editTextDescription.text.toString().trim()
        val client = editTextClient.text.toString().trim()
        val startDate = editTextStartDate.text.toString().trim()
        val endDate = editTextEndDate.text.toString().trim()

        if (name.isEmpty() || description.isEmpty() || client.isEmpty() || startDate.isEmpty() || endDate.isEmpty()) {
            // Display an error message if any field is empty
            showError("Please fill in all fields.")
            return
        }

        // Create a random document ID
        val projectId = firestore.collection("Projects").document().id

        // Create a project object
        val project = hashMapOf(
            "LoginID" to loginId,
            "Name" to name,
            "Description" to description,
            "Client" to client,
            "StartDate" to startDate,
            "EndDate" to endDate
        )

        // Add the project to the Firestore database
        firestore.collection("Projects").document(projectId)
            .set(project)
            .addOnSuccessListener {
                // Project added successfully
                backPressed() // Go back to the previous activity
            }
            .addOnFailureListener { exception ->
                // Error occurred while adding the project
                showError("Failed to add project: ${exception.message}")
            }
    }

    private fun showError(errorMessage: String) {
        // Display the error message
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }

    private fun backPressed() {
        // Handle the back button press
        startActivity(Intent(this@AddProjectActivity, MainPage::class.java).apply {
            putExtra("loginId", loginId)
        })
    }
}
