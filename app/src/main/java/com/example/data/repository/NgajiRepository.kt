package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.NgajiRecord
import com.example.data.model.Student
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

class NgajiRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    fun observeStudents(userId: String): Flow<List<Student>> {
        val path = "users/$userId/students"
        return db.collection("users").document(userId).collection("students")
            .snapshots()
            .map { snapshot -> snapshot.toObjects(Student::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
    }

    suspend fun addStudent(userId: String, student: Student): Result<String> {
        val docId = if (student.id.isNotBlank()) student.id else "student_${UUID.randomUUID()}"
        val docRef = db.collection("users").document(userId).collection("students").document(docId)
        val studentWithId = student.copy(id = docId, userId = userId)
        return try {
            docRef.set(studentWithId.toMap()).await()
            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateStudent(userId: String, student: Student): Result<Unit> {
        require(student.id.isNotBlank()) { "Student ID must not be blank" }
        val docRef = db.collection("users").document(userId).collection("students").document(student.id)
        return try {
            docRef.set(student.toMap()).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteStudent(userId: String, studentId: String): Result<Unit> {
        val docRef = db.collection("users").document(userId).collection("students").document(studentId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeRecords(userId: String): Flow<List<NgajiRecord>> {
        val path = "users/$userId/records"
        return db.collection("users").document(userId).collection("records")
            .snapshots()
            .map { snapshot -> snapshot.toObjects(NgajiRecord::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
    }

    fun observeRecordsForStudent(userId: String, studentId: String): Flow<List<NgajiRecord>> {
        val path = "users/$userId/records"
        return db.collection("users").document(userId).collection("records")
            .whereEqualTo("studentId", studentId)
            .snapshots()
            .map { snapshot -> snapshot.toObjects(NgajiRecord::class.java) }
            .catch { e ->
                if (e is Exception) handleFirestoreError(e, OperationType.LIST, path)
                throw e
            }
    }

    suspend fun addRecord(
        userId: String,
        record: NgajiRecord,
        updateStudentProgress: Boolean = true
    ): Result<String> {
        val docId = if (record.id.isNotBlank()) record.id else "record_${UUID.randomUUID()}"
        val docRef = db.collection("users").document(userId).collection("records").document(docId)
        val recordWithId = record.copy(id = docId, userId = userId)
        return try {
            docRef.set(recordWithId.toMap()).await()

            if (updateStudentProgress && record.studentId.isNotBlank()) {
                val studentRef = db.collection("users").document(userId).collection("students").document(record.studentId)
                val studentSnap = studentRef.get().await()
                if (studentSnap.exists()) {
                    val currentStudent = studentSnap.toObject(Student::class.java)
                    if (currentStudent != null) {
                        val updatedStudent = currentStudent.copy(
                            currentType = record.type,
                            currentLevel = record.level,
                            currentPage = record.page
                        )
                        studentRef.set(updatedStudent.toMap()).await()
                    }
                }
            }

            Result.success(docId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateRecord(userId: String, record: NgajiRecord): Result<Unit> {
        require(record.id.isNotBlank()) { "Record ID must not be blank" }
        val docRef = db.collection("users").document(userId).collection("records").document(record.id)
        return try {
            docRef.set(record.toMap()).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteRecord(userId: String, recordId: String): Result<Unit> {
        val docRef = db.collection("users").document(userId).collection("records").document(recordId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }
}
