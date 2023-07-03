package com.example.st10082074_tridenttimetracker

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore


class ProjectsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var projectAdapter: ProjectAdapter
    private val projectsList: MutableList<Project> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_projects, container, false)
        recyclerView = view.findViewById(R.id.recyclerView)
        projectAdapter = ProjectAdapter(projectsList)
        recyclerView.adapter = projectAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        val loginId = arguments?.getString("loginId")
        fetchProjects(loginId)

        return view
    }

    private fun fetchProjects(loginId: String?) {
        val db = FirebaseFirestore.getInstance()
        db.collection("Projects")
            .whereEqualTo("LoginID", loginId)
            .orderBy("StartDate")
            .get()
            .addOnSuccessListener { documents ->
                projectsList.clear()
                for (document in documents) {
                    val projectName = document.getString("ProjectName")
                    val client = document.getString("Client")
                    val startDate = document.getString("StartDate")
                    val endDate = document.getString("EndDate")
                    val project = Project(projectName, client, startDate, endDate)
                    projectsList.add(project)
                }
                projectAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                // Handle any errors
            }
    }
}