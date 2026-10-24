package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppUser
import com.example.data.repository.UserRepository
import com.example.ui.theme.DesertGold
import com.example.ui.theme.IslamicEmerald
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    userRepository: UserRepository,
    onEnterApp: () -> Unit,
    onLogout: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val allUsers by userRepository.observeAllUsers().collectAsState(initial = emptyList())
    var selectedTab by remember { mutableIntStateOf(1) } // Default to "Menunggu Persetujuan"
    var searchQuery by remember { mutableStateOf("") }

    val pendingCount = allUsers.count { it.status == AppUser.STATUS_MENUNGGU }
    val approvedCount = allUsers.count { it.status == AppUser.STATUS_DISETUJUI }
    val rejectedCount = allUsers.count { it.status == AppUser.STATUS_DITOLAK }

    val filteredUsers = allUsers.filter { user ->
        val matchesTab = when (selectedTab) {
            0 -> true // Semua
            1 -> user.status == AppUser.STATUS_MENUNGGU
            2 -> user.status == AppUser.STATUS_DISETUJUI
            3 -> user.status == AppUser.STATUS_DITOLAK
            else -> true
        }
        val matchesSearch = if (searchQuery.isBlank()) true else {
            user.name.contains(searchQuery, ignoreCase = true) ||
                    user.username.contains(searchQuery, ignoreCase = true) ||
                    user.jabatan.contains(searchQuery, ignoreCase = true)
        }
        matchesTab && matchesSearch
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = DesertGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Panel Administrator",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Text(
                            text = "SRT 1 KABUPATEN KEDIRI",
                            fontSize = 11.sp,
                            color = DesertGold,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                actions = {
                    // Button to enter Ngaji app as admin
                    IconButton(
                        onClick = onEnterApp,
                        modifier = Modifier.testTag("admin_open_app_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = "Buka Aplikasi Mengaji",
                            tint = Color.White
                        )
                    }
                    IconButton(
                        onClick = onLogout,
                        modifier = Modifier.testTag("admin_logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Keluar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = IslamicEmerald
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF7FAF6))
        ) {
            // Summary Cards Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatusSummaryBox(
                    title = "Menunggu",
                    count = pendingCount,
                    color = Color(0xFFE65100),
                    bgColor = Color(0xFFFFF3E0),
                    modifier = Modifier.weight(1f)
                )
                StatusSummaryBox(
                    title = "Disetujui",
                    count = approvedCount,
                    color = Color(0xFF2E7D32),
                    bgColor = Color(0xFFE8F5E9),
                    modifier = Modifier.weight(1f)
                )
                StatusSummaryBox(
                    title = "Ditolak",
                    count = rejectedCount,
                    color = Color(0xFFC62828),
                    bgColor = Color(0xFFFFEBEE),
                    modifier = Modifier.weight(1f)
                )
            }

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari nama / username / jabatan...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color.Gray)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tabs for filtering
            val tabs = listOf("Semua", "Menunggu ($pendingCount)", "Disetujui ($approvedCount)", "Ditolak ($rejectedCount)")
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 16.dp,
                containerColor = Color.White,
                contentColor = IslamicEmerald
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) IslamicEmerald else Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            // User List
            if (filteredUsers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.HourglassTop,
                            contentDescription = null,
                            tint = Color.LightGray,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Tidak ada user yang sesuai pencarian" else "Belum ada user pada kategori ini",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredUsers, key = { it.username }) { user ->
                        UserApprovalCard(
                            user = user,
                            onApprove = {
                                scope.launch {
                                    userRepository.updateUserStatus(user.username, AppUser.STATUS_DISETUJUI)
                                }
                            },
                            onReject = {
                                scope.launch {
                                    userRepository.updateUserStatus(user.username, AppUser.STATUS_DITOLAK)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusSummaryBox(
    title: String,
    count: Int,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = bgColor,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = color.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun UserApprovalCard(
    user: AppUser,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")) }
    val formattedDate = remember(user.createdAt) {
        try {
            dateFormat.format(Date(user.createdAt))
        } catch (e: Exception) {
            "-"
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(IslamicEmerald.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = IslamicEmerald,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.name.ifBlank { "Tanpa Nama" },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E2923)
                    )
                    Text(
                        text = "@${user.username}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Status Badge
                StatusPill(status = user.status)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF0F3F1))
            Spacer(modifier = Modifier.height(10.dp))

            // Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Jabatan", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = user.jabatan.ifBlank { "-" },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2C3E35)
                    )
                }
                Column {
                    Text("Unit Kerja", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = user.unitKerja,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = IslamicEmerald
                    )
                }
                Column {
                    Text("Mendaftar", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        text = formattedDate,
                        fontSize = 11.5.sp,
                        color = Color(0xFF555555)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (user.status != AppUser.STATUS_DISETUJUI) {
                    Button(
                        onClick = onApprove,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Setujui", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                if (user.status != AppUser.STATUS_DITOLAK) {
                    OutlinedButton(
                        onClick = onReject,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFC62828)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Tolak", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(status: String) {
    val (label, bgColor, textColor) = when (status) {
        AppUser.STATUS_DISETUJUI -> Triple("Disetujui", Color(0xFFE8F5E9), Color(0xFF2E7D32))
        AppUser.STATUS_DITOLAK -> Triple("Ditolak", Color(0xFFFFEBEE), Color(0xFFC62828))
        else -> Triple("Menunggu", Color(0xFFFFF3E0), Color(0xFFE65100))
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
