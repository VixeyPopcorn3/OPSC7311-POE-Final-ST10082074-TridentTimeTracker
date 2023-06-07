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


class LoginActivity : AppCompatActivity() {
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
        val db = Firebase.firestore
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
}






