package com.example.st10082074_tridenttimetracker

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.sql.Time
import java.text.SimpleDateFormat
import java.util.*

@Suppress("NAME_SHADOWING")
class TasksFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private val tasks: MutableList<Task> = mutableListOf()

    private lateinit var loginId: String // Class-level variable to store the login ID

    private val db = Firebase.firestore

    // Date range variables
    private lateinit var startDate: Date
    private lateinit var endDate: Date

    private var clicked = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tasks, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = TaskAdapter(tasks)
        recyclerView.adapter = adapter
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Retrieve the login ID from the arguments bundle
        loginId = arguments?.getString("loginId") ?: ""

        // Initialize the start and end dates with current date
        val calendar = Calendar.getInstance()
        startDate = calendar.time
        endDate = calendar.time

        loadTask()
        // Show tasks for the selected date range
        //loadTasksForDateRange()

        // Show date range picker dialog on button click
        val selectDateRangeButton: Button = view.findViewById(R.id.dateRangeButton)
        selectDateRangeButton.setOnClickListener {
            clicked = true
            showDatePickerDialog()

        }
        // Add task button
        val btnAddTask: Button = view.findViewById(R.id.btnAddTask)
        btnAddTask.setOnClickListener {
            val addTaskIntent = Intent(requireContext(), AddTaskActivity::class.java)
            addTaskIntent.putExtra("loginId", loginId)
            startActivity(addTaskIntent)
        }
        // Goal input fields
        val minGoalEditText: EditText = view.findViewById(R.id.etMinGoal)
        val maxGoalEditText: EditText = view.findViewById(R.id.etMaxGoal)
        val saveGoalsButton: Button = view.findViewById(R.id.btnSaveGoals)

        // Retrieve and display the user's current goals (if available)
        db.collection("Goal")
            .whereEqualTo("LoginID", loginId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documentSnapshot = querySnapshot.documents[0]
                    val minGoal = documentSnapshot.getDouble("minGoal")
                    val maxGoal = documentSnapshot.getDouble("maxGoal")
                    minGoalEditText.setText(minGoal?.toString())
                    maxGoalEditText.setText(maxGoal?.toString())
                }
            }
        // Save goals button click listener
        saveGoalsButton.setOnClickListener {
            val minGoal = minGoalEditText.text.toString().toDoubleOrNull()
            val maxGoal = maxGoalEditText.text.toString().toDoubleOrNull()

            if (minGoal != null && maxGoal != null) {
                // Update the goals in Firestore
                val goals = hashMapOf(
                    "LoginID" to loginId,
                    "minGoal" to minGoal,
                    "maxGoal" to maxGoal
                )
                db.collection("Goal")
                    .add(goals)
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            "Goals saved successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(
                            requireContext(),
                            "Failed to save goals: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Invalid goal values",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        adapter.handleActivityResult(requestCode, resultCode, data)
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val startYear = calendar.get(Calendar.YEAR)
        val startMonth = calendar.get(Calendar.MONTH)
        val startDay = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                val startCalendar = Calendar.getInstance()
                startCalendar.set(year, month, dayOfMonth)
                startDate = startCalendar.time

                val endYear = calendar.get(Calendar.YEAR)
                val endMonth = calendar.get(Calendar.MONTH)
                val endDay = calendar.get(Calendar.DAY_OF_MONTH)

                val endDatePickerDialog = DatePickerDialog(
                    requireContext(),
                    { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                        val endCalendar = Calendar.getInstance()
                        endCalendar.set(year, month, dayOfMonth)
                        endDate = endCalendar.time

                        // Show tasks for the selected date range
                        loadTasksForDateRange()
                    },
                    endYear,
                    endMonth,
                    endDay
                )

                endDatePickerDialog.show()
            },
            startYear,
            startMonth,
            startDay
        )

        datePickerDialog.show()
    }

    private fun loadTask() {

        db.collection("Tasks")
            .whereEqualTo("LoginID", loginId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                tasks.clear()
                for (document in querySnapshot.documents) {
                    val description = document.getString("Description")
                    val taskName = document.getString("TaskName")
                    val projectID = document.getString("ProjectID")
                    val startDateString = document.getString("StartDate")
                    val startTimeString = document.getString("StartTime")
                    val endTimeString = document.getString("EndTime")
                    val loginID = document.getString("LoginID")
                    val photoUrl = document.getString("photoUrl") ?: ""
                    val hasPhoto = document.getBoolean("hasPhoto") ?: false

                    val startDate = startDateString?.toDate() ?: Date()
                    val startTime = startTimeString?.toTime() ?: Time(0)
                    val endTime = endTimeString?.toTime() ?: Time(0)

                    val task = Task(
                        description = description ?: "",//string
                        taskName = taskName ?: "",//string
                        projectID = projectID ?: "",//string
                        startDate = startDate ?: Date(),//needs to be date
                        startTime = startTime ?: Time(0),//needs to be time
                        endTime = endTime ?: Time(0),//needs to be time
                        loginID = loginID ?: "",//string
                        photoUrl = photoUrl,//string?
                        hasPhoto = hasPhoto//boolean
                    )

                    tasks.add(task)
                }
                    //val task = document.toObject(Task::class.java)
                    //task?.let { tasks.add(it) }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Failed to load tasks: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("TasksFragment", "Error loading tasks", exception)
            }
    }
    private fun loadTasksForDateRange() {//startTime = (startTime ?: Time(0)) as Time
        // Perform the Firestore query with the selected date range
        db.collection("Tasks")
            .whereEqualTo("LoginID", loginId)
            .whereGreaterThanOrEqualTo("startDate", startDate)
            .whereLessThanOrEqualTo("startDate", endDate)
            .get()
            .addOnSuccessListener { querySnapshot ->
                tasks.clear()
                for (document in querySnapshot.documents) {
                    val description = document.getString("Description")
                    val taskName = document.getString("TaskName")
                    val projectID = document.getString("ProjectID")
                    val startDateString = document.getString("StartDate")
                    val startTimeString = document.getString("StartTime")
                    val endTimeString = document.getString("EndTime")
                    val loginID = document.getString("LoginID")
                    val photoUrl = document.getString("photoUrl") ?: ""
                    val hasPhoto = document.getBoolean("hasPhoto") ?: false

                    val startDate = startDateString?.toDate() ?: Date()
                    val startTime = startTimeString?.toTime() ?: Time(0)
                    val endTime = endTimeString?.toTime() ?: Time(0)

                    val task = Task(
                        description = description ?: "",//string
                        taskName = taskName ?: "",//string
                        projectID = projectID ?: "",//string
                        startDate = startDate ?: Date(),//needs to be date
                        startTime = startTime ?: Time(0) ,//needs to be time
                        endTime = endTime ?: Time(0),//needs to be time
                        loginID = loginID ?: "",//string
                        photoUrl = photoUrl,//string?
                        hasPhoto = hasPhoto//boolean
                    )

                    tasks.add(task)
                    //val task = document.toObject(Task::class.java)
                    //task?.let { tasks.add(it) }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Failed to load tasks: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e("TasksFragment", "Error loading tasks", exception)
            }
    }
    fun String.toDate(): Date? {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return try {
            format.parse(this)
        } catch (e: Exception) {
            null
        }
    }

    fun String.toTime(): Time? {
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return try {
            val date = format.parse(this)
            Time(date.time)
        } catch (e: Exception) {
            null
        }
    }
    companion object {
        fun newInstance(loginId: String): TasksFragment {
            val fragment = TasksFragment()
            val args = Bundle()
            args.putString("loginId", loginId)
            fragment.arguments = args
            return fragment
        }
    }
}
