package com.example.st10082074_tridenttimetracker

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
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
import java.util.*

@Suppress("NAME_SHADOWING")
class TasksFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter
    private val tasks: MutableList<Task> = mutableListOf()

    private lateinit var loginId: String // Class-level variable to store the login ID

    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Date range variables
    private lateinit var startDate: Date
    private lateinit var endDate: Date

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

        // Show tasks for the selected date range
        loadTasksForDateRange()

        // Show date range picker dialog on button click
        val selectDateRangeButton: Button = view.findViewById(R.id.dateRangeButton)
        selectDateRangeButton.setOnClickListener {
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
        firestore.collection("Goal")
            .document(loginId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
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
                    "loginId" to loginId,
                    "minGoal" to minGoal,
                    "maxGoal" to maxGoal
                )
                firestore.collection("Goal")
                    .document(loginId)
                    .set(goals)
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

    private fun loadTasksForDateRange() {
        // Perform the Firestore query with the selected date range
        // Adjust this query based on your Firestore data structure
        firestore.collection("tasks")
            .whereGreaterThanOrEqualTo("startDate", startDate)
            .whereLessThanOrEqualTo("startDate", endDate)
            .whereEqualTo("assignedTo", loginId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                tasks.clear()
                for (document in querySnapshot.documents) {
                    val task = document.toObject(Task::class.java)
                    task?.let { tasks.add(it) }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    requireContext(),
                    "Failed to load tasks: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
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
