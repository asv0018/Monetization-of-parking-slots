package com.iotree.monitizing_parking_slot_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var selected_lane = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        radioGroup.setOnCheckedChangeListener(
            RadioGroup.OnCheckedChangeListener { group, checkedId ->
                val radio: RadioButton = findViewById(checkedId)
                selected_lane = radio.text.toString()
            })

        currentbookedslot.setOnClickListener {
            startActivity(Intent(this, CurrentStatus::class.java))
        }

        button.setOnClickListener {
            if(otp.text.isNotEmpty()){
                if(selected_lane!=""){
                    val myotp = otp.text.toString()
                    FirebaseDatabase.getInstance().reference.child("OTP")
                        .addValueEventListener(object :ValueEventListener{
                            override fun onCancelled(error: DatabaseError) {}
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val givenotp = snapshot.child(selected_lane).value.toString()
                                Log.d("CHECCK", selected_lane)
                                Log.d("CHECCK", givenotp)
                                if (givenotp == myotp){
                                    val uid = FirebaseAuth.getInstance().currentUser.uid.toString()
                                    val dada = FirebaseDatabase.getInstance().reference.child("SLOTS").child(selected_lane)
                                    val hashMap = HashMap<String, Any>()
                                    hashMap["duration"] = "00:00:00"
                                    hashMap["price"] = "0.00"
                                    hashMap["uid"] = uid
                                    dada.updateChildren(hashMap)
                                        .addOnSuccessListener {
                                            FirebaseDatabase.getInstance().reference.child("SLOTKEY").child(uid).setValue(selected_lane)
                                                .addOnSuccessListener {
                                                    FirebaseDatabase.getInstance().reference.child("OTP").child(selected_lane).setValue("None")
                                                    startActivity(Intent(this@MainActivity, CurrentStatus::class.java))

                                                }
                                        }

                                }else{
                                    Toast.makeText(this@MainActivity, "OTP is incorrect or wrong slot selected.", Toast.LENGTH_SHORT).show()

                                }
                            }
                        })
                }
            }
        }
        val options = FirebaseDatabase.getInstance().reference.child("SLOTS")
        options.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {
                val slots = mutableListOf("slot1", "slot2", "slot3")
                slot1.isEnabled = true
                slot2.isEnabled = true
                slot3.isEnabled = true
                for(i in slots){
                    if(snapshot.child(i).child("uid").value.toString() != "None"){
                        when (i) {
                            "slot1" -> {
                                slot1.isEnabled = false
                            }
                            "slot2" -> {
                                slot2.isEnabled = false
                            }
                            "slot3" -> {
                                slot3.isEnabled = false
                            }
                        }
                    }
                }
            }
        })
    }
}