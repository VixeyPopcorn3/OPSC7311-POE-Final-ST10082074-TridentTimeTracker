package com.example.st10082074_tridenttimetracker

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat
import java.util.*


class LoginActivity : AppCompatActivity() {
    private var db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //var editTextEmail = findViewById<EditText>(R.id.editTextEmail)
        //var editTextPassword = findViewById<EditText>(R.id.editTextPassword)

        val buttonLogin = findViewById<Button>(R.id.buttonLogin)
        val buttonBack = findViewById<Button>(R.id.buttonBack)

        buttonLogin.setOnClickListener {
            login()
        }

        buttonBack.setOnClickListener {
            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
        }
    }

    private fun login() {
        //val db = Firebase.firestore
        val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)

        val Eemail = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        if (TextUtils.isEmpty(Eemail) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        // Hash the password
        val hashedPassword = hashPassword(password)

        val usersCollection = db.collection("Users")
        val enteredEmail = editTextEmail.text.toString().trim()

        //checks all emails in each document
        usersCollection.get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot)
                {
                    val dBemail = document.getString("Email")
                    val dBpassword = document.getString("Password")
                    if (dBemail == Eemail && dBpassword == hashedPassword)
                    {
                        // Email and Password found in the Firestore collection
                        Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                        val loginId = document.getString("LoginID")
                        fetchStreakData(loginId.toString())
                        startActivity(Intent(this@LoginActivity, MainPage::class.java).apply
                        {
                            putExtra("loginId", loginId)
                        })
                        break // Exit the loop if a match is found
                    }
                }
                // If the loop completes without finding a match, the email does not exist
                Toast.makeText(this, "Please enter a valid email or password", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Error connecting to Database", Toast.LENGTH_SHORT).show()
            }
    }

    private fun hashPassword(password: String): String? {
        return try {
            val messageDigest = MessageDigest.getInstance("SHA-256")
            val hashBytes = messageDigest.digest(password.toByteArray())
            val stringBuilder = StringBuilder()
            for (hashByte in hashBytes) {
                stringBuilder.append(Integer.toString((hashByte.toInt() and 0xff) + 0x100, 16).substring(1))
            }
            stringBuilder.toString()
        }
        catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            null
        }
    }

    fun fetchStreakData(loginId: String) {
        //val db = Firebase.firestore
        val streakCollection = db.collection("Streak")

        val query = streakCollection.whereEqualTo("LoginID", loginId)

        query.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val lastDate = document.getString("LastDate")
                    val streak = document.getLong("Streak")?.toInt()
                    val goldStar = document.getString("GoldStar")

                    // Perform actions/checks with the retrieved data
                    if (lastDate != null && streak != null && goldStar != null) {
                        // checks/actions
                        checkLastDate(lastDate, streak, loginId, goldStar)
                    }
                } else {
                    // The user's streak data doesn't exist in the Firestore
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors that occurred while fetching the data
            }
    }


    fun checkLastDate(lastDate: String , streak: Int, loginId: String, goldStar: String) {
        val currentDate = getCurrentDate()
        val lastDateObj = parseDate(lastDate)

        if (lastDateObj == currentDate) {
            // Last date is the same as the current date
            // Nothing needs to be done
            if (streak == 5)
            {
                updateStreakInFirestore(0, loginId, goldStar + 1)
            }
        } else if (isPreviousDay(lastDateObj, currentDate)) {
            // Last date is the day before the current date
            // Check streak  and either add by one or change to 0 and update goldstar and update it in the database
            if (streak == 5)
            {
                updateStreakInFirestore(0, loginId, goldStar + 1)
            }
            else if (streak < 5)
            {
                updateStreakInFirestore(streak + 1, loginId, goldStar)
            }
        } else if (isMultipleDaysBefore(lastDateObj, currentDate, 2)) {
            // Last date is 2 or more days before the current date
            // Reset the streak to 0 and update it in the database
            updateStreakInFirestore(0, loginId,goldStar)
        }
    }

    fun getCurrentDate(): Date {
        return Calendar.getInstance().time
    }

    fun parseDate(dateString: String): Date {
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return format.parse(dateString) ?: Date()
    }

    fun isPreviousDay(date1: Date, date2: Date): Boolean {
        val calendar = Calendar.getInstance()
        calendar.time = date2
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val previousDay = calendar.time
        return date1 == previousDay
    }

    fun isMultipleDaysBefore(date1: Date, date2: Date, days: Int): Boolean {
        val calendar = Calendar.getInstance()
        calendar.time = date2
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        val beforeDays = calendar.time
        return date1.before(beforeDays)
    }

    fun updateStreakInFirestore(streak: Int, loginId: String,goldStar: String) {

        val streakCollection = db.collection("Streak")

        val query = streakCollection.whereEqualTo("LoginID", loginId)

        query.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {

                    val document = querySnapshot.documents[0]
                    val documentId = document.id


                    val updatedData = hashMapOf(
                        "GoldStar" to goldStar,
                        "LastDate" to getCurrentDate(),
                        "Streak" to streak
                    )

                    streakCollection.document(documentId)
                        .update(updatedData as Map<String, Any>)
                        .addOnSuccessListener {
                            // Streak updated successfully
                        }
                        .addOnFailureListener { exception ->
                            // Handle any errors that occurred while updating the streak
                        }
                } else {
                    // The user's streak data doesn't exist in the Firestore
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors that occurred while fetching the data
            }
    }

}






