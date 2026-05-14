package com.example.terrirun

import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class NotificationRepository {

    private val db = FirebaseFirestore.getInstance()

    fun sendNotification(
        userId: String,
        title: String,
        message: String
    ) {

        val notification = NotificationData(
            id = UUID.randomUUID().toString(),
            userId = userId,
            title = title,
            message = message
        )

        db.collection("notifications")
            .document(notification.id)
            .set(notification)
    }

    fun getNotifications(
        userId: String,
        onResult: (List<NotificationData>) -> Unit
    ) {
        db.collection("notifications")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { result ->

                val notifications = result.documents.mapNotNull {
                    it.toObject(NotificationData::class.java)
                }

                onResult(
                    notifications.sortedByDescending { it.timestamp }
                )
            }
    }
}