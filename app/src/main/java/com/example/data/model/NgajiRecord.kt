package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class FluencyStatus(val label: String) {
    LANCAR("Lancar"),
    KURANG_LANCAR("Kurang Lancar"),
    BELUM_LANCAR("Belum Lancar")
}

@IgnoreExtraProperties
data class NgajiRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val type: String = ReadingType.IQRO.name,
    val level: Long = 1L,
    val page: Long = 1L,
    val surahName: String = "",
    val verse: String = "",
    val status: String = FluencyStatus.LANCAR.name,
    val notes: String = "",
    val teacherName: String = "",
    val date: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any?>()
        map["userId"] = userId
        map["studentId"] = studentId
        map["studentName"] = studentName
        map["type"] = type
        map["level"] = level
        map["page"] = page
        map["surahName"] = surahName
        map["verse"] = verse
        map["status"] = status
        map["notes"] = notes
        map["teacherName"] = teacherName
        map["date"] = date
        map["createdAt"] = createdAt ?: FieldValue.serverTimestamp()
        map["updatedAt"] = FieldValue.serverTimestamp()
        return map.filterValues { it != null } as Map<String, Any>
    }
}
