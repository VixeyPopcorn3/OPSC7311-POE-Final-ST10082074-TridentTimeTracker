package com.example.st10082074_tridenttimetracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MainPage : AppCompatActivity() {
        private val tasksFragment = TasksFragment()
        private val projectsFragment = ProjectsFragment()
        private val calculationsFragment = CalculationsFragment()
        private val goalsFragment = Goals()
        private val calendar = CalendarFragment()
        private lateinit var goldStarTextView: TextView
        private lateinit var loginId: String // Declare loginId as a lateinit var
        //val loginId = intent.getStringExtra("loginId")

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main_page)

            // Retrieve the loginId extra from the intent
            loginId = intent.getStringExtra("loginId").toString()

            val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)
            bottomNavigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

            goldStarTextView = findViewById(R.id.GoldStarTextView)

            switchFragment(tasksFragment)
            displayGoldStar(loginId, goldStarTextView)

            val logoutButton = findViewById<Button>(R.id.logoutBtn)
            logoutButton.setOnClickListener {
                startActivity(Intent(this@MainPage, MainActivity::class.java))
            }
        }
        private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_tasks -> {
                    switchFragment(tasksFragment)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_projects -> {
                    switchFragment(projectsFragment)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_calculations -> {
                    switchFragment(calculationsFragment)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_calendar -> {
                    switchFragment(goalsFragment)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_graph -> {
                    switchFragment(calendar)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

        private fun switchFragment(fragment: Fragment) {
            val bundle = Bundle()
            bundle.putString("loginId", loginId)
            fragment.arguments = bundle

            supportFragmentManager.beginTransaction()
                .replace(R.id.contentLayout, fragment)
                .commit()
        }
    fun displayGoldStar(loginId: String, textView: TextView) {
        var db = Firebase.firestore
        val streakCollection = db.collection("Streak")

        val query = streakCollection.whereEqualTo("LoginID", loginId)

        query.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val goldStar = document.getString("GoldStar") ?: 0

                    textView.text = goldStar.toString()
                } else {
                    textView.text = (0).toString()
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors that occurred while fetching the data
                // Display an error message or handle the failure
            }
    }







}
