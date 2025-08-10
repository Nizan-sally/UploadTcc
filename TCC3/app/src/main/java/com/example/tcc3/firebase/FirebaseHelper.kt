package com.example.tcc3.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.example.tcc3.models.WaiterData
import com.google.firebase.database.getValue

class FirebaseHelper {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val usersReference: DatabaseReference = database.reference.child("Users")

    // Cria um garçom
    fun createWaiter(
        email: String, password: String,
        onSuccess: (String) -> Unit, onFailure: (String) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess(task.result.user?.uid.toString())
            } else {
                onFailure(task.exception?.message ?: "Erro ao criar usuário.")
            }
        }
    }

    // Salva dados do garçom
    fun saveWaiterData(userId: String, userData: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        usersReference.child(userId).updateChildren(userData).addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }

    // Obtém a lista de garçons
    fun getWaiters(onDataReceived: (List<WaiterData>) -> Unit, onError: (String) -> Unit) {
        usersReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val waitersList = mutableListOf<WaiterData>()
                snapshot.children.forEach { userSnapshot ->
                    val userId = userSnapshot.key.toString()
                    val userDataMap = userSnapshot.getValue<Map<String, Any>>()
                    val userEmail = userDataMap?.get("email").toString()
                    val userName = userDataMap?.get("name").toString()
                    val userRole = userDataMap?.get("role").toString()

                    if (userRole == "waiter") {
                        val user = WaiterData(userId, userEmail, userName, userRole)
                        waitersList.add(user)
                    }
                }
                onDataReceived(waitersList)
            }

            override fun onCancelled(error: DatabaseError) {
                onError(error.message)
            }
        })
    }

    // Deleta um garçom
    fun deleteWaiter(userId: String, onComplete: (Boolean) -> Unit) {
        usersReference.child(userId).removeValue().addOnCompleteListener { task ->
            onComplete(task.isSuccessful)
        }
    }
}
