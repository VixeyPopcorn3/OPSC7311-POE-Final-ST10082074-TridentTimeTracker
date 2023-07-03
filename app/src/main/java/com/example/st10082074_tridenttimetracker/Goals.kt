package com.example.st10082074_tridenttimetracker

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

/**
 * A simple [Fragment] subclass.
 * Use the [Goals.newInstance] factory method to
 * create an instance of this fragment.
 */
class Goals : Fragment() {

    private lateinit var calendarView: CalendarView
    private lateinit var colorTextView: TextView
    private lateinit var goldStarTextView: TextView
    private lateinit var streakTextView: TextView
    private lateinit var loginId: String // Class-level variable to store the login ID

    private val db = Firebase.firestore

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_goals, container, false)

        calendarView = view.findViewById(R.id.calendarView)
        colorTextView = view.findViewById(R.id.colorTextView)
        goldStarTextView = view.findViewById(R.id.goldStarTextView)
        streakTextView = view.findViewById(R.id.streakTextView)

        loginId = arguments?.getString("loginId") ?: ""

        displayGoldStar(loginId, streakTextView, goldStarTextView)

        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            val selectedDate = "$year-${month + 1}-$dayOfMonth" // Assuming month is zero-based


            if (loginId != null) {
                db.collection("Tasks")
                    .whereEqualTo("LoginID", loginId)
                    .whereEqualTo("StartDate", selectedDate)
                    .get()
                    .addOnSuccessListener { taskSnapshot ->
                        var totalHours = 0

                        for (task in taskSnapshot) {
                            val startTime = task.getString("StartTime")
                            val endTime = task.getString("EndTime")
                            if (startTime != null && endTime != null) {
                                totalHours += calculateHours(startTime, endTime)
                            }
                        }

                        db.collection("Goals")
                            .whereEqualTo("LoginID", loginId)
                            .get()
                            .addOnSuccessListener { goalSnapshot ->
                                if (!goalSnapshot.isEmpty) {
                                    val goal = goalSnapshot.documents[0]
                                    val minGoal = goal.getDouble("minGoal")
                                    val maxGoal = goal.getDouble("maxGoal")

                                    if (minGoal != null && maxGoal != null) {
                                        val message = buildToastMessage(
                                            selectedDate,
                                            minGoal,
                                            maxGoal,
                                            totalHours
                                        )
                                        showToast(message)

                                        val color = getColorForHours(totalHours, minGoal, maxGoal)
                                        setColorView(color)
                                    }
                                }
                            }
                    }
            }
        }

        return view
    }

    private fun calculateHours(startTime: String, endTime: String): Int {
        // Calculate the difference between start and end time in hours
        // Implement your own logic here, consider using a Date/Time library
        return 0
    }

    private fun buildToastMessage(
        date: String,
        minGoal: Double,
        maxGoal: Double,
        totalHours: Int
    ): String {
        val message =
            "Date: $date\nMin Goal: $minGoal hours\nMax Goal: $maxGoal hours\nTotal Hours: $totalHours"
        return message
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun getColorForHours(hours: Int, minGoal: Double, maxGoal: Double): Int {
        val greenColor = Color.parseColor("#00FF00")
        val orangeColor = Color.parseColor("#FFA500")
        val redColor = Color.parseColor("#FF0000")

        return when {
            hours >= minGoal && hours <= maxGoal -> greenColor
            hours < minGoal -> orangeColor
            else -> redColor
        }
    }

    private fun setColorView(color: Int) {
        colorTextView.setBackgroundColor(color)
    }
    private fun displayGoldStar(loginId: String, StreakTextView: TextView, GoldStarTextView: TextView) {

        val streakCollection = db.collection("Streak")

        val query = streakCollection.whereEqualTo("LoginID", loginId)

        query.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val goldStar = document.getString("GoldStar") ?: 0
                    val streak = document.getString("Streak") ?: 0

                    StreakTextView.text = "Current Streak is: $streak Days"
                    GoldStarTextView.text = "$goldStar Gold Stars"
                } else {
                    // The user's streak data doesn't exist in the Firestore
                    // Display an appropriate message or handle the situation
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors that occurred while fetching the data
                // Display an error message or handle the failure
            }
    }
}