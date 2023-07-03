package com.example.st10082074_tridenttimetracker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*

class TaskAdapter(private val tasks: List<Task>) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskName: TextView = itemView.findViewById(R.id.TaskNameTextView)
        val projectName: TextView = itemView.findViewById(R.id.ProjectNameTextView)
        val totalHours: TextView = itemView.findViewById(R.id.TotalHoursTextView)
        val taskPhoto: ImageView = itemView.findViewById(R.id.TaskPhoto)
        val addPhotoButton: Button = itemView.findViewById(R.id.addPhotoButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.taskName.text = task.taskName
        holder.projectName.text = task.projectID
        holder.totalHours.text = calculateTotalHours(task)

        val db = Firebase.firestore
        val projectsCollection = db.collection("Projects")
        //val projectRef = projectsCollection.document(task.projectID)
        .whereEqualTo("ProjectID", task.projectID)
        projectsCollection.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {

                    if (!querySnapshot.isEmpty) {
                        val projectName = document.getString("projectName")
                        holder.projectName.text = projectName
                    }else {
                        holder.projectName.text = "Unknown project"
                    }
                }
                }

            .addOnFailureListener { exception ->
                // Handle failure to fetch project name
                holder.projectName.text = "Error loading project name"
                Log.e("TaskAdapter", "Error fetching project name", exception)
            }

        // Handle add photo button visibility
        if (task.hasPhoto || task.photoUrl != null) {
            holder.addPhotoButton.visibility = View.GONE
        } else {
            holder.addPhotoButton.visibility = View.VISIBLE
        }

        // Handle task photo visibility and loading
        if (task.hasPhoto && task.photoUrl != null) {
            holder.taskPhoto.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(task.photoUrl)
                .into(holder.taskPhoto)
        } else {
            holder.taskPhoto.visibility = View.GONE
        }

        // Add click listeners or other customizations here
        holder.addPhotoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            val activity = holder.itemView.context as? Activity
            activity?.startActivityForResult(intent, position)
        }
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    private fun calculateTotalHours(task: Task): String {
        //val format = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())

        //val startDate = format.parse("${task.startDate} ${task.startTime}")
        //val endDate = format.parse("${task.startDate} ${task.endTime}")

        val format1 = SimpleDateFormat("HH:mm", Locale.getDefault())

        val startTime = format1.parse("${task.startTime}")
        val endTime = format1.parse("${task.endTime}")
        val durationMillis = endTime.time - startTime.time
        val durationHours = durationMillis / (1000 * 60 * 60)

        //val durationMillis = endDate.time - startDate.time
        //val durationHours = durationMillis / (1000 * 60 * 60)

        return "$durationHours hours"
    }
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            val selectedImageUri: Uri? = data.data
            val position = requestCode

            if (selectedImageUri != null) {
                val task = tasks[position]
                uploadPhoto(task, selectedImageUri)
            }
        }
    }

    private fun uploadPhoto(task: Task, imageUri: Uri) {
        // Upload the selected image URI to Firestore
        // Adjust this code based on your Firestore data structure and storage configuration
        val db = Firebase.firestore
        val usersCollection = db.collection("Tasks")
        val photoRef = usersCollection.document(task.loginID)
        val photoUrl = imageUri.toString()

        photoRef
            .update("photoUrl", photoUrl, "hasPhoto", true)
            .addOnSuccessListener {
                    //"Photo added successfully",
            }
            .addOnFailureListener { exception ->
                    //"Failed to add photo
            }
    }
}
