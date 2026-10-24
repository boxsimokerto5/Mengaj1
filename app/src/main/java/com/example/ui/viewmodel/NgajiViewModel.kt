package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.FluencyStatus
import com.example.data.model.NgajiRecord
import com.example.data.model.ReadingType
import com.example.data.model.Student
import com.example.data.repository.NgajiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

enum class MainTab {
    SANTRI,
    CATAT_NGAJI,
    EVALUASI,
    LAPORAN
}

enum class ReportPeriod(val label: String) {
    PEKAN_INI("Pekan Ini"),
    BULAN_INI("Bulan Ini"),
    SEMUA("Semua Waktu")
}

class NgajiViewModel(
    private val repository: NgajiRepository,
    private val currentUserId: String
) : ViewModel() {

    private val _activeTab = MutableStateFlow(MainTab.SANTRI)
    val activeTab: StateFlow<MainTab> = _activeTab.asStateFlow()

    private val _selectedStudent = MutableStateFlow<Student?>(null)
    val selectedStudent: StateFlow<Student?> = _selectedStudent.asStateFlow()

    private val _quickLogStudent = MutableStateFlow<Student?>(null)
    val quickLogStudent: StateFlow<Student?> = _quickLogStudent.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _filterType = MutableStateFlow("SEMUA") // "SEMUA", "IQRO", "QURAN"
    val filterType: StateFlow<String> = _filterType.asStateFlow()

    private val _reportPeriod = MutableStateFlow(ReportPeriod.PEKAN_INI)
    val reportPeriod: StateFlow<ReportPeriod> = _reportPeriod.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    val studentsState: StateFlow<UiState<List<Student>>> = repository.observeStudents(currentUserId)
        .map<List<Student>, UiState<List<Student>>> { list ->
            if (list.isEmpty()) {
                seedInitialDataIfEmpty()
            }
            UiState.Success(list)
        }
        .catch { error ->
            Log.e(TAG, "Gagal mengambil data santri", error)
            emit(UiState.Error(error.localizedMessage ?: "Gagal memuat data santri"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val recordsState: StateFlow<UiState<List<NgajiRecord>>> = repository.observeRecords(currentUserId)
        .map<List<NgajiRecord>, UiState<List<NgajiRecord>>> { UiState.Success(it) }
        .catch { error ->
            Log.e(TAG, "Gagal mengambil riwayat mengaji", error)
            emit(UiState.Error(error.localizedMessage ?: "Gagal memuat catatan mengaji"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    fun setActiveTab(tab: MainTab) {
        _activeTab.value = tab
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterType(type: String) {
        _filterType.value = type
    }

    fun setReportPeriod(period: ReportPeriod) {
        _reportPeriod.value = period
    }

    fun selectStudentForDetail(student: Student?) {
        _selectedStudent.value = student
        if (student != null) {
            _activeTab.value = MainTab.EVALUASI
        }
    }

    fun startQuickLog(student: Student) {
        _quickLogStudent.value = student
        _activeTab.value = MainTab.CATAT_NGAJI
    }

    fun clearQuickLogStudent() {
        _quickLogStudent.value = null
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun addStudent(
        name: String,
        gender: String,
        roomOrClass: String,
        currentType: String,
        currentLevel: Long,
        currentPage: Long,
        notes: String = "",
        onComplete: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val student = Student(
                name = name.trim(),
                gender = gender,
                roomOrClass = roomOrClass.trim(),
                currentType = currentType,
                currentLevel = currentLevel,
                currentPage = currentPage,
                notes = notes.trim()
            )
            val result = repository.addStudent(currentUserId, student)
            if (result.isSuccess) {
                _userMessage.value = "Santri ${student.name} berhasil ditambahkan"
                onComplete(true)
            } else {
                _userMessage.value = "Gagal menambah santri: ${result.exceptionOrNull()?.message}"
                onComplete(false)
            }
        }
    }

    fun updateStudent(
        student: Student,
        onComplete: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = repository.updateStudent(currentUserId, student)
            if (result.isSuccess) {
                _userMessage.value = "Data santri ${student.name} berhasil diperbarui"
                if (_selectedStudent.value?.id == student.id) {
                    _selectedStudent.value = student
                }
                onComplete(true)
            } else {
                _userMessage.value = "Gagal memperbarui: ${result.exceptionOrNull()?.message}"
                onComplete(false)
            }
        }
    }

    fun deleteStudent(student: Student, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val result = repository.deleteStudent(currentUserId, student.id)
            if (result.isSuccess) {
                _userMessage.value = "Santri ${student.name} berhasil dihapus"
                if (_selectedStudent.value?.id == student.id) {
                    _selectedStudent.value = null
                    _activeTab.value = MainTab.SANTRI
                }
                onComplete()
            } else {
                _userMessage.value = "Gagal menghapus santri: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun addRecord(
        student: Student,
        type: String,
        level: Long,
        page: Long,
        surahName: String,
        verse: String,
        status: String,
        notes: String,
        teacherName: String,
        date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
        onComplete: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val record = NgajiRecord(
                studentId = student.id,
                studentName = student.name,
                type = type,
                level = level,
                page = page,
                surahName = surahName.trim(),
                verse = verse.trim(),
                status = status,
                notes = notes.trim(),
                teacherName = teacherName.trim(),
                date = date
            )
            val result = repository.addRecord(currentUserId, record, updateStudentProgress = true)
            if (result.isSuccess) {
                _userMessage.value = "Catatan mengaji untuk ${student.name} berhasil disimpan"
                // Update local selection if relevant
                _selectedStudent.value = student.copy(
                    currentType = type,
                    currentLevel = level,
                    currentPage = page
                )
                onComplete(true)
            } else {
                _userMessage.value = "Gagal menyimpan catatan: ${result.exceptionOrNull()?.message}"
                onComplete(false)
            }
        }
    }

    fun deleteRecord(record: NgajiRecord) {
        viewModelScope.launch {
            val result = repository.deleteRecord(currentUserId, record.id)
            if (result.isSuccess) {
                _userMessage.value = "Catatan mengaji berhasil dihapus"
            } else {
                _userMessage.value = "Gagal menghapus catatan"
            }
        }
    }

    private var hasSeeded = false
    private fun seedInitialDataIfEmpty() {
        if (hasSeeded) return
        hasSeeded = true
        viewModelScope.launch {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cal = Calendar.getInstance()

            val sampleStudents = listOf(
                Student(
                    name = "Ahmad Zaki Al-Faruq",
                    gender = "Laki-laki",
                    roomOrClass = "Kamar Abu Bakar",
                    currentType = ReadingType.IQRO.name,
                    currentLevel = 4L,
                    currentPage = 18L,
                    notes = "Makhraj huruf shad dan tha sudah meningkat"
                ),
                Student(
                    name = "Muhammad Rizki Pratama",
                    gender = "Laki-laki",
                    roomOrClass = "Kamar Umar bin Khattab",
                    currentType = ReadingType.QURAN.name,
                    currentLevel = 2L,
                    currentPage = 35L,
                    notes = "Hafalan surah Al-Baqarah lancar, tajwid mad jaiz baik"
                ),
                Student(
                    name = "Fatimah Azzahra Putri",
                    gender = "Perempuan",
                    roomOrClass = "Kamar Khadijah",
                    currentType = ReadingType.QURAN.name,
                    currentLevel = 5L,
                    currentPage = 98L,
                    notes = "Bacaan tartil dan tajwid gunnah rapi"
                ),
                Student(
                    name = "Aisyah Nabila Syifa",
                    gender = "Perempuan",
                    roomOrClass = "Kamar Aisyah",
                    currentType = ReadingType.IQRO.name,
                    currentLevel = 2L,
                    currentPage = 12L,
                    notes = "Perlu pembiasaan pengucapan huruf 'ain dan ghain"
                ),
                Student(
                    name = "Bilal Hidayatulloh",
                    gender = "Laki-laki",
                    roomOrClass = "Kamar Ali bin Abi Thalib",
                    currentType = ReadingType.IQRO.name,
                    currentLevel = 6L,
                    currentPage = 25L,
                    notes = "Segera persiapan tashih masuk Al-Qur'an Juz 1"
                )
            )

            for (sample in sampleStudents) {
                val addRes = repository.addStudent(currentUserId, sample)
                if (addRes.isSuccess) {
                    val sId = addRes.getOrNull() ?: continue
                    // Add sample historical records
                    for (daysAgo in listOf(4, 2, 0)) {
                        cal.time = Date()
                        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
                        val recDate = dateFormat.format(cal.time)

                        val status = when (daysAgo) {
                            4 -> FluencyStatus.KURANG_LANCAR.name
                            2 -> FluencyStatus.LANCAR.name
                            else -> FluencyStatus.LANCAR.name
                        }

                        val note = when {
                            sample.currentType == ReadingType.IQRO.name -> "Latihan baris 1-5 halaman ${sample.currentPage - daysAgo}"
                            else -> "Membaca dengan tartil dan menjaga waqaf"
                        }

                        repository.addRecord(
                            userId = currentUserId,
                            record = NgajiRecord(
                                studentId = sId,
                                studentName = sample.name,
                                type = sample.currentType,
                                level = sample.currentLevel,
                                page = maxOf(1L, sample.currentPage - daysAgo),
                                surahName = if (sample.currentType == ReadingType.QURAN.name) "Al-Baqarah" else "",
                                verse = if (sample.currentType == ReadingType.QURAN.name) "Ayat 1-15" else "Baris 1-6",
                                status = status,
                                notes = note,
                                teacherName = "Ustadz Abdullah",
                                date = recDate
                            ),
                            updateStudentProgress = false
                        )
                    }
                }
            }
        }
    }

    companion object {
        private const val TAG = "NgajiViewModel"
    }
}
