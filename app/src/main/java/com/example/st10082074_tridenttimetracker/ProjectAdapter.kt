package com.example.st10082074_tridenttimetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ProjectAdapter(private val projects: List<Project>) :
    RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_project, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projects[position]
        holder.bind(project)
    }

    override fun getItemCount(): Int {
        return projects.size
    }

    inner class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val projectNameTextView: TextView = itemView.findViewById(R.id.projectNameTextView)
        private val clientTextView: TextView = itemView.findViewById(R.id.clientTextView)
        private val totalHoursTextView: TextView = itemView.findViewById(R.id.totalHoursTextView)

        fun bind(project: Project) {
            projectNameTextView.text = project.projectName
            clientTextView.text = project.client
            totalHoursTextView.text = calculateTotalHours(project.startDate, project.endDate)
        }

        private fun calculateTotalHours(startDate: String?, endDate: String?): String {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            try {
                val start = dateFormat.parse(startDate!!)
                val end = dateFormat.parse(endDate!!)
                val milliseconds = end.time - start.time
                val totalSeconds = milliseconds / 1000
                val hours = totalSeconds / 3600
                val minutes = (totalSeconds % 3600) / 60
                return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return "Unknown"
        }
    }
}
