package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.DesertOasisHeader
import com.example.ui.theme.DesertGold
import com.example.ui.theme.IslamicEmerald
import com.example.ui.viewmodel.MainTab
import com.example.ui.viewmodel.NgajiViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun MainAppScaffold(
    viewModel: NgajiViewModel,
    onSignOutRequest: () -> Unit
) {
    val activeTab by viewModel.activeTab.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSignOutDialog by remember { mutableStateOf(false) }

    val user = Firebase.auth.currentUser
    val userEmail = user?.email ?: user?.displayName

    // Show feedback messages
    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    // Back handling: If on sub-tabs, return to SANTRI
    BackHandler(enabled = activeTab != MainTab.SANTRI) {
        viewModel.setActiveTab(MainTab.SANTRI)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_app_scaffold"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = activeTab == MainTab.SANTRI,
                    onClick = { viewModel.setActiveTab(MainTab.SANTRI) },
                    icon = { Icon(imageVector = Icons.Default.Groups, contentDescription = "Daftar Santri") },
                    label = { Text("Santri", fontSize = 11.sp, fontWeight = if (activeTab == MainTab.SANTRI) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IslamicEmerald,
                        selectedTextColor = IslamicEmerald,
                        indicatorColor = Color(0xFFE8F5E9)
                    ),
                    modifier = Modifier.testTag("nav_tab_santri")
                )

                NavigationBarItem(
                    selected = activeTab == MainTab.CATAT_NGAJI,
                    onClick = { viewModel.setActiveTab(MainTab.CATAT_NGAJI) },
                    icon = { Icon(imageVector = Icons.Default.AutoStories, contentDescription = "Catat Ngaji") },
                    label = { Text("Catat Ngaji", fontSize = 11.sp, fontWeight = if (activeTab == MainTab.CATAT_NGAJI) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IslamicEmerald,
                        selectedTextColor = IslamicEmerald,
                        indicatorColor = Color(0xFFE8F5E9)
                    ),
                    modifier = Modifier.testTag("nav_tab_catat_ngaji")
                )

                NavigationBarItem(
                    selected = activeTab == MainTab.EVALUASI,
                    onClick = { viewModel.setActiveTab(MainTab.EVALUASI) },
                    icon = { Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "Evaluasi & Kartu") },
                    label = { Text("Evaluasi & PDF", fontSize = 11.sp, fontWeight = if (activeTab == MainTab.EVALUASI) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IslamicEmerald,
                        selectedTextColor = IslamicEmerald,
                        indicatorColor = Color(0xFFE8F5E9)
                    ),
                    modifier = Modifier.testTag("nav_tab_evaluasi")
                )

                NavigationBarItem(
                    selected = activeTab == MainTab.LAPORAN,
                    onClick = { viewModel.setActiveTab(MainTab.LAPORAN) },
                    icon = { Icon(imageVector = Icons.Default.Assessment, contentDescription = "Laporan") },
                    label = { Text("Laporan", fontSize = 11.sp, fontWeight = if (activeTab == MainTab.LAPORAN) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = IslamicEmerald,
                        selectedTextColor = IslamicEmerald,
                        indicatorColor = Color(0xFFE8F5E9)
                    ),
                    modifier = Modifier.testTag("nav_tab_laporan")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF9FBF8))
        ) {
            // Desert Oasis Header (Merchant with camels, oasis, date palms under blue sky)
            DesertOasisHeader(
                userEmail = userEmail,
                onSignOutClick = { showSignOutDialog = true }
            )

            // Content body according to active tab
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when (activeTab) {
                    MainTab.SANTRI -> StudentsScreen(viewModel = viewModel)
                    MainTab.CATAT_NGAJI -> RecordInputScreen(viewModel = viewModel)
                    MainTab.EVALUASI -> StudentDetailAndCardScreen(viewModel = viewModel)
                    MainTab.LAPORAN -> ReportsScreen(viewModel = viewModel)
                }
            }
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text("Keluar Akun", fontWeight = FontWeight.Bold, color = IslamicEmerald) },
            text = { Text("Apakah Anda yakin ingin keluar dari akun pengasuh/ustadz SRT 1 Kediri?") },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutDialog = false
                        onSignOutRequest()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald)
                ) {
                    Text("Keluar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
