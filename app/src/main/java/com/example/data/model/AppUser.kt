package com.example.data.model

data class AppUser(
    val username: String = "",
    val name: String = "",
    val unitKerja: String = "SRT 1 KAB KEDIRI",
    val jabatan: String = "", // "Wali Asuh", "Wali Asrama", "Ustadz / Ustadzah"
    val password: String = "",
    val status: String = STATUS_MENUNGGU, // "MENUNGGU", "DISETUJUI", "DITOLAK"
    val role: String = ROLE_USER, // "ADMIN", "USER"
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "username" to username,
        "name" to name,
        "unitKerja" to unitKerja,
        "jabatan" to jabatan,
        "password" to password,
        "status" to status,
        "role" to role,
        "createdAt" to createdAt
    )

    companion object {
        const val STATUS_MENUNGGU = "MENUNGGU"
        const val STATUS_DISETUJUI = "DISETUJUI"
        const val STATUS_DITOLAK = "DITOLAK"

        const val ROLE_ADMIN = "ADMIN"
        const val ROLE_USER = "USER"

        val JABATAN_OPTIONS = listOf(
            "Wali Asuh",
            "Wali Asrama",
            "Ustadz / Ustadzah"
        )

        val UNIT_KERJA_DEFAULT = "SRT 1 KAB KEDIRI"

        fun fromMap(map: Map<String, Any?>): AppUser {
            return AppUser(
                username = map["username"] as? String ?: "",
                name = map["name"] as? String ?: "",
                unitKerja = map["unitKerja"] as? String ?: UNIT_KERJA_DEFAULT,
                jabatan = map["jabatan"] as? String ?: "",
                password = map["password"] as? String ?: "",
                status = map["status"] as? String ?: STATUS_MENUNGGU,
                role = map["role"] as? String ?: ROLE_USER,
                createdAt = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
