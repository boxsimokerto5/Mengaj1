package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.R
import com.example.data.model.AppUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject

class UserRepository(
    private val db: FirebaseFirestore,
    context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("srt_auth_prefs", Context.MODE_PRIVATE)

    companion object {
        const val ADMIN_USERNAME = "admin"
        const val ADMIN_PASSWORD = "Woyowoyo12@"

        val ADMIN_USER = AppUser(
            username = ADMIN_USERNAME,
            name = "Administrator SRT 1 Kediri",
            unitKerja = "SRT 1 KAB KEDIRI",
            jabatan = "Administrator",
            password = ADMIN_PASSWORD,
            status = AppUser.STATUS_DISETUJUI,
            role = AppUser.ROLE_ADMIN,
            createdAt = 1704067200000L
        )

        private const val KEY_LOGGED_IN_USER = "key_logged_in_user"
        private const val KEY_LOCAL_USERS_CACHE = "key_local_users_cache"
    }

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        context.applicationContext
    )

    init {
        // Ensure admin user exists in local cache
        saveUserToLocalCache(ADMIN_USER)
    }

    suspend fun register(user: AppUser): Result<Unit> {
        val cleanUsername = user.username.trim().lowercase()
        if (cleanUsername.isBlank()) {
            return Result.failure(IllegalArgumentException("Username tidak boleh kosong"))
        }
        if (cleanUsername == ADMIN_USERNAME.lowercase()) {
            return Result.failure(IllegalArgumentException("Username 'admin' sudah digunakan sebagai akun administrator"))
        }

        try {
            // Check if username already exists in Firestore
            val existingDoc = db.collection("app_users").document(cleanUsername).get().await()
            if (existingDoc.exists()) {
                return Result.failure(IllegalArgumentException("Username '$cleanUsername' sudah digunakan. Silakan pilih username lain."))
            }

            val newUser = user.copy(
                username = cleanUsername,
                status = AppUser.STATUS_MENUNGGU,
                role = AppUser.ROLE_USER,
                createdAt = System.currentTimeMillis()
            )

            // Save to Firestore
            db.collection("app_users").document(cleanUsername).set(newUser.toMap()).await()

            // Save to local cache
            saveUserToLocalCache(newUser)

            return Result.success(Unit)
        } catch (e: Exception) {
            // Fallback to local cache if Firestore is not reachable
            val localUsers = getLocalUsers()
            if (localUsers.any { it.username.equals(cleanUsername, ignoreCase = true) }) {
                return Result.failure(IllegalArgumentException("Username '$cleanUsername' sudah terdaftar"))
            }

            val newUser = user.copy(
                username = cleanUsername,
                status = AppUser.STATUS_MENUNGGU,
                role = AppUser.ROLE_USER,
                createdAt = System.currentTimeMillis()
            )
            saveUserToLocalCache(newUser)
            return Result.success(Unit)
        }
    }

    suspend fun login(usernameInput: String, passwordInput: String): Result<AppUser> {
        val cleanUsername = usernameInput.trim().lowercase()

        // 1. Direct check for Admin credentials
        if (cleanUsername == ADMIN_USERNAME.lowercase()) {
            if (passwordInput == ADMIN_PASSWORD) {
                saveSession(ADMIN_USER)
                return Result.success(ADMIN_USER)
            } else {
                return Result.failure(IllegalArgumentException("Password administrator salah"))
            }
        }

        // 2. Regular user verification
        try {
            val doc = db.collection("app_users").document(cleanUsername).get().await()
            if (doc.exists()) {
                val user = AppUser.fromMap(doc.data ?: emptyMap())
                if (user.password != passwordInput) {
                    return Result.failure(IllegalArgumentException("Username atau password salah"))
                }

                // Check status
                when (user.status) {
                    AppUser.STATUS_MENUNGGU -> {
                        return Result.failure(IllegalStateException("Akun Anda masih menunggu persetujuan Admin SRT 1 Kediri."))
                    }
                    AppUser.STATUS_DITOLAK -> {
                        return Result.failure(IllegalStateException("Pendaftaran akun Anda ditolak oleh Admin SRT 1 Kediri."))
                    }
                    AppUser.STATUS_DISETUJUI -> {
                        saveSession(user)
                        saveUserToLocalCache(user)
                        return Result.success(user)
                    }
                    else -> {
                        return Result.failure(IllegalStateException("Status akun belum terverifikasi"))
                    }
                }
            }
        } catch (e: IllegalStateException) {
            return Result.failure(e)
        } catch (e: IllegalArgumentException) {
            return Result.failure(e)
        } catch (e: Exception) {
            // Check local cache if network/Firestore fails
        }

        // 3. Fallback check local cache
        val localUsers = getLocalUsers()
        val cachedUser = localUsers.firstOrNull { it.username.equals(cleanUsername, ignoreCase = true) }
        if (cachedUser != null) {
            if (cachedUser.password != passwordInput) {
                return Result.failure(IllegalArgumentException("Username atau password salah"))
            }
            when (cachedUser.status) {
                AppUser.STATUS_MENUNGGU -> return Result.failure(IllegalStateException("Akun Anda masih menunggu persetujuan Admin SRT 1 Kediri."))
                AppUser.STATUS_DITOLAK -> return Result.failure(IllegalStateException("Pendaftaran akun Anda ditolak oleh Admin SRT 1 Kediri."))
                AppUser.STATUS_DISETUJUI -> {
                    saveSession(cachedUser)
                    return Result.success(cachedUser)
                }
            }
        }

        return Result.failure(IllegalArgumentException("Username tidak ditemukan atau belum terdaftar"))
    }

    fun observeAllUsers(): Flow<List<AppUser>> = callbackFlow {
        val listener = db.collection("app_users")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Fallback to local cache
                    trySend(getLocalUsers().filter { it.username != ADMIN_USERNAME })
                    return@addSnapshotListener
                }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { AppUser.fromMap(it) }
                } ?: emptyList()

                // Sync with local cache
                list.forEach { saveUserToLocalCache(it) }
                trySend(list)
            }

        awaitClose { listener.remove() }
    }

    suspend fun updateUserStatus(username: String, newStatus: String): Result<Unit> {
        val cleanUsername = username.trim().lowercase()
        return try {
            db.collection("app_users").document(cleanUsername)
                .update("status", newStatus)
                .await()

            // Update local cache
            val localUsers = getLocalUsers().toMutableList()
            val index = localUsers.indexOfFirst { it.username.equals(cleanUsername, ignoreCase = true) }
            if (index != -1) {
                val updated = localUsers[index].copy(status = newStatus)
                localUsers[index] = updated
                saveLocalUsers(localUsers)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            // Update local cache anyway
            val localUsers = getLocalUsers().toMutableList()
            val index = localUsers.indexOfFirst { it.username.equals(cleanUsername, ignoreCase = true) }
            if (index != -1) {
                val updated = localUsers[index].copy(status = newStatus)
                localUsers[index] = updated
                saveLocalUsers(localUsers)
                Result.success(Unit)
            } else {
                Result.failure(e)
            }
        }
    }

    fun saveSession(user: AppUser) {
        prefs.edit().putString(KEY_LOGGED_IN_USER, JSONObject(user.toMap()).toString()).apply()
    }

    fun getSession(): AppUser? {
        val jsonStr = prefs.getString(KEY_LOGGED_IN_USER, null) ?: return null
        return try {
            val json = JSONObject(jsonStr)
            val map = mutableMapOf<String, Any?>()
            val keys = json.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                map[k] = json.opt(k)
            }
            AppUser.fromMap(map)
        } catch (e: Exception) {
            null
        }
    }

    fun logout() {
        prefs.edit().remove(KEY_LOGGED_IN_USER).apply()
    }

    private fun getLocalUsers(): List<AppUser> {
        val raw = prefs.getString(KEY_LOCAL_USERS_CACHE, null) ?: return listOf(ADMIN_USER)
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<AppUser>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val map = mutableMapOf<String, Any?>()
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    map[k] = obj.opt(k)
                }
                list.add(AppUser.fromMap(map))
            }
            list
        } catch (e: Exception) {
            listOf(ADMIN_USER)
        }
    }

    private fun saveLocalUsers(list: List<AppUser>) {
        val arr = JSONArray()
        list.forEach { arr.put(JSONObject(it.toMap())) }
        prefs.edit().putString(KEY_LOCAL_USERS_CACHE, arr.toString()).apply()
    }

    private fun saveUserToLocalCache(user: AppUser) {
        val current = getLocalUsers().toMutableList()
        val index = current.indexOfFirst { it.username.equals(user.username, ignoreCase = true) }
        if (index != -1) {
            current[index] = user
        } else {
            current.add(0, user)
        }
        saveLocalUsers(current)
    }
}
