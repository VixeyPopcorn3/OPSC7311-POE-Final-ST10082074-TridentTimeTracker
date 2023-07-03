package com.example.st10082074_tridenttimetracker

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlin.collections.HashMap




/**
 * A simple [Fragment] subclass.
 * Use the [CalendarFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CalendarFragment : Fragment() {
    private lateinit var chart: LineChart
    private lateinit var loginId: String
    private lateinit var startDate: String
    private lateinit var minGoal: String
    private lateinit var maxGoal: String
    private lateinit var endDate: String
    private lateinit var dateRangeButton: Button

//verride fun onCreateView(
//   inflater: LayoutInflater,
//   container: ViewGroup?,
//   savedInstanceState: Bundle?
//: View? {
//   val view = inflater.inflate(R.layout.fragment_calendar, container, false)
//
//   chart = view.findViewById(R.id.chart)
//   dateRangeButton = view.findViewById(R.id.dateRangeButton)
//
//   val loginId = arguments?.getString("loginId")
//
//
//   val initialCalendar = Calendar.getInstance()
//   initialCalendar.add(Calendar.MONTH, -1)
//   startDate = formatDate(initialCalendar.time)
//   endDate = formatDate(Date())
//
//   // Set the date range button text
//   updateDateRangeButtonText()
//
//   // Fetch the data from Firestore and update the chart
//   fetchDataFromFirestore()
//
//   // Set click listener for date range button
//   dateRangeButton.setOnClickListener {
//       showDatePickerDialog()
//   }
//
//   return view
//
override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
): View? {
    val view = inflater.inflate(R.layout.fragment_calendar, container, false)
    return view
}

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chart = view.findViewById(R.id.chart)
        dateRangeButton = view.findViewById(R.id.dateRangeButton)

        loginId = arguments?.getString("loginId") ?: ""

        val initialCalendar = Calendar.getInstance()
        initialCalendar.add(Calendar.MONTH, -1)
        startDate = formatDate(initialCalendar.time)
        endDate = formatDate(Date())

        // Set the date range button text
        updateDateRangeButtonText()

        // Fetch the data from Firestore and update the chart
        fetchDataFromFirestore()

        // Set click listener for date range button
        dateRangeButton.setOnClickListener {
            showDatePickerDialog()
        }
    }


    private fun fetchDataFromFirestore() {
        val db = Firebase.firestore

        val goalsRef = db.collection("Goals")
            .whereEqualTo("LoginID", loginId)
            .limit(1)

        goalsRef.get().addOnSuccessListener { goalsSnapshot ->
            if (!goalsSnapshot.isEmpty) {
                val goalsDoc = goalsSnapshot.documents[0]
                minGoal = goalsDoc.getString("minGoal") ?: ""
                maxGoal = goalsDoc.getString("maxGoal") ?: ""
            }
        }
        val projectsRef = db.collection("Projects")
            .whereEqualTo("LoginID", loginId)

        projectsRef.get().addOnSuccessListener { projectsSnapshot ->
            val taskCounts = HashMap<String, Int>()

            for (projectDoc in projectsSnapshot.documents) {
                val projectId = projectDoc.id
                val tasksRef = db.collection("Tasks")
                    .whereEqualTo("ProjectID", projectId)
                    .whereGreaterThanOrEqualTo("StartDate", startDate)
                    .whereLessThanOrEqualTo("StartDate", endDate)

                tasksRef.get().addOnSuccessListener { tasksSnapshot ->
                    for (taskDoc in tasksSnapshot.documents) {
                        val startDate = taskDoc.getString("StartDate") ?: ""
                        val endTime = taskDoc.getString("EndTime") ?: ""

                        if (startDate.isNotEmpty() && endTime.isNotEmpty()) {
                            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(startDate)
                            val day = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(date)

                            if (taskCounts.containsKey(day)) {
                                val count = taskCounts[day] ?: 0
                                taskCounts[day] = count + 1
                            } else {
                                taskCounts[day] = 1
                            }
                        }
                    }

                    updateChart(taskCounts)
                }.addOnFailureListener { exception ->
                    // Handle error
                }
            }
        }.addOnFailureListener { exception ->
            // Handle error
        }
    }

    private fun updateChart(taskCounts: HashMap<String, Int>) {
        val entries = ArrayList<Entry>()

        val description = Description()
        description.text = "Min Goal: $minGoal, Max Goal: $maxGoal"
        chart.description = description

        for ((index, taskCount) in taskCounts) {
            entries.add(Entry(index.toFloat(), taskCount.toFloat()))
        }

        val xAxis = chart.xAxis
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                val date = Date(value.toLong())
                val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                return formatter.format(date)
            }
        }

        val dataSet = LineDataSet(entries, "Hours Worked")
        val data = LineData(dataSet)

        chart.data = data
        chart.invalidate()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                // Update the start and end dates
                val startDateCalendar = Calendar.getInstance()
                startDateCalendar.set(year, month, dayOfMonth)
                startDate = formatDate(startDateCalendar.time)

                val endDateCalendar = Calendar.getInstance()
                endDateCalendar.set(year, month, dayOfMonth)
                endDate = formatDate(endDateCalendar.time)

                // Update the date range button text
                updateDateRangeButtonText()

                // Fetch the data from Firestore and update the chart
                fetchDataFromFirestore()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        // Set the date range limit
        datePickerDialog.datePicker.minDate = getMinimumDate().time
        datePickerDialog.datePicker.maxDate = calendar.timeInMillis

        datePickerDialog.show()
    }

    private fun updateDateRangeButtonText() {
        val startDateText = formatDateForButton(startDate)
        val endDateText = formatDateForButton(endDate)
        val buttonText = "$startDateText - $endDateText"
        dateRangeButton.text = buttonText
    }

    private fun formatDate(date: Date): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return formatter.format(date)
    }

    private fun formatDateForButton(dateString: String): String {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
        val formatter = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault())
        return formatter.format(date)
    }

    private fun getMinimumDate(): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -1) // Set minimum date to one year ago
        return calendar.time
    }
}
