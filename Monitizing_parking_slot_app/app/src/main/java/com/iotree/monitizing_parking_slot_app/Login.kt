package com.iotree.monitizing_parking_slot_app

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*


class Login : AppCompatActivity() {
    lateinit var vibrator:Vibrator
    private lateinit var auth: FirebaseAuth
    lateinit var progressDialog: ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        auth = FirebaseAuth.getInstance()
        progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Hold your seat with patience...")
        progressDialog.setCanceledOnTouchOutside(false)


        signinbtn.setOnClickListener {
            if(!email_login.text.isNullOrBlank()) {
                if (!password_login.text.isNullOrBlank()) {
                    progressDialog.show()
                    attemptToLogin()
                } else {
                    Toast.makeText(
                        this,
                        "You cannot leave the password empty",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }else{
                Toast.makeText(this,"Username or password is not provided",Toast.LENGTH_SHORT).show()
            }
        }

        // intent to register activity if user clicks on the register button
        gotosignup.setOnClickListener {
            gotosignup.isEnabled = false
            vibrator.vibrate(60)
            Log.d("DEBUG","User choose to register himself")
            startActivity(Intent(this, Signup::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            finish()
        }

    }

    private fun attemptToLogin(){
        Log.d("DEBUG","User choose to register himself")
        auth.signInWithEmailAndPassword(email_login.text.toString(), password_login.text.toString()).addOnCompleteListener(this, OnCompleteListener { task ->
            if(task.isSuccessful) {
                val user = auth.currentUser?.email
                val name = user?.split("@")?.toTypedArray()
                Toast.makeText(this, "welcome ${name?.get(0)}, you are signed in", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                progressDialog.dismiss()
                startActivity(intent)
                finish()
            }else {
                progressDialog.dismiss()
                Toast.makeText(this, "Hey! Login Failed, if you have an account, you can retrieve it by forgot password", Toast.LENGTH_SHORT).show()
            }
        })

    }


}