package com.reportetransito.app.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.reportetransito.app.data.model.Incident
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IncidentRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    companion object {
        private const val COLLECTION = "incidents"
        private const val EXPIRY_MILLIS = 60 * 60 * 1000L // 1 hora
    }

    fun getActiveIncidents(cityId: String): Flow<List<Incident>> = callbackFlow {
        val cutoff = Timestamp(Date(System.currentTimeMillis() - EXPIRY_MILLIS))

        val listener = firestore.collection(COLLECTION)
            .whereEqualTo("cityId", cityId)
            .whereGreaterThan("lastConfirmedAt", cutoff)
            .orderBy("lastConfirmedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val incidents = snapshot?.documents?.mapNotNull { doc ->
                    runCatching {
                        Incident.fromMap(doc.id, doc.data ?: emptyMap())
                    }.getOrNull()
                } ?: emptyList()
                trySend(incidents)
            }

        awaitClose { listener.remove() }
    }

    suspend fun reportIncident(incident: Incident): Result<String> = runCatching {
        ensureAuthenticated()
        val uid = auth.currentUser?.uid ?: error("No autenticado")
        val doc = firestore.collection(COLLECTION).document()
        val data = incident.copy(
            id = doc.id,
            reportedBy = uid,
            createdAt = Timestamp.now(),
            lastConfirmedAt = Timestamp.now()
        ).toMap()
        doc.set(data).await()
        doc.id
    }

    suspend fun confirmIncident(incidentId: String): Result<Unit> = runCatching {
        ensureAuthenticated()
        val ref = firestore.collection(COLLECTION).document(incidentId)
        firestore.runTransaction { tx ->
            val snap = tx.get(ref)
            val current = (snap.getLong("confirmations") ?: 1L) + 1
            tx.update(ref, mapOf(
                "lastConfirmedAt" to Timestamp.now(),
                "confirmations" to current
            ))
        }.await()
    }

    private suspend fun ensureAuthenticated() {
        if (auth.currentUser == null) {
            auth.signInAnonymously().await()
        }
    }
}
