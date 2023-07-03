package com.example.st10082074_tridenttimetracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView


class MainPage : AppCompatActivity() {
        private val tasksFragment = TasksFragment()
        private val projectsFragment = ProjectsFragment()
        private val calculationsFragment = CalculationsFragment()
        private lateinit var loginId: String // Declare loginId as a lateinit var
        //val loginId = intent.getStringExtra("loginId")

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main_page)

            // Retrieve the loginId extra from the intent
            loginId = intent.getStringExtra("loginId").toString()

            val bottomNavigation: BottomNavigationView = findViewById(R.id.bottomNavigation)
            bottomNavigation.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

            switchFragment(tasksFragment)

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

    }
