package com.example.st10082074_tridenttimetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CompletedProjAdapter(private val completedProjectFragment: List<Completed>) : RecyclerView.Adapter<CompletedProjAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_completed, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val completedProject = completedProjectFragment[position]
        holder.projectName.text = completedProject.name
        holder.projectDescription.text = completedProject.description
        holder.projectClient.text = completedProject.client
        holder.totalHours.text = completedProject.totalHours.toString()
    }

    override fun getItemCount(): Int {
        return completedProjectFragment.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val projectName: TextView = itemView.findViewById(R.id.textViewProjectName)
        val projectDescription: TextView = itemView.findViewById(R.id.textViewProjectDescription)
        val projectClient: TextView = itemView.findViewById(R.id.textViewProjectClient)
        val totalHours: TextView = itemView.findViewById(R.id.textViewTotalHours)
    }
}