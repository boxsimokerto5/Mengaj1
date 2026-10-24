package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReadingType
import com.example.data.model.Student
import com.example.ui.theme.DesertGold
import com.example.ui.theme.IslamicEmerald
import com.example.ui.viewmodel.NgajiViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun StudentsScreen(
    viewModel: NgajiViewModel
) {
    val studentsState by viewModel.studentsState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterType by viewModel.filterType.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var studentToEdit by remember { mutableStateOf<Student?>(null) }
    var studentToDelete by remember { mutableStateOf<Student?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .testTag("students_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_student_input"),
                placeholder = { Text("Cari nama santri / asrama...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = IslamicEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Hapus pencarian")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IslamicEmerald,
                    unfocusedBorderColor = Color(0xFFD5DFD8),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf("SEMUA" to "Semua Santri", "IQRO" to "Iqro (Jilid 1-6)", "QURAN" to "Al-Qur'an (30 Juz)")
                items(filters) { (key, label) ->
                    val isSelected = filterType == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setFilterType(key) },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IslamicEmerald,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Color(0xFF3B4A41)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) IslamicEmerald else Color(0xFFD5DFD8)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (val state = studentsState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = IslamicEmerald)
                    }
                }
                is UiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 13.sp
                        )
                    }
                }
                is UiState.Success -> {
                    val allStudents = state.data
                    val filtered = allStudents.filter { student ->
                        val matchesSearch = student.name.contains(searchQuery, ignoreCase = true) ||
                                student.roomOrClass.contains(searchQuery, ignoreCase = true)
                        val matchesType = when (filterType) {
                            "IQRO" -> student.currentType == ReadingType.IQRO.name
                            "QURAN" -> student.currentType == ReadingType.QURAN.name
                            else -> true
                        }
                        matchesSearch && matchesType
                    }

                    // Count summary row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Menampilkan ${filtered.size} dari ${allStudents.size} anak asuh",
                            fontSize = 12.sp,
                            color = Color(0xFF5A6B60),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Iqro: ${allStudents.count { it.currentType == ReadingType.IQRO.name }} | Quran: ${allStudents.count { it.currentType == ReadingType.QURAN.name }}",
                                fontSize = 11.sp,
                                color = IslamicEmerald,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (filtered.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = null,
                                    tint = Color(0xFFA2B2A7),
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (searchQuery.isNotBlank()) "Tidak ada santri yang cocok dengan pencarian" else "Belum ada santri terdaftar",
                                    fontSize = 13.sp,
                                    color = Color(0xFF5A6B60)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filtered, key = { it.id }) { student ->
                                StudentCard(
                                    student = student,
                                    onQuickLog = { viewModel.startQuickLog(student) },
                                    onViewDetail = { viewModel.selectStudentForDetail(student) },
                                    onEdit = { studentToEdit = student },
                                    onDelete = { studentToDelete = student }
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.height(80.dp)) // padding for FAB
                            }
                        }
                    }
                }
            }
        }

        // FAB: Add New Student
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_student_fab"),
            containerColor = IslamicEmerald,
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tambah Santri", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }

    // Add Student Dialog
    if (showAddDialog) {
        StudentFormDialog(
            title = "Tambah Anak Asuh Baru",
            initialStudent = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, gender, roomOrClass, type, level, page, notes ->
                viewModel.addStudent(
                    name = name,
                    gender = gender,
                    roomOrClass = roomOrClass,
                    currentType = type,
                    currentLevel = level,
                    currentPage = page,
                    notes = notes
                )
                showAddDialog = false
            }
        )
    }

    // Edit Student Dialog
    studentToEdit?.let { student ->
        StudentFormDialog(
            title = "Edit Data Santri",
            initialStudent = student,
            onDismiss = { studentToEdit = null },
            onConfirm = { name, gender, roomOrClass, type, level, page, notes ->
                viewModel.updateStudent(
                    student.copy(
                        name = name,
                        gender = gender,
                        roomOrClass = roomOrClass,
                        currentType = type,
                        currentLevel = level,
                        currentPage = page,
                        notes = notes
                    )
                )
                studentToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog
    studentToDelete?.let { student ->
        AlertDialog(
            onDismissRequest = { studentToDelete = null },
            title = { Text("Hapus Data Santri?", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus data ${student.name}? Riwayat bacaan terkait akan tetap tersimpan.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteStudent(student)
                        studentToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { studentToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun StudentCard(
    student: Student,
    onQuickLog: () -> Unit,
    onViewDetail: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetail() }
            .testTag("student_card_${student.id}"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Avatar circle with initial
                val avatarBg = if (student.gender == "Perempuan") Color(0xFFFCE4EC) else Color(0xFFE8F5E9)
                val avatarText = if (student.gender == "Perempuan") Color(0xFFC2185B) else IslamicEmerald
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(avatarBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = student.name.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = avatarText
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = student.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF1E2822)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = student.gender,
                            fontSize = 11.5.sp,
                            color = Color(0xFF5A6B60)
                        )
                        if (student.roomOrClass.isNotBlank()) {
                            Text(text = " • ", fontSize = 11.sp, color = Color(0xFFA2B2A7))
                            Text(
                                text = student.roomOrClass,
                                fontSize = 11.5.sp,
                                color = Color(0xFF5A6B60)
                            )
                        }
                    }
                }

                // Edit & Delete actions
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Santri",
                        tint = Color(0xFF75857B),
                        modifier = Modifier.size(17.dp)
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus Santri",
                        tint = Color(0xFFC62828),
                        modifier = Modifier.size(17.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Reading Progress Badge
            Surface(
                color = if (student.currentType == ReadingType.IQRO.name) Color(0xFFFFF8E1) else Color(0xFFE8F5E9),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (student.currentType == ReadingType.IQRO.name) DesertGold.copy(alpha = 0.5f) else IslamicEmerald.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (student.currentType == ReadingType.IQRO.name) Icons.Default.MenuBook else Icons.Default.AutoStories,
                        contentDescription = null,
                        tint = if (student.currentType == ReadingType.IQRO.name) Color(0xFFB78103) else IslamicEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (student.currentType == ReadingType.IQRO.name) "Iqro Jilid ${student.currentLevel} • Halaman ${student.currentPage}" else "Al-Qur'an Juz ${student.currentLevel} • Halaman ${student.currentPage}",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (student.currentType == ReadingType.IQRO.name) Color(0xFF6F5000) else IslamicEmerald
                    )
                }
            }

            if (student.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Catatan: ${student.notes}",
                    fontSize = 11.5.sp,
                    color = Color(0xFF6D7E73),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onQuickLog,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("quick_log_button_${student.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Catat Ngaji", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                OutlinedButton(
                    onClick = onViewDetail,
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("view_card_button_${student.id}"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = IslamicEmerald),
                    border = androidx.compose.foundation.BorderStroke(1.dp, IslamicEmerald)
                ) {
                    Text("Evaluasi & PDF", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentFormDialog(
    title: String,
    initialStudent: Student?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, gender: String, roomOrClass: String, type: String, level: Long, page: Long, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(initialStudent?.name ?: "") }
    var gender by remember { mutableStateOf(initialStudent?.gender ?: "Laki-laki") }
    var roomOrClass by remember { mutableStateOf(initialStudent?.roomOrClass ?: "Kamar Abu Bakar") }
    var currentType by remember { mutableStateOf(initialStudent?.currentType ?: ReadingType.IQRO.name) }
    var currentLevel by remember { mutableLongStateOf(initialStudent?.currentLevel ?: 1L) }
    var currentPage by remember { mutableLongStateOf(initialStudent?.currentPage ?: 1L) }
    var notes by remember { mutableStateOf(initialStudent?.notes ?: "") }

    var nameError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, color = IslamicEmerald) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Nama Lengkap Santri *") },
                    isError = nameError,
                    supportingText = if (nameError) { { Text("Nama tidak boleh kosong") } } else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Gender Toggle
                Text("Jenis Kelamin:", fontSize = 12.sp, color = Color(0xFF5A6B60))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Laki-laki", "Perempuan").forEach { g ->
                        FilterChip(
                            selected = gender == g,
                            onClick = { gender = g },
                            label = { Text(g, fontSize = 12.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = roomOrClass,
                    onValueChange = { roomOrClass = it },
                    label = { Text("Kamar / Asrama / Kelas") },
                    placeholder = { Text("Contoh: Kamar Abu Bakar / Kelas 4") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Category: Iqro vs Quran
                Text("Kategori Pembelajaran Awal:", fontSize = 12.sp, color = Color(0xFF5A6B60))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = currentType == ReadingType.IQRO.name,
                        onClick = {
                            currentType = ReadingType.IQRO.name
                            if (currentLevel > 6L) currentLevel = 1L
                        },
                        label = { Text("Iqro (1-6)", fontSize = 12.sp) }
                    )
                    FilterChip(
                        selected = currentType == ReadingType.QURAN.name,
                        onClick = {
                            currentType = ReadingType.QURAN.name
                        },
                        label = { Text("Al-Qur'an (Juz 1-30)", fontSize = 12.sp) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = currentLevel.toString(),
                        onValueChange = { str ->
                            val num = str.filter { it.isDigit() }.toLongOrNull() ?: 1L
                            currentLevel = if (currentType == ReadingType.IQRO.name) num.coerceIn(1L, 6L) else num.coerceIn(1L, 30L)
                        },
                        label = { Text(if (currentType == ReadingType.IQRO.name) "Jilid (1-6)" else "Juz (1-30)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = currentPage.toString(),
                        onValueChange = { str ->
                            currentPage = str.filter { it.isDigit() }.toLongOrNull()?.coerceIn(1L, 1000L) ?: 1L
                        },
                        label = { Text("Halaman") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan Tambahan (Opsional)") },
                    placeholder = { Text("Misal: Perlu perhatian pada tajwid...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank()) {
                        nameError = true
                    } else {
                        onConfirm(name, gender, roomOrClass, currentType, currentLevel, currentPage, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald)
            ) {
                Text("Simpan Data")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
