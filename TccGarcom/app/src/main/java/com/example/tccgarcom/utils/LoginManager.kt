package com.example.tccgarcom.utils

import android.content.Context
import android.widget.Toast
import com.example.tccgarcom.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class LoginManager (viewContext: Context) {

    private val context = viewContext
    private val firebaseAuth = FirebaseAuth.getInstance()

    fun doLoginVerification(email: String, password: String, callback: (Boolean) -> Unit) {
        if (!hasEmptyData(listOf(email, password))) {
            if (isEmailCorrectlyFormatted(email)) {
                firebaseAuth.signInWithEmailAndPassword(email, password).addOnSuccessListener { authResult ->
                    val databaseReference = FirebaseDatabase.getInstance().reference
                    val userDataReference = databaseReference.child("Users").child(authResult.user!!.uid)
                    userDataReference.get().addOnSuccessListener { userDataResult ->
                        val role = userDataResult.child("role").value
                        if (role == "waiter") {
                            Toast.makeText(context, R.string.msg_authenticated, Toast.LENGTH_SHORT).show()
                            callback (true)
                        } else { Toast.makeText(context, R.string.msg_user_is_adm, Toast.LENGTH_SHORT).show() }
                    } .addOnFailureListener { failure -> Toast.makeText(context, failure.message, Toast.LENGTH_SHORT).show() }
                } .addOnFailureListener { Toast.makeText(context, R.string.msg_incorrect_login_data, Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun hasEmptyData(dataList: List<String>): Boolean {
        var hasEmpty = false
        for (data in dataList) {
            if (data.isEmpty()) {
                hasEmpty = true
            }
        }
        if (hasEmpty) {
            Toast.makeText(context, R.string.msg_has_empty_fields, Toast.LENGTH_SHORT).show()
        }
        return hasEmpty
    }

    private fun isEmailCorrectlyFormatted(email: String): Boolean {
        val condition = email.contains("@") && email.contains(".")
        if (!condition) {
            Toast.makeText(context, R.string.msg_invalid_email_format, Toast.LENGTH_SHORT).show()
        }
        return condition
    }
}