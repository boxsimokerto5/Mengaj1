package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
import com.example.ui.theme.FluencyBelumLancarContainer
import com.example.ui.theme.FluencyKurangLancar
import com.example.ui.theme.FluencyKurangLancarContainer
import com.example.ui.theme.FluencyLancar
import com.example.ui.theme.FluencyLancarContainer
import com.example.ui.theme.IslamicEmerald
import com.example.util.PdfReportGenerator
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.NgajiViewModel
import com.example.ui.viewmodel.UiState

@Composable
fun StudentDetailAndCardScreen(
    viewModel: NgajiViewModel
) {
    val context = LocalContext.current
    val selectedStudent by viewModel.selectedStudent.collectAsState()
    val studentsState by viewModel.studentsState.collectAsState()
    val recordsState by viewModel.recordsState.collectAsState()

    var isGeneratingPdf by remember { mutableStateOf(false) }

    val allStudents = (studentsState as? UiState.Success)?.data ?: emptyList()
    val allRecords = (recordsState as? UiState.Success)?.data ?: emptyList()

    val currentStudent = selectedStudent ?: allStudents.firstOrNull()

    if (currentStudent == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color(0xFFA5B5AB),
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Pilih anak asuh di tab 'Santri' untuk melihat evaluasi progres dan mencetak kartu ngaji.",
                    fontSize = 13.5.sp,
                    color = Color(0xFF5A6B60),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.setActiveTab(MainTab.SANTRI) },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald)
                ) {
                    Text("Buka Daftar Santri")
                }
            }
        }
        return
    }

    val studentRecords = allRecords.filter { it.studentId == currentStudent.id }.sortedByDescending { it.date }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("student_detail_card_screen")
    ) {
        item {
            Spacer(modifier = Modifier.height(14.dp))

            // Switch Student Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Santri Terpilih:",
                        fontSize = 11.5.sp,
                        color = Color(0xFF67776E),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentStudent.name,
                        fontSize = 13.sp,
                        color = IslamicEmerald,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = { viewModel.setActiveTab(MainTab.SANTRI) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text("Ganti Santri", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Student Profile & Progress Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val avatarBg = if (currentStudent.gender == "Perempuan") Color(0xFFFCE4EC) else Color(0xFFE8F5E9)
                        val avatarColor = if (currentStudent.gender == "Perempuan") Color(0xFFC2185B) else IslamicEmerald
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(avatarBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentStudent.name.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = avatarColor
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = currentStudent.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color(0xFF1E2822)
                            )
                            Text(
                                text = "${currentStudent.gender} • ${currentStudent.roomOrClass}",
                                fontSize = 12.sp,
                                color = Color(0xFF64746A)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Divider(color = Color(0xFFE5EDE7))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Target & Progress Bar
                    val isIqro = currentStudent.currentType == ReadingType.IQRO.name
                    val maxTarget = if (isIqro) 6f else 30f
                    val progressFraction = (currentStudent.currentLevel.toFloat() / maxTarget).coerceIn(0f, 1f)
                    val progressPercent = (progressFraction * 100).toInt()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isIqro) "Target: Khatam Iqro (Jilid 6)" else "Target: Khatam 30 Juz Al-Qur'an",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E3D34)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "$progressPercent% Tercapai",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = IslamicEmerald
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = IslamicEmerald,
                        trackColor = Color(0xFFE2EDE5)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isIqro) "Posisi saat ini: Iqro Jilid ${currentStudent.currentLevel} Halaman ${currentStudent.currentPage}" else "Posisi saat ini: Al-Qur'an Juz ${currentStudent.currentLevel} Halaman ${currentStudent.currentPage}",
                        fontSize = 12.sp,
                        color = IslamicEmerald,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Statistics Grid
                    val totalSetoran = studentRecords.size
                    val lancarCount = studentRecords.count { it.status == FluencyStatus.LANCAR.name }
                    val kurangCount = studentRecords.count { it.status == FluencyStatus.KURANG_LANCAR.name }
                    val belumCount = studentRecords.count { it.status == FluencyStatus.BELUM_LANCAR.name }
                    val lancarRatio = if (totalSetoran > 0) (lancarCount * 100) / totalSetoran else 0

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MetricCard(
                            title = "Total Sesi",
                            value = "$totalSetoran",
                            subtitle = "Setoran",
                            containerColor = Color(0xFFF1F6F2),
                            textColor = IslamicEmerald,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Kelancaran",
                            value = "$lancarRatio%",
                            subtitle = "$lancarCount Lancar",
                            containerColor = FluencyLancarContainer,
                            textColor = FluencyLancar,
                            modifier = Modifier.weight(1f)
                        )
                        MetricCard(
                            title = "Perhatian",
                            value = "${kurangCount + belumCount}",
                            subtitle = "Perlu Bimbingan",
                            containerColor = FluencyKurangLancarContainer,
                            textColor = FluencyKurangLancar,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // PDF Print Card Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F5132)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = DesertGold,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Kartu Kendali Mengaji (PDF Resmi)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Format A4 rapi ber-kop Sekolah Rakyat SRT 1 Kediri",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Print / View Button
                        Button(
                            onClick = {
                                isGeneratingPdf = true
                                try {
                                    val pdfFile = PdfReportGenerator.generateStudentCardPdf(
                                        context = context,
                                        student = currentStudent,
                                        records = studentRecords
                                    )
                                    PdfReportGenerator.viewPdf(context, pdfFile)
                                    Toast.makeText(context, "Membuka Kartu Ngaji PDF...", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Gagal membuat PDF: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isGeneratingPdf = false
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("print_card_pdf_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DesertGold,
                                contentColor = Color(0xFF2C2200)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isGeneratingPdf
                        ) {
                            if (isGeneratingPdf) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color(0xFF2C2200))
                            } else {
                                Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Cetak / Buka", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                            }
                        }

                        // Share Button
                        OutlinedButton(
                            onClick = {
                                isGeneratingPdf = true
                                try {
                                    val pdfFile = PdfReportGenerator.generateStudentCardPdf(
                                        context = context,
                                        student = currentStudent,
                                        records = studentRecords
                                    )
                                    PdfReportGenerator.sharePdf(context, pdfFile)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Gagal membagikan PDF: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isGeneratingPdf = false
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("share_card_pdf_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.8f)),
                            shape = RoundedCornerShape(10.dp),
                            enabled = !isGeneratingPdf
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bagikan PDF", fontWeight = FontWeight.SemiBold, fontSize = 12.5.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Timeline Header
            Text(
                text = "Histori & Riwayat Bacaan Anak Asuh (${studentRecords.size} Catatan)",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = IslamicEmerald
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (studentRecords.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Belum ada catatan setoran bacaan untuk ${currentStudent.name}.",
                            fontSize = 13.sp,
                            color = Color(0xFF64746A),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.startQuickLog(currentStudent) },
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald)
                        ) {
                            Text("Tambah Catatan Ngaji")
                        }
                    }
                }
            }
        } else {
            items(studentRecords, key = { it.id }) { record ->
                RecordTimelineItem(record = record)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    containerColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 10.5.sp, color = Color(0xFF5A6B60))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = textColor)
            Text(text = subtitle, fontSize = 9.5.sp, color = textColor)
        }
    }
}

@Composable
private fun RecordTimelineItem(
    record: NgajiRecord
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = when (record.status) {
                        FluencyStatus.LANCAR.name -> FluencyLancarContainer
                        FluencyStatus.KURANG_LANCAR.name -> FluencyKurangLancarContainer
                        else -> FluencyBelumLancarContainer
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    val statusText = when (record.status) {
                        FluencyStatus.LANCAR.name -> "🟢 Lancar"
                        FluencyStatus.KURANG_LANCAR.name -> "🟡 Kurang Lancar"
                        else -> "🔴 Belum Lancar"
                    }
                    val statusTextColor = when (record.status) {
                        FluencyStatus.LANCAR.name -> FluencyLancar
                        FluencyStatus.KURANG_LANCAR.name -> FluencyKurangLancar
                        else -> FluencyBelumLancar
                    }
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusTextColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = record.date,
                    fontSize = 11.5.sp,
                    color = Color(0xFF7A8B80),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            val levelLabel = if (record.type == ReadingType.IQRO.name) "Iqro Jilid ${record.level}" else "Al-Qur'an Juz ${record.level}"
            val detailLabel = if (record.verse.isNotBlank()) "Halaman ${record.page} (${record.verse})" else "Halaman ${record.page}"

            Text(
                text = "$levelLabel • $detailLabel",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = IslamicEmerald
            )

            if (record.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Catatan: ${record.notes}",
                    fontSize = 12.sp,
                    color = Color(0xFF4A5A50)
                )
            }

            if (record.teacherName.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Penyimak: ${record.teacherName}",
                    fontSize = 10.5.sp,
                    color = Color(0xFF86968B)
                )
            }
        }
    }
}
