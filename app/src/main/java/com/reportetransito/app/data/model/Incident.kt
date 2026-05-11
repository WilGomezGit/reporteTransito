package com.reportetransito.app.data.model

import com.google.firebase.Timestamp

data class Incident(
    val id: String = "",
    val type: String = IncidentType.ACCIDENT.name,
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val description: String = "",
    val reportedBy: String = "",
    val createdAt: Timestamp = Timestamp.now(),
    val lastConfirmedAt: Timestamp = Timestamp.now(),
    val confirmations: Int = 1,
    val cityId: String = "bogota"
) {
    val incidentType: IncidentType
        get() = try { IncidentType.valueOf(type) } catch (e: Exception) { IncidentType.HAZARD }

    fun toMap(): Map<String, Any> = mapOf(
        "type" to type,
        "latitude" to latitude,
        "longitude" to longitude,
        "description" to description,
        "reportedBy" to reportedBy,
        "createdAt" to createdAt,
        "lastConfirmedAt" to lastConfirmedAt,
        "confirmations" to confirmations,
        "cityId" to cityId
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): Incident = Incident(
            id = id,
            type = map["type"] as? String ?: IncidentType.ACCIDENT.name,
            latitude = (map["latitude"] as? Double) ?: 0.0,
            longitude = (map["longitude"] as? Double) ?: 0.0,
            description = map["description"] as? String ?: "",
            reportedBy = map["reportedBy"] as? String ?: "",
            createdAt = map["createdAt"] as? Timestamp ?: Timestamp.now(),
            lastConfirmedAt = map["lastConfirmedAt"] as? Timestamp ?: Timestamp.now(),
            confirmations = (map["confirmations"] as? Long)?.toInt() ?: 1,
            cityId = map["cityId"] as? String ?: "bogota"
        )
    }
}
