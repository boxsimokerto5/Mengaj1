package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.AppUser
import com.example.data.repository.NgajiRepository
import com.example.data.repository.UserRepository
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.MainAppScaffold
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.NgajiViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        setContent {
            MyApplicationTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    val context = LocalContext.current
    val userRepository = remember { UserRepository(context) }
    var currentUser by remember { mutableStateOf<AppUser?>(userRepository.getSession()) }
    var showSplash by remember { mutableStateOf(true) }
    var adminViewingApp by remember { mutableStateOf(false) }

    if (showSplash) {
        SplashScreen(
            onTimeout = {
                showSplash = false
            }
        )
    } else {
        val user = currentUser
        if (user == null) {
            AuthScreen(
                userRepository = userRepository,
                onAuthSuccess = { loggedInUser ->
                    currentUser = loggedInUser
                    adminViewingApp = false
                }
            )
        } else if (user.role == AppUser.ROLE_ADMIN && !adminViewingApp) {
            AdminScreen(
                userRepository = userRepository,
                onEnterApp = {
                    adminViewingApp = true
                },
                onLogout = {
                    userRepository.logout()
                    currentUser = null
                    adminViewingApp = false
                }
            )
        } else {
            // Main Mengaji Management App
            val sharedSchoolId = "srt1_kediri_school"
            val viewModel: NgajiViewModel = viewModel(
                key = sharedSchoolId,
                factory = viewModelFactory {
                    initializer {
                        val app = checkNotNull(this[APPLICATION_KEY])
                        val databaseId = app.getString(R.string.firestore_database_id)
                        val db = FirebaseFirestore.getInstance(databaseId)
                        NgajiViewModel(NgajiRepository(db), sharedSchoolId)
                    }
                }
            )

            MainAppScaffold(
                viewModel = viewModel,
                currentUser = user,
                onBackToAdmin = if (user.role == AppUser.ROLE_ADMIN) {
                    { adminViewingApp = false }
                } else null,
                onSignOutRequest = {
                    userRepository.logout()
                    currentUser = null
                    adminViewingApp = false
                }
            )
        }
    }
}
