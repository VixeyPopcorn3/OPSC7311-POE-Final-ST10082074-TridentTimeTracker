package com.example.st10082074_tridenttimetracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val buttonRegister = findViewById<Button>(R.id.buttonRegister)
        val buttonBack = findViewById<Button>(R.id.buttonBack)

        buttonRegister.setOnClickListener {
            register()
        }

        buttonBack.setOnClickListener {
            startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
        }
    }

    private fun register() {
        val db = Firebase.firestore
        val editTextEmail = findViewById<EditText>(R.id.editTextEmail)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)

        val email = editTextEmail.text.toString().trim()
        val password = editTextPassword.text.toString().trim()

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        // Hash the password
        val hashedPassword = hashPassword(password)

        // Check if the email is already registered
        val usersCollection = db.collection("Users")
        usersCollection.whereEqualTo("Email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot.isEmpty) {
                    // Email is not registered, proceed with registration

                    // Get the highest loginID from the existing documents
                    usersCollection.orderBy("LoginID", Query.Direction.DESCENDING).limit(1)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            var highestLoginID = 0
                            for (document in querySnapshot) {
                                highestLoginID = document.getLong("LoginID")?.toInt() ?: 0
                            }

                            // Increment the highest loginID by 1 to generate a new unique loginID
                            val newLoginID = highestLoginID + 1

                            val newUser = hashMapOf(
                                "Email" to email,
                                "Password" to hashedPassword,
                                "LoginID" to newLoginID
                                // Add other user details if needed
                            )

                            // Save user to Firestore
                            usersCollection.add(newUser)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this@RegisterActivity, MainActivity::class.java))
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(this, "Error registering user", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "Error retrieving highest LoginID", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    // Email is already registered
                    Toast.makeText(this, "Email already exists", Toast.LENGTH_SHORT).show()
                }
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
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
            null
        }
    }
}