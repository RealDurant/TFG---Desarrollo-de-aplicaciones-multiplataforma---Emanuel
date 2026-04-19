package com.example.terrirun

import com.google.firebase.firestore.FirebaseFirestore

class TerritoryRepository {

    private val db = FirebaseFirestore.getInstance()

    fun saveTerritory(
        territory: TerritoryDto,
        onResult: (Boolean, String?) -> Unit
    ) {
        db.collection("territories")
            .document("${territory.ownerId}_${territory.id}")
            .set(territory)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }

    fun loadTerritoriesByOwner(
        ownerId: String,
        onResult: (List<TerritoryDto>, String?) -> Unit
    ) {
        db.collection("territories")
            .whereEqualTo("ownerId", ownerId)
            .get()
            .addOnSuccessListener { result ->
                val territories = result.documents.mapNotNull { doc ->
                    doc.toObject(TerritoryDto::class.java)
                }
                onResult(territories, null)
            }
            .addOnFailureListener { e ->
                onResult(emptyList(), e.message)
            }
    }

    fun updateTerritoryControl(
        ownerId: String,
        territoryId: Int,
        control: Int,
        onResult: (Boolean, String?) -> Unit
    ) {
        db.collection("territories")
            .document("${ownerId}_${territoryId}")
            .update("control", control)
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }
    fun loadAllTerritories(
        onResult: (List<TerritoryDto>, String?) -> Unit
    ) {
        db.collection("territories")
            .get()
            .addOnSuccessListener { result ->
                val territories = result.documents.mapNotNull { doc ->
                    doc.toObject(TerritoryDto::class.java)
                }
                onResult(territories, null)
            }
            .addOnFailureListener { e ->
                onResult(emptyList(), e.message)
            }
    }
    fun deleteTerritory(
        ownerId: String,
        territoryId: Int,
        onResult: (Boolean, String?) -> Unit
    ) {
        db.collection("territories")
            .document("${ownerId}_${territoryId}")
            .delete()
            .addOnSuccessListener {
                onResult(true, null)
            }
            .addOnFailureListener { e ->
                onResult(false, e.message)
            }
    }
}