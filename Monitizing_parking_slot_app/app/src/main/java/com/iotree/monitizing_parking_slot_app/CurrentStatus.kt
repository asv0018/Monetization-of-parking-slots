package com.iotree.monitizing_parking_slot_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.FirebaseApiNotAvailableException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_current_status.*

class CurrentStatus : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_current_status)
        val uid = FirebaseAuth.getInstance().uid.toString()
        slotname.text = "None"
        priceview.text = "None"
        timeview.text = "None"

        FirebaseDatabase.getInstance().reference.child("USERS").child(uid).addValueEventListener(
            object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {}
                override fun onDataChange(snapshot: DataSnapshot) {
                    textView3.text = "Hi "+snapshot.child("username").value.toString()+","
                }
            }
        )
        paybtn.setOnClickListener {
            if((slotname.text!="None")and(priceview.text != "None") and(timeview.text != "None")){
                val hashMap = HashMap<String,Any>()
                hashMap["uid"] = "None"
                hashMap["duration"] = "None"
                hashMap["price"] = "None"

                FirebaseDatabase.getInstance().reference.child("SLOTKEY").child(uid).setValue("None")

                val b = FirebaseDatabase.getInstance().reference.child("SLOTS").child(slotname.text.toString())
                b.updateChildren(hashMap).addOnSuccessListener {
                    startActivity(Intent(this, PaidStatus::class.java))
                }
            }
        }
        val db = FirebaseDatabase.getInstance().reference.child("SLOTKEY")
        db.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.hasChild(uid)){
                    val slot_name = snapshot.child(uid).value.toString()
                    val getdata = FirebaseDatabase.getInstance().reference.child("SLOTS").child(slot_name)
                    if(slot_name!="None" && slot_name!="null"){
                        getdata.addValueEventListener(object : ValueEventListener{
                            override fun onDataChange(p0: DataSnapshot) {
                                if(p0.child("price").value.toString()!="None"){
                                    slotname.text = slot_name
                                    priceview.text = "Rs "+p0.child("price").value.toString()
                                    timeview.text = p0.child("duration").value.toString()
                                }

                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }

                }
            }
        })
    }
}