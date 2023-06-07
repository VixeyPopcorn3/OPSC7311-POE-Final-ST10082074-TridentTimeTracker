package com.example.st10082074_tridenttimetracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * A simple [Fragment] subclass.
 * Use the [CalculationsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CalculationsFragment : Fragment() {
    private lateinit var loginId: String
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_calculations, container, false)

        // Set up the ListView and adapter
        val listViewProjects: ListView = view.findViewById(R.id.listViewProjects)
        val adapter = CalculationsAdapter(requireContext())
        listViewProjects.adapter = adapter

        // Get the loginId from the arguments
        val bundle = arguments
        loginId = bundle?.getString("loginId") ?: ""

        // Initialize Firestore
        firestore = FirebaseFirestore.getInstance()

        // Calculate total hours for projects
        calculateTotalHoursForProjects()

        return view
    }

    private fun calculateTotalHoursForProjects() {
        // Query Firestore for projects with the specified loginId
        firestore.collection("Projects")
            .whereEqualTo("LoginID", loginId)
            .get()
            .addOnSuccessListener { projectsQuerySnapshot ->
                val projects = projectsQuerySnapshot.documents

                // Map to store project names and total hours
                val projectHoursMap = mutableMapOf<String, Int>()

                // Loop through each project
                for (project in projects) {
                    val projectName = project.getString("ProjectName")

                    // Query Firestore for tasks linked to the current project
                    if (projectName != null) {
                        firestore.collection("Tasks")
                            .whereEqualTo("LoginID", loginId)
                            .whereEqualTo("ProjectName", projectName)
                            .get()
                            .addOnSuccessListener { tasksQuerySnapshot ->
                                val tasks = tasksQuerySnapshot.documents

                                var totalHours = 0
                                for (task in tasks) {
                                    val startTime = task.getString("StartTime")
                                    val endTime = task.getString("EndTime")
                                    val hoursDiff = calculateHoursDifference(startTime, endTime)
                                    totalHours += hoursDiff
                                }

                                projectHoursMap[projectName] = totalHours

                                if (projectHoursMap.size == projects.size) {
                                    // All projects have been processed, do something with the project-hours map
                                    for ((project, hours) in projectHoursMap) {
                                        println("Total hours for $project: $hours")
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                // Handle failure
                                println("Error getting tasks: $exception")
                            }
                    }
                }
            }
            .addOnFailureListener { exception ->
                // Handle failure
                println("Error getting projects: $exception")
            }
    }
    private fun calculateHoursDifference(startTime: String?, endTime: String?): Int {
        // Define date format to parse start and end times
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        try {
            // Parse start and end times into Date objects
            val startDate = timeFormat.parse(startTime)
            val endDate = timeFormat.parse(endTime)

            // Calculate the difference between start and end times in milliseconds
            val durationInMillis = endDate.time - startDate.time

            // Convert milliseconds to hours
            val hours = TimeUnit.MILLISECONDS.toHours(durationInMillis)

            return hours.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return 0
    }
}