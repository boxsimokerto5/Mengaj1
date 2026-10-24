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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.FluencyStatus
import com.example.data.model.NgajiRecord
import com.example.data.model.ReadingType
import com.example.data.model.Student
import com.example.ui.theme.DesertGold
import com.example.ui.theme.FluencyBelumLancar
import com.example.ui.theme.FluencyBelumLancarContainer
import com.example.ui.theme.FluencyKurangLancar
import com.example.ui.theme.FluencyKurangLancarContainer
import com.example.ui.theme.FluencyLancar
import com.example.ui.theme.FluencyLancarContainer
import com.example.ui.theme.IslamicEmerald
import com.example.ui.viewmodel.NgajiViewModel
import com.example.ui.viewmodel.UiState
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordInputScreen(
    viewModel: NgajiViewModel
) {
    val studentsState by viewModel.studentsState.collectAsState()
    val recordsState by viewModel.recordsState.collectAsState()
    val quickLogStudent by viewModel.quickLogStudent.collectAsState()

    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    var currentType by remember { mutableStateOf(ReadingType.IQRO.name) }
    var currentLevel by remember { mutableLongStateOf(1L) }
    var currentPage by remember { mutableLongStateOf(1L) }
    var surahName by remember { mutableStateOf("") }
    var verse by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf(FluencyStatus.LANCAR.name) }
    var notes by remember { mutableStateOf("") }
    var teacherName by remember {
        mutableStateOf(
            Firebase.auth.currentUser?.displayName ?: Firebase.auth.currentUser?.email?.substringBefore("@") ?: "Ustadz Pembina"
        )
    }
    var recordDate by remember {
        mutableStateOf(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
    }

    var showStudentDropdown by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Synchronize if user came via Quick Log button from a student card
    LaunchedEffect(quickLogStudent) {
        quickLogStudent?.let { student ->
            selectedStudent = student
            currentType = student.currentType
            currentLevel = student.currentLevel
            currentPage = student.currentPage
            viewModel.clearQuickLogStudent()
        }
    }

    // Auto-select first student if none selected
    LaunchedEffect(studentsState) {
        if (selectedStudent == null && studentsState is UiState.Success) {
            val list = (studentsState as UiState.Success<List<Student>>).data
            if (list.isNotEmpty()) {
                val first = list.first()
                selectedStudent = first
                currentType = first.currentType
                currentLevel = first.currentLevel
                currentPage = first.currentPage
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("record_input_screen")
    ) {
        item {
            Spacer(modifier = Modifier.height(14.dp))

            // Card Form
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoStories,
                                contentDescription = null,
                                tint = IslamicEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Form Setoran & Evaluasi Mengaji",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = IslamicEmerald
                            )
                            Text(
                                text = "Catat kelancaran hafalan / bacaan anak asuh",
                                fontSize = 11.5.sp,
                                color = Color(0xFF6A7B70)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 1. Pilih Anak Asuh (Student Selector)
                    Text("Pilih Anak Asuh / Santri *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3B4A41))
                    Spacer(modifier = Modifier.height(4.dp))

                    val students = (studentsState as? UiState.Success)?.data ?: emptyList()

                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showStudentDropdown = true }
                                .testTag("select_student_dropdown"),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD0DCD3))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = IslamicEmerald,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = selectedStudent?.name ?: "Pilih Santri...",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedStudent != null) Color(0xFF1E2822) else Color(0xFF86968B)
                                    )
                                    if (selectedStudent != null) {
                                        Text(
                                            text = "${selectedStudent!!.gender} • ${selectedStudent!!.roomOrClass} (Posisi: ${selectedStudent!!.currentType} ${selectedStudent!!.currentLevel} Hal ${selectedStudent!!.currentPage})",
                                            fontSize = 11.sp,
                                            color = Color(0xFF5A6B60)
                                        )
                                    }
                                }
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xFF5A6B60))
                            }
                        }

                        DropdownMenu(
                            expanded = showStudentDropdown,
                            onDismissRequest = { showStudentDropdown = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            students.forEach { s ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(s.name, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                                            Text(
                                                "${s.gender} • ${s.roomOrClass} • Saat ini: ${s.currentType} ${s.currentLevel} Hal ${s.currentPage}",
                                                fontSize = 11.sp,
                                                color = Color(0xFF687A6F)
                                            )
                                        }
                                    },
                                    onClick = {
                                        selectedStudent = s
                                        currentType = s.currentType
                                        currentLevel = s.currentLevel
                                        currentPage = s.currentPage
                                        showStudentDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 2. Kategori Bacaan (Iqro vs Quran)
                    Text("Materi Bacaan Yang Disimak:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3B4A41))
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        val isIqro = currentType == ReadingType.IQRO.name
                        val isQuran = currentType == ReadingType.QURAN.name

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clickable {
                                    currentType = ReadingType.IQRO.name
                                    if (currentLevel > 6L) currentLevel = 1L
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isIqro) Color(0xFFE8F5E9) else Color(0xFFF6F8F5),
                            border = androidx.compose.foundation.BorderStroke(
                                if (isIqro) 1.5.dp else 1.dp,
                                if (isIqro) IslamicEmerald else Color(0xFFD0DCD3)
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = if (isIqro) IslamicEmerald else Color(0xFF7A8B80),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Iqro (Jilid 1 - 6)",
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isIqro) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isIqro) IslamicEmerald else Color(0xFF5A6B60)
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clickable {
                                    currentType = ReadingType.QURAN.name
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isQuran) Color(0xFFE8F5E9) else Color(0xFFF6F8F5),
                            border = androidx.compose.foundation.BorderStroke(
                                if (isQuran) 1.5.dp else 1.dp,
                                if (isQuran) IslamicEmerald else Color(0xFFD0DCD3)
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = null,
                                    tint = if (isQuran) IslamicEmerald else Color(0xFF7A8B80),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Al-Qur'an (Juz 1 - 30)",
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isQuran) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isQuran) IslamicEmerald else Color(0xFF5A6B60)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 3. Tingkat (Jilid / Juz) dan Halaman
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = currentLevel.toString(),
                            onValueChange = { str ->
                                val n = str.filter { it.isDigit() }.toLongOrNull() ?: 1L
                                currentLevel = if (currentType == ReadingType.IQRO.name) n.coerceIn(1L, 6L) else n.coerceIn(1L, 30L)
                            },
                            label = { Text(if (currentType == ReadingType.IQRO.name) "Iqro Jilid (1-6)" else "Juz (1-30)") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_level"),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = currentPage.toString(),
                            onValueChange = { str ->
                                currentPage = str.filter { it.isDigit() }.toLongOrNull()?.coerceIn(1L, 1000L) ?: 1L
                            },
                            label = { Text("Halaman") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_page"),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    // Optional Surah & Verse if Al-Qur'an
                    if (currentType == ReadingType.QURAN.name) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = surahName,
                                onValueChange = { surahName = it },
                                label = { Text("Nama Surah (Opsional)") },
                                placeholder = { Text("Misal: Al-Baqarah") },
                                singleLine = true,
                                modifier = Modifier.weight(1.3f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            OutlinedTextField(
                                value = verse,
                                onValueChange = { verse = it },
                                label = { Text("Ayat (Opsional)") },
                                placeholder = { Text("Misal: 1-25") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 4. Penilaian Kelancaran (Status: Lancar / Kurang Lancar / Belum Lancar)
                    Text("Penilaian Tingkat Kelancaran Bacaan *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3B4A41))
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusChoiceButton(
                            label = "Lancar",
                            isSelected = selectedStatus == FluencyStatus.LANCAR.name,
                            activeColor = FluencyLancar,
                            activeBg = FluencyLancarContainer,
                            onClick = { selectedStatus = FluencyStatus.LANCAR.name },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("status_lancar_button")
                        )

                        StatusChoiceButton(
                            label = "Kurang Lancar",
                            isSelected = selectedStatus == FluencyStatus.KURANG_LANCAR.name,
                            activeColor = FluencyKurangLancar,
                            activeBg = FluencyKurangLancarContainer,
                            onClick = { selectedStatus = FluencyStatus.KURANG_LANCAR.name },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("status_kurang_lancar_button")
                        )

                        StatusChoiceButton(
                            label = "Belum Lancar",
                            isSelected = selectedStatus == FluencyStatus.BELUM_LANCAR.name,
                            activeColor = FluencyBelumLancar,
                            activeBg = FluencyBelumLancarContainer,
                            onClick = { selectedStatus = FluencyStatus.BELUM_LANCAR.name },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("status_belum_lancar_button")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5. Catatan / Bimbingan Pembimbing
                    Text("Catatan Pembimbing & Evaluasi Tajwid:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF3B4A41))
                    Spacer(modifier = Modifier.height(4.dp))

                    // Quick suggestion chips
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val suggestions = listOf(
                            "Makhraj Huruf",
                            "Tajwid Mad Thabi'i",
                            "Ghunnah & Dengung",
                            "Tartil & Tenang",
                            "Lancar Sekali",
                            "Ulangi Halaman"
                        )
                        items(suggestions) { s ->
                            Surface(
                                modifier = Modifier.clickable {
                                    notes = if (notes.isBlank()) s else "$notes, $s"
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF1F6F2),
                                border = androidx.compose.foundation.BorderStroke(0.8.dp, Color(0xFFCAD8CE))
                            ) {
                                Text(
                                    text = "+ $s",
                                    fontSize = 11.sp,
                                    color = Color(0xFF2E4D3A),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        placeholder = { Text("Tuliskan evaluasi makhraj, tajwid, adab mengaji...", fontSize = 13.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_notes"),
                        maxLines = 3,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 6. Tanggal & Nama Penyimak
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = recordDate,
                            onValueChange = { recordDate = it },
                            label = { Text("Tanggal (YYYY-MM-DD)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = teacherName,
                            onValueChange = { teacherName = it },
                            label = { Text("Ustadz Penyimak") },
                            singleLine = true,
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }

                    if (validationError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = validationError!!,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 7. Tombol Simpan
                    Button(
                        onClick = {
                            val student = selectedStudent
                            if (student == null) {
                                validationError = "Silakan pilih anak asuh terlebih dahulu"
                                return@Button
                            }
                            validationError = null
                            viewModel.addRecord(
                                student = student,
                                type = currentType,
                                level = currentLevel,
                                page = currentPage,
                                surahName = surahName,
                                verse = verse,
                                status = selectedStatus,
                                notes = notes,
                                teacherName = teacherName,
                                date = recordDate,
                                onComplete = { success ->
                                    if (success) {
                                        // Reset fields for next entry
                                        notes = ""
                                        surahName = ""
                                        verse = ""
                                    }
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_record_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simpan & Perbarui Progres Santri", fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Recent Logs Section
            Text(
                text = "Riwayat Catatan Mengaji Terbaru",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = IslamicEmerald
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Recent records stream
        when (val recState = recordsState) {
            is UiState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = IslamicEmerald, modifier = Modifier.size(28.dp))
                    }
                }
            }
            is UiState.Error -> {
                item {
                    Text(text = recState.message, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
            is UiState.Success -> {
                val recent = recState.data.sortedByDescending { it.date }.take(10)
                if (recent.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "Belum ada catatan tersimpan. Silakan simpan catatan di atas.",
                                modifier = Modifier.padding(16.dp),
                                fontSize = 12.5.sp,
                                color = Color(0xFF6B7B71)
                            )
                        }
                    }
                } else {
                    items(recent, key = { it.id }) { r ->
                        RecentRecordItem(
                            record = r,
                            onDelete = { viewModel.deleteRecord(r) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun StatusChoiceButton(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    activeBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) activeBg else Color(0xFFF7FAF7),
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) activeColor else Color(0xFFD3E0D7)
        )
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(activeColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) activeColor else Color(0xFF4A5A50)
            )
        }
    }
}

@Composable
private fun RecentRecordItem(
    record: NgajiRecord,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val statusColor = when (record.status) {
                FluencyStatus.LANCAR.name -> FluencyLancar
                FluencyStatus.KURANG_LANCAR.name -> FluencyKurangLancar
                else -> FluencyBelumLancar
            }

            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = record.studentName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = Color(0xFF1E2822)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = record.date,
                        fontSize = 11.sp,
                        color = Color(0xFF7A8B80)
                    )
                }

                val levelStr = if (record.type == ReadingType.IQRO.name) "Iqro Jilid ${record.level} Hal ${record.page}" else "Al-Qur'an Juz ${record.level} Hal ${record.page}"
                Text(
                    text = levelStr,
                    fontSize = 12.sp,
                    color = IslamicEmerald,
                    fontWeight = FontWeight.SemiBold
                )

                if (record.notes.isNotBlank()) {
                    Text(
                        text = "Catatan: ${record.notes}",
                        fontSize = 11.5.sp,
                        color = Color(0xFF5A6B60),
                        maxLines = 1
                    )
                }
            }

            Surface(
                color = when (record.status) {
                    FluencyStatus.LANCAR.name -> FluencyLancarContainer
                    FluencyStatus.KURANG_LANCAR.name -> FluencyKurangLancarContainer
                    else -> FluencyBelumLancarContainer
                },
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = when (record.status) {
                        FluencyStatus.LANCAR.name -> "Lancar"
                        FluencyStatus.KURANG_LANCAR.name -> "Kurang"
                        else -> "Belum"
                    },
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Hapus catatan",
                    tint = Color(0xFFB0BEC5),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
