package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FluencyStatus
import com.example.data.model.NgajiRecord
import com.example.data.model.ReadingType
import com.example.data.model.Student
import com.example.ui.theme.DesertGold
import com.example.ui.theme.FluencyBelumLancar
import com.example.ui.theme.FluencyKurangLancar
import com.example.ui.theme.FluencyLancar
import com.example.ui.theme.IslamicEmerald
import com.example.ui.viewmodel.NgajiViewModel
import com.example.ui.viewmodel.ReportPeriod
import com.example.ui.viewmodel.UiState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: NgajiViewModel
) {
    val reportPeriod by viewModel.reportPeriod.collectAsState()
    val studentsState by viewModel.studentsState.collectAsState()
    val recordsState by viewModel.recordsState.collectAsState()

    val allStudents = (studentsState as? UiState.Success)?.data ?: emptyList()
    val allRecords = (recordsState as? UiState.Success)?.data ?: emptyList()

    // Filter records based on selected period
    val filteredRecords = rememberFilteredRecords(allRecords, reportPeriod)

    val totalRecords = filteredRecords.size
    val activeStudentIds = filteredRecords.map { it.studentId }.distinct()
    val lancarCount = filteredRecords.count { it.status == FluencyStatus.LANCAR.name }
    val kurangCount = filteredRecords.count { it.status == FluencyStatus.KURANG_LANCAR.name }
    val belumCount = filteredRecords.count { it.status == FluencyStatus.BELUM_LANCAR.name }
    val lancarPercentage = if (totalRecords > 0) (lancarCount * 100) / totalRecords else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("reports_screen")
    ) {
        item {
            Spacer(modifier = Modifier.height(14.dp))

            // Period Selector Card
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
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = IslamicEmerald,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Laporan & Evaluasi Berkala",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = IslamicEmerald
                            )
                            Text(
                                text = "Rekap kemajuan mengaji santri SRT 1 Kediri",
                                fontSize = 11.5.sp,
                                color = Color(0xFF64746A)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Period Filter Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReportPeriod.values().forEach { period ->
                            val isSelected = reportPeriod == period
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setReportPeriod(period) },
                                label = { Text(period.label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IslamicEmerald,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF6F8F5),
                                    labelColor = Color(0xFF3B4A41)
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = isSelected,
                                    borderColor = if (isSelected) IslamicEmerald else Color(0xFFD4E0D7)
                                ),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Overall Summary KPI Cards
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KpiCard(
                    title = "Total Setoran",
                    value = "$totalRecords",
                    subtitle = "Sesi Mengaji",
                    icon = Icons.Default.DateRange,
                    tint = IslamicEmerald,
                    bgColor = Color(0xFFE8F5E9),
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Santri Aktif",
                    value = "${activeStudentIds.size}",
                    subtitle = "Dari ${allStudents.size} santri",
                    icon = Icons.Default.Groups,
                    tint = DesertGold,
                    bgColor = Color(0xFFFFF8E1),
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Kelancaran",
                    value = "$lancarPercentage%",
                    subtitle = "$lancarCount Lancar",
                    icon = Icons.Default.TrendingUp,
                    tint = FluencyLancar,
                    bgColor = Color(0xFFE8F5E9),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Visual Breakdown Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Distribusi Tingkat Kelancaran (${reportPeriod.label})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color(0xFF223128)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Lancar bar
                    FluencyProgressBar(
                        label = "Lancar",
                        count = lancarCount,
                        total = totalRecords,
                        color = FluencyLancar
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Kurang Lancar bar
                    FluencyProgressBar(
                        label = "Kurang Lancar",
                        count = kurangCount,
                        total = totalRecords,
                        color = FluencyKurangLancar
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Belum Lancar bar
                    FluencyProgressBar(
                        label = "Belum Lancar",
                        count = belumCount,
                        total = totalRecords,
                        color = FluencyBelumLancar
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Rekap Per Santri Table Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Rekap Keaktifan & Progres Santri",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = IslamicEmerald
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "${allStudents.size} Anak Asuh",
                    fontSize = 11.5.sp,
                    color = Color(0xFF67776E)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        if (allStudents.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Belum ada santri terdaftar.",
                        fontSize = 13.sp,
                        color = Color(0xFF64746A),
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(allStudents, key = { it.id }) { student ->
                val studentPeriodRecords = filteredRecords.filter { it.studentId == student.id }
                StudentReportRow(
                    student = student,
                    recordsInPeriod = studentPeriodRecords
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun KpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, fontSize = 11.sp, color = Color(0xFF6B7B71))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E2822))
            Text(text = subtitle, fontSize = 9.5.sp, color = Color(0xFF86968B))
        }
    }
}

@Composable
private fun FluencyProgressBar(
    label: String,
    count: Int,
    total: Int,
    color: Color
) {
    val percent = if (total > 0) (count * 100) / total else 0
    val fraction = if (total > 0) count.toFloat() / total else 0f

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, fontSize = 12.sp, color = Color(0xFF3B4A41), fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "$count sesi ($percent%)", fontSize = 11.5.sp, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = Color(0xFFEEF3EF)
        )
    }
}

@Composable
private fun StudentReportRow(
    student: Student,
    recordsInPeriod: List<NgajiRecord>
) {
    val lancarInPeriod = recordsInPeriod.count { it.status == FluencyStatus.LANCAR.name }
    val sessionCount = recordsInPeriod.size

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
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = student.name.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = IslamicEmerald
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = Color(0xFF1E2822)
                )
                Text(
                    text = "${student.roomOrClass} • Posisi: ${student.currentType} ${student.currentLevel} Hal ${student.currentPage}",
                    fontSize = 11.sp,
                    color = Color(0xFF64746A)
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    color = if (sessionCount > 0) Color(0xFFE8F5E9) else Color(0xFFF3F5F3),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = if (sessionCount > 0) "$sessionCount x Simak ($lancarInPeriod Lancar)" else "Belum Menyetor",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (sessionCount > 0) IslamicEmerald else Color(0xFF7D8C83),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun rememberFilteredRecords(
    allRecords: List<NgajiRecord>,
    period: ReportPeriod
): List<NgajiRecord> {
    if (period == ReportPeriod.SEMUA) return allRecords

    val cal = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val startDateStr = when (period) {
        ReportPeriod.PEKAN_INI -> {
            cal.time = Date()
            cal.add(Calendar.DAY_OF_YEAR, -7)
            dateFormat.format(cal.time)
        }
        ReportPeriod.BULAN_INI -> {
            cal.time = Date()
            cal.add(Calendar.DAY_OF_YEAR, -30)
            dateFormat.format(cal.time)
        }
        else -> "1970-01-01"
    }

    return allRecords.filter { it.date >= startDateStr }
}
