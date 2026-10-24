package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.data.model.FluencyStatus
import com.example.data.model.NgajiRecord
import com.example.data.model.ReadingType
import com.example.data.model.Student
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfReportGenerator {

    /**
     * Generates an official printable PDF "Kartu Kendali Mengaji" for a foster child / student.
     * Returns the generated File.
     */
    fun generateStudentCardPdf(
        context: Context,
        student: Student,
        records: List<NgajiRecord>
    ): File {
        val document = PdfDocument()
        val pageWidth = 595 // Standard A4 width in points
        val pageHeight = 842 // Standard A4 height in points

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }
        val emeraldColor = Color.rgb(15, 81, 50)
        val goldColor = Color.rgb(212, 175, 55)
        val lightGray = Color.rgb(240, 244, 238)
        val borderGray = Color.rgb(210, 218, 212)
        val textDark = Color.rgb(30, 40, 35)
        val textMuted = Color.rgb(100, 115, 105)

        // 1. Top Decorative Bar
        paint.color = emeraldColor
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), 12f, paint)
        paint.color = goldColor
        canvas.drawRect(0f, 12f, pageWidth.toFloat(), 15f, paint)

        var yPos = 45f

        // 2. Official Header (Kop Surat)
        paint.color = emeraldColor
        paint.textSize = 15f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("SEKOLAH RAKYAT TERINTEGRASI (SRT) 1 KABUPATEN KEDIRI", pageWidth / 2f, yPos, paint)

        yPos += 16f
        paint.color = textMuted
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Pencatatan dan Evaluasi Progres Pembinaan Mengaji Anak Asuh", pageWidth / 2f, yPos, paint)

        yPos += 13f
        canvas.drawText("Sekretariat: Kabupaten Kediri, Jawa Timur | Telp: (0354) SRT-KEDIRI", pageWidth / 2f, yPos, paint)

        yPos += 10f
        paint.color = emeraldColor
        paint.strokeWidth = 2f
        canvas.drawLine(36f, yPos, pageWidth - 36f, yPos, paint)
        paint.color = goldColor
        paint.strokeWidth = 0.8f
        canvas.drawLine(36f, yPos + 3f, pageWidth - 36f, yPos + 3f, paint)

        yPos += 24f
        // Document Title
        paint.color = emeraldColor
        paint.textSize = 12.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("KARTU KENDALI BACAAN MENGAJI (KARTU NGAJI)", pageWidth / 2f, yPos, paint)

        yPos += 18f
        // 3. Student Profile Card Box
        paint.color = lightGray
        val profileRect = RectF(36f, yPos, pageWidth - 36f, yPos + 72f)
        canvas.drawRoundRect(profileRect, 6f, 6f, paint)

        paint.color = borderGray
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(profileRect, 6f, 6f, paint)
        paint.style = Paint.Style.FILL

        paint.textAlign = Paint.Align.LEFT
        paint.textSize = 9.5f

        // Col 1
        paint.color = textMuted
        canvas.drawText("Nama Santri / Anak Asuh", 48f, yPos + 20f, paint)
        paint.color = textDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText(": ${student.name}", 165f, yPos + 20f, paint)

        paint.color = textMuted
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Jenis Kelamin", 48f, yPos + 38f, paint)
        paint.color = textDark
        canvas.drawText(": ${student.gender}", 165f, yPos + 38f, paint)

        paint.color = textMuted
        canvas.drawText("Kamar / Asrama", 48f, yPos + 56f, paint)
        paint.color = textDark
        canvas.drawText(": ${if (student.roomOrClass.isNotBlank()) student.roomOrClass else "-"}", 165f, yPos + 56f, paint)

        // Col 2
        paint.color = textMuted
        canvas.drawText("Tingkat Bacaan", 320f, yPos + 20f, paint)
        paint.color = emeraldColor
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        val levelLabel = if (student.currentType == ReadingType.IQRO.name) "Iqro Jilid ${student.currentLevel}" else "Al-Qur'an Juz ${student.currentLevel}"
        canvas.drawText(": $levelLabel", 410f, yPos + 20f, paint)

        paint.color = textMuted
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Posisi Halaman", 320f, yPos + 38f, paint)
        paint.color = textDark
        canvas.drawText(": Halaman ${student.currentPage}", 410f, yPos + 38f, paint)

        paint.color = textMuted
        canvas.drawText("Total Riwayat Simak", 320f, yPos + 56f, paint)
        paint.color = textDark
        canvas.drawText(": ${records.size} Kali Setoran", 410f, yPos + 56f, paint)

        yPos += 86f

        // 4. History Table
        // Table Header
        paint.color = emeraldColor
        val tableHeadRect = RectF(36f, yPos, pageWidth - 36f, yPos + 22f)
        canvas.drawRoundRect(tableHeadRect, 4f, 4f, paint)

        paint.color = Color.WHITE
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        val colNo = 44f
        val colDate = 70f
        val colLevel = 135f
        val colPage = 210f
        val colStatus = 280f
        val colNotes = 360f
        val colTeacher = 490f

        canvas.drawText("No", colNo, yPos + 14f, paint)
        canvas.drawText("Tanggal", colDate, yPos + 14f, paint)
        canvas.drawText("Tingkat", colLevel, yPos + 14f, paint)
        canvas.drawText("Hal/Ayat", colPage, yPos + 14f, paint)
        canvas.drawText("Kelancaran", colStatus, yPos + 14f, paint)
        canvas.drawText("Catatan Pembimbing", colNotes, yPos + 14f, paint)
        canvas.drawText("Penyimak", colTeacher, yPos + 14f, paint)

        yPos += 24f

        val sortedRecords = records.sortedByDescending { it.date }
        val maxRecordsOnPage = 18
        val displayRecords = sortedRecords.take(maxRecordsOnPage)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        displayRecords.forEachIndexed { index, record ->
            val rowHeight = 20f
            if (index % 2 == 1) {
                paint.color = Color.rgb(247, 250, 246)
                canvas.drawRect(36f, yPos, pageWidth - 36f, yPos + rowHeight, paint)
            }

            paint.color = borderGray
            paint.strokeWidth = 0.5f
            canvas.drawLine(36f, yPos + rowHeight, pageWidth - 36f, yPos + rowHeight, paint)

            paint.color = textDark
            paint.textSize = 8f

            canvas.drawText("${index + 1}", colNo, yPos + 13f, paint)
            canvas.drawText(record.date, colDate, yPos + 13f, paint)

            val recLevel = if (record.type == ReadingType.IQRO.name) "Iqro ${record.level}" else "Juz ${record.level}"
            canvas.drawText(recLevel, colLevel, yPos + 13f, paint)

            val pageInfoStr = if (record.verse.isNotBlank()) "H.${record.page} (${record.verse})" else "Halaman ${record.page}"
            canvas.drawText(pageInfoStr, colPage, yPos + 13f, paint)

            // Status Badge text with color
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            when (record.status) {
                FluencyStatus.LANCAR.name -> {
                    paint.color = Color.rgb(27, 94, 32)
                    canvas.drawText("Lancar", colStatus, yPos + 13f, paint)
                }
                FluencyStatus.KURANG_LANCAR.name -> {
                    paint.color = Color.rgb(230, 81, 0)
                    canvas.drawText("Kurang Lancar", colStatus, yPos + 13f, paint)
                }
                else -> {
                    paint.color = Color.rgb(198, 40, 40)
                    canvas.drawText("Belum Lancar", colStatus, yPos + 13f, paint)
                }
            }

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = textDark

            val notesTrimmed = if (record.notes.length > 25) record.notes.take(23) + "..." else record.notes
            canvas.drawText(if (notesTrimmed.isNotBlank()) notesTrimmed else "-", colNotes, yPos + 13f, paint)

            val teacherTrimmed = if (record.teacherName.length > 15) record.teacherName.take(13) + ".." else record.teacherName
            canvas.drawText(if (teacherTrimmed.isNotBlank()) teacherTrimmed else "Ustadz", colTeacher, yPos + 13f, paint)

            yPos += rowHeight
        }

        if (displayRecords.isEmpty()) {
            paint.color = textMuted
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("Belum ada catatan setoran bacaan.", pageWidth / 2f, yPos + 30f, paint)
            yPos += 50f
            paint.textAlign = Paint.Align.LEFT
        }

        // 5. Summary & Evaluation Stats Box
        yPos = maxOf(yPos + 16f, 650f)
        val lancarCount = records.count { it.status == FluencyStatus.LANCAR.name }
        val kurangCount = records.count { it.status == FluencyStatus.KURANG_LANCAR.name }
        val belumCount = records.count { it.status == FluencyStatus.BELUM_LANCAR.name }
        val total = records.size

        val lancarPercent = if (total > 0) (lancarCount * 100) / total else 0

        paint.color = Color.rgb(244, 248, 243)
        val statsRect = RectF(36f, yPos, pageWidth - 36f, yPos + 40f)
        canvas.drawRoundRect(statsRect, 4f, 4f, paint)

        paint.color = borderGray
        paint.style = Paint.Style.STROKE
        canvas.drawRoundRect(statsRect, 4f, 4f, paint)
        paint.style = Paint.Style.FILL

        paint.textSize = 8.5f
        paint.color = textDark
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Ringkasan Evaluasi Progres Bacaan:", 48f, yPos + 16f, paint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Total Sesi: $total kali", 48f, yPos + 30f, paint)
        canvas.drawText("Lancar: $lancarCount ($lancarPercent%)", 170f, yPos + 30f, paint)
        canvas.drawText("Kurang Lancar: $kurangCount", 290f, yPos + 30f, paint)
        canvas.drawText("Belum Lancar: $belumCount", 410f, yPos + 30f, paint)

        yPos += 55f

        // 6. Signature Section
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        val todayStr = dateFormat.format(Date())

        paint.textAlign = Paint.Align.CENTER
        paint.textSize = 8.5f
        paint.color = textDark

        // Right side: Tanggal & Ustadz
        val rightColX = pageWidth - 120f
        canvas.drawText("Kediri, $todayStr", rightColX, yPos, paint)
        canvas.drawText("Ustadz Pembimbing / Penyimak,", rightColX, yPos + 14f, paint)

        canvas.drawText("_________________________", rightColX, yPos + 55f, paint)
        canvas.drawText("( Ustadz Pembina Asrama )", rightColX, yPos + 68f, paint)

        // Left side: Mengetahui
        val leftColX = 120f
        canvas.drawText("Mengetahui,", leftColX, yPos, paint)
        canvas.drawText("Pengurus SRT 1 Kabupaten Kediri,", leftColX, yPos + 14f, paint)

        canvas.drawText("_________________________", leftColX, yPos + 55f, paint)
        canvas.drawText("( Kepala Sekolah Rakyat SRT )", leftColX, yPos + 68f, paint)

        // 7. Footer
        paint.color = textMuted
        paint.textSize = 7f
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(
            "Dokumen Resmi Kartu Kendali Mengaji Sekolah Rakyat Terintegrasi 1 Kab. Kediri - Dicetak pada $todayStr",
            pageWidth / 2f,
            pageHeight - 18f,
            paint
        )

        document.finishPage(page)

        // Write to cache file
        val cleanName = student.name.replace("\\s+".toRegex(), "_").replace("[^a-zA-Z0-9_]".toRegex(), "")
        val outputFile = File(context.cacheDir, "Kartu_Ngaji_${cleanName}.pdf")
        FileOutputStream(outputFile).use { fos ->
            document.writeTo(fos)
        }
        document.close()

        return outputFile
    }

    /**
     * Share or view the generated PDF via Android system Intent
     */
    fun sharePdf(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Kartu Ngaji SRT 1 Kediri - ${pdfFile.nameWithoutExtension}")
            putExtra(Intent.EXTRA_TEXT, "Berikut adalah Kartu Kendali Mengaji resmi anak asuh Sekolah Rakyat Terintegrasi 1 Kabupaten Kediri.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Bagikan / Cetak Kartu Ngaji").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    /**
     * Open PDF for viewing or immediate wireless printing
     */
    fun viewPdf(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to share intent if no PDF viewer is found
            sharePdf(context, pdfFile)
        }
    }
}
