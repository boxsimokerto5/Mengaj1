package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.FluencyStatus
import com.example.data.model.NgajiRecord
import com.example.data.model.ReadingType
import com.example.data.model.Student
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class NgajiRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun addStudent_authenticatedUser_success() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = NgajiRepository(firestore)

        val studentId = "student_${UUID.randomUUID()}"
        val student = Student(
            id = studentId,
            name = "Ahmad Santoso",
            gender = "Laki-laki",
            roomOrClass = "Kamar Abu Bakar",
            currentType = ReadingType.IQRO.name,
            currentLevel = 2L,
            currentPage = 10L,
            notes = "Semangat belajar tajwid"
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addStudent(aliceUid, student)
        }
        assertTrue("Expected addStudent to succeed", result.isSuccess)
    }

    @Test
    fun observeStudents_authenticatedUser_receivesStudent() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = NgajiRepository(firestore)

        val studentId = "student_${UUID.randomUUID()}"
        val student = Student(
            id = studentId,
            name = "Fatimah Azzahra",
            gender = "Perempuan",
            roomOrClass = "Kamar Khadijah",
            currentType = ReadingType.QURAN.name,
            currentLevel = 1L,
            currentPage = 5L
        )

        val addResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addStudent(aliceUid, student)
        }
        assertTrue(addResult.isSuccess)

        val students = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeStudents(aliceUid).first { list -> list.any { it.id == studentId } }
        }
        assertTrue(students.any { it.name == "Fatimah Azzahra" })
    }

    @Test
    fun addRecord_authenticatedUser_updatesStudentProgress() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = NgajiRepository(firestore)

        val studentId = "student_${UUID.randomUUID()}"
        val initialStudent = Student(
            id = studentId,
            name = "Rahmat Hidayat",
            currentType = ReadingType.IQRO.name,
            currentLevel = 1L,
            currentPage = 5L
        )
        repository.addStudent(aliceUid, initialStudent)

        val recordId = "rec_${UUID.randomUUID()}"
        val record = NgajiRecord(
            id = recordId,
            studentId = studentId,
            studentName = "Rahmat Hidayat",
            type = ReadingType.IQRO.name,
            level = 2L,
            page = 1L,
            status = FluencyStatus.LANCAR.name,
            notes = "Naik ke Iqro 2 halaman 1",
            teacherName = "Ustadz Hasan",
            date = "2026-10-23"
        )

        val addRecordResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.addRecord(aliceUid, record, updateStudentProgress = true)
        }
        assertTrue(addRecordResult.isSuccess)

        val updatedStudents = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeStudents(aliceUid).first { list -> list.any { it.id == studentId && it.currentLevel == 2L } }
        }
        val target = updatedStudents.first { it.id == studentId }
        assertEquals(2L, target.currentLevel)
        assertEquals(1L, target.currentPage)
    }

    @Test
    fun observeStudents_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = NgajiRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeStudents("unauth_user_id").first()
            }
            fail("Expected exception due to missing auth")
        } catch (e: Exception) {
            val firestoreException = e.unwrapFirestoreException()
            assertNotNull("Expected unwrapped Firestore exception", firestoreException)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "ustadz_alice@srt1kediri.sch.id"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 5000L
    }
}
