package com.example.st10082074_tridenttimetracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [CompletedProjectFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CompletedProjectFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CompletedProjAdapter
    private lateinit var loginId: String // Class-level variable to store the login ID

    private val db = Firebase.firestore
    private val projectsCollection = db.collection("Projects")
    private val tasksCollection = db.collection("Tasks")
    private val completedProjects: MutableList<Completed> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_completed_project, container, false)
        recyclerView = view.findViewById(R.id.completedProjectsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = CompletedProjAdapter(completedProjects)
        recyclerView.adapter = adapter

        loginId = arguments?.getString("loginId") ?: ""

        // Query completed projects for the specific user
        projectsCollection
            .whereLessThanOrEqualTo("EndDate", getCurrentDate())
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val projectId = document.getString("ProjectID") ?: ""
                    val projectName = document.getString("Name") ?: ""
                    val projectDescription = document.getString("Description") ?: ""
                    val projectClient = document.getString("Client") ?: ""

                    // Calculate total hours worked on the project
                    tasksCollection
                        .whereEqualTo("ProjectID", projectId)
                        .get()
                        .addOnSuccessListener { taskQuerySnapshot ->
                            var totalHours = 0
                            for (taskDocument in taskQuerySnapshot.documents) {
                                val startTime = taskDocument.getLong("StartTime") ?: 0
                                val endTime = taskDocument.getLong("EndTime") ?: 0
                                totalHours += (endTime - startTime).toInt()
                            }

                            // Add completed project to the list
                            completedProjects.add(
                                Completed(
                                    projectId,
                                    projectName,
                                    projectDescription,
                                    projectClient,
                                    totalHours
                                )
                            )
                            adapter.notifyDataSetChanged()
                        }
                }
            }

        return view
    }

    // Helper function to get the current date in the format required by Firestore (YYYY-MM-DD)
    private fun getCurrentDate(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        return "$year-${String.format("%02d", month)}-${String.format("%02d", day)}"
    }
}