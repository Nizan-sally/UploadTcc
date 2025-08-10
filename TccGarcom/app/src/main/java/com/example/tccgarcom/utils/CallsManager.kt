package com.example.tccgarcom.utils

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.getValue
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CallsManager(private val waiterId: String = "") {

    @RequiresApi(Build.VERSION_CODES.O)
    fun checkLastCall(btnKey: String, isWaiter: Boolean) {
        val database = FirebaseDatabase.getInstance()
        database.reference.child("LastCalls").child(btnKey).get().addOnSuccessListener { getLastCallIdResult ->
            val lastCallId = getLastCallIdResult.getValue<String>()

            if (lastCallId == null) {
                if (!isWaiter) {
                    saveCall(btnKey)
                }
            } else {
                database.reference.child("Calls").child(btnKey).child(lastCallId).get()
                    .addOnSuccessListener { lastCall ->
                        val dataMap = lastCall.getValue<Map<String, Any>>()
                        val called = dataMap?.get("Called")
                        val attended = dataMap?.get("Attended")

                        if (isWaiter && attended == "false") {
                            updateCall(lastCallId, btnKey, called.toString(), waiterId)
                        } else {
                            if (attended != "false") {
                                saveCall(btnKey)
                            }
                        }
                    }.addOnFailureListener { println(it.message) }
            }
        }


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveCall(btnKey: String) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val current = LocalDateTime.now().format(formatter)

        val dataMap = mapOf<String, Any>("Called" to current, "Attended" to "false")

        val database = FirebaseDatabase.getInstance()
        val newCallReference = database.reference.child("Calls").child(btnKey).push()
        newCallReference.updateChildren(dataMap)
        val callId = newCallReference.key.toString()

        updateLastCall(callId, btnKey)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateCall(callId: String, btnKey: String, calledAt: String, waiterId: String) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val current = LocalDateTime.now().format(formatter)

        val dataMap = mapOf<String, Any>("Called" to calledAt, "Attended" to current, "Waiter" to waiterId)

        val database = FirebaseDatabase.getInstance()
        database.reference.child("Calls").child(btnKey).child(callId).updateChildren(dataMap)
    }

    private fun updateLastCall(callId: String, btnKey: String) {
        val database = FirebaseDatabase.getInstance()
        val dataMap = mapOf<String, Any>(btnKey to callId)
        database.reference.child("LastCalls").updateChildren(dataMap)
    }
}