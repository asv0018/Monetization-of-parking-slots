package com.iotree.monitizing_parking_slot_app

import android.app.Activity
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Vibrator
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_signup.*
import java.text.SimpleDateFormat
import java.util.*

class Signup : AppCompatActivity() {
    var gender:String = ""
    private lateinit var database: DatabaseReference
    private lateinit var myref:DatabaseReference
    lateinit var progressDialog:ProgressDialog
    lateinit var vibrator: Vibrator
    lateinit var mskillsbutton : Button
    lateinit var auth:FirebaseAuth
    private var choosen_image_uri: Uri? = null
    private var firebaseUserID: String  = ""
    private lateinit var refUsersChat: DatabaseReference
    var button_date: Button? = null
    var textview_date: TextView? = null
    var cal = Calendar.getInstance()
    lateinit var myRef:DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        supportActionBar?.hide()
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        refUsersChat = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("users")
         // THIS IS FOR SIGN UP BUTTON
        signup.setOnClickListener {
            vibrator.vibrate(60)
            progressDialog = ProgressDialog(this)
            progressDialog.setMessage("Hold on, getting a account for you ...")
            progressDialog.setCanceledOnTouchOutside(false)
            if (!name.text.isNullOrBlank()) {
                val username = name.text.toString()
                if (!email.text.isNullOrBlank()) {
                    val  mail = email.text.toString()
                    if (password.text.toString().length > 8) {
                        val pwd = password.text.toString()
                        progressDialog.show()
                        auth.createUserWithEmailAndPassword(
                            mail,
                            pwd
                        ).addOnCompleteListener(
                            this,
                            OnCompleteListener { task ->
                                progressDialog.show()
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        this,
                                        "Email is already registered",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    progressDialog.setMessage("account is created, working on saving your data...")
                                    /////////////////////////////////////////////////////////////////
                                    val userHashMap =
                                        HashMap<String, Any>()
                                    userHashMap["uid"] = FirebaseAuth.getInstance().currentUser.uid.toString()
                                    userHashMap["username"] = username
                                    userHashMap["email"] = mail
                                    val sub = refUsersChat.child("USERS").child(userHashMap["uid"].toString())
                                    sub.updateChildren(
                                        userHashMap
                                    )
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                Toast.makeText(
                                                    this,
                                                    "Your Account has been created!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                progressDialog.dismiss()
                                                startActivity(Intent(this, Login::class.java))
                                                finish()
                                            }
                                        }


                                } else {
                                    Toast.makeText(
                                        this,
                                        "Sign up Failed, Try with different email id",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    progressDialog.dismiss()
                                }
                            })
                    } else {
                        Toast.makeText(
                                this,
                                "Password is less than 8 characters",
                                Toast.LENGTH_LONG
                        ).show()
                    }


                } else {
                    Toast.makeText(
                        this,
                        "email is needed",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Toast.makeText(this,
                    "we need your name",
                    Toast.LENGTH_SHORT).show()
            }
        }

        gotosignin.setOnClickListener {
            startActivity(Intent(this, Login::class.java))
            finish()
        }
    }



    override fun onBackPressed() {
        val alertBox = AlertDialog.Builder(this)
        alertBox.setTitle("Do you wish to discard sign up process?")
        alertBox.setIcon(R.mipmap.ic_launcher)
        alertBox.setMessage("Its ok, come back any time to sign up to our service")
        alertBox.setCancelable(true)
        alertBox.setPositiveButton("yes") { _, _ ->
            startActivity(Intent(this, Login::class.java))
            finish()
        }
        alertBox.setNegativeButton("No") { _, _ ->

        }
        alertBox.create().show()
    }



}