package com.example.terrirun


import com.google.firebase.firestore.FirebaseFirestore

class UserRepository {

    private val db = FirebaseFirestore.getInstance()

    fun saveUserProfile(
        profile: UserProfile,
        onResult: (Boolean, String?) -> Unit
    ) {
        db.collection("users")
            .document(profile.uid)
            .set(profile)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

    fun getUserProfile(
        uid: String,
        onResult: (UserProfile?, String?) -> Unit
    ) {
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val profile = document.toObject(UserProfile::class.java)
                onResult(profile, null)
            }
            .addOnFailureListener { e ->
                onResult(null, e.message)
            }
    }
    fun updateUserProfileFields(
        uid: String,
        name: String,
        colorHex: String,
        avatar: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        db.collection("users")
            .document(uid)
            .update(
                mapOf(
                    "name" to name,
                    "colorHex" to colorHex,
                    "avatar" to avatar
                )
            )
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }
    fun updateReinforcementPoints(
        uid: String,
        reinforcementPoints: Int,
        onResult: (Boolean, String?) -> Unit
    ) {
        db.collection("users")
            .document(uid)
            .update("reinforcementPoints", reinforcementPoints)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }
    fun updateNotifications(
        uid: String,
        enabled: Boolean,
        onResult: (Boolean, String?) -> Unit
    ) {
        db.collection("users")
            .document(uid)
            .update("notificationsEnabled", enabled)
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { onResult(false, it.message) }
    }

}
