package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class ReadingType {
    IQRO,
    QURAN
}

@IgnoreExtraProperties
data class Student(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val gender: String = "Laki-laki",
    val roomOrClass: String = "",
    val currentType: String = ReadingType.IQRO.name,
    val currentLevel: Long = 1L,
    val currentPage: Long = 1L,
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any?>()
        map["userId"] = userId
        map["name"] = name
        map["gender"] = gender
        map["roomOrClass"] = roomOrClass
        map["currentType"] = currentType
        map["currentLevel"] = currentLevel
        map["currentPage"] = currentPage
        map["notes"] = notes
        map["createdAt"] = createdAt ?: FieldValue.serverTimestamp()
        map["updatedAt"] = FieldValue.serverTimestamp()
        return map.filterValues { it != null } as Map<String, Any>
    }
}
