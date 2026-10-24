package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Work
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppUser
import com.example.data.repository.UserRepository
import com.example.ui.theme.DesertGold
import com.example.ui.theme.IslamicEmerald
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    userRepository: UserRepository,
    onAuthSuccess: (AppUser) -> Unit
) {
    val scope = rememberCoroutineScope()
    var isRegisterMode by remember { mutableStateOf(false) }

    // Login Form State
    var loginUsername by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Register Form State
    var regName by remember { mutableStateOf("") }
    val regUnitKerja = AppUser.UNIT_KERJA_DEFAULT
    var regJabatan by remember { mutableStateOf(AppUser.JABATAN_OPTIONS.first()) }
    var isJabatanExpanded by remember { mutableStateOf(false) }
    var regUsername by remember { mutableStateOf("") }
    var regPassword by remember { mutableStateOf("") }
    var regConfirmPassword by remember { mutableStateOf("") }
    var isRegPasswordVisible by remember { mutableStateOf(false) }

    // Feedback State
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showRegisterSuccessDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF9FBF8))
            .verticalScroll(scrollState)
            .testTag("auth_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Desert Oasis Visual Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_desert_oasis),
                contentDescription = "Oase Padang Pasir SRT Kediri",
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.35f),
                                Color.Transparent,
                                Color(0xFF0F5132).copy(alpha = 0.95f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .matchParentSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    color = Color.Black.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DesertGold)
                ) {
                    Text(
                        text = "SRT 1 KABUPATEN KEDIRI",
                        color = DesertGold,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Aplikasi Pencatatan Mengaji",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Sekolah Rakyat Terintegrasi 1 Kab. Kediri",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 11.5.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Center Form Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F5E9)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_icon),
                        contentDescription = "Logo Quran SRT",
                        modifier = Modifier.size(44.dp).clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isRegisterMode) "Pendaftaran Pengguna Baru" else "Masuk Akun Pengasuh & Ustadz",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = IslamicEmerald,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isRegisterMode)
                        "Lengkapi formulir pendaftaran akun petugas SRT 1 Kab Kediri."
                    else
                        "Silakan masukkan username dan password Anda untuk masuk.",
                    fontSize = 12.sp,
                    color = Color(0xFF5A6B60),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (errorMessage != null) {
                    Surface(
                        color = Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFC62828),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage!!,
                                color = Color(0xFFC62828),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                if (!isRegisterMode) {
                    // ================= LOGIN FORM =================
                    OutlinedTextField(
                        value = loginUsername,
                        onValueChange = {
                            loginUsername = it
                            errorMessage = null
                        },
                        label = { Text("Username") },
                        placeholder = { Text("Masukkan username") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = IslamicEmerald)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_username_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = loginPassword,
                        onValueChange = {
                            loginPassword = it
                            errorMessage = null
                        },
                        label = { Text("Password") },
                        placeholder = { Text("Masukkan password") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = IslamicEmerald)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isPasswordVisible) "Sembunyikan password" else "Tampilkan password"
                                )
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("login_password_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (loginUsername.isBlank() || loginPassword.isBlank()) {
                                errorMessage = "Username dan password tidak boleh kosong"
                                return@Button
                            }
                            isLoading = true
                            errorMessage = null
                            scope.launch {
                                val result = userRepository.login(loginUsername, loginPassword)
                                isLoading = false
                                result.fold(
                                    onSuccess = { user ->
                                        onAuthSuccess(user)
                                    },
                                    onFailure = { error ->
                                        errorMessage = error.message ?: "Gagal masuk"
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("login_submit_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IslamicEmerald,
                            contentColor = Color.White
                        ),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Masuk",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Switch to Register Button
                    TextButton(
                        onClick = {
                            isRegisterMode = true
                            errorMessage = null
                        },
                        modifier = Modifier.testTag("go_to_register_button")
                    ) {
                        Text(
                            text = "Belum punya akun? Daftar Sekarang",
                            color = IslamicEmerald,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                } else {
                    // ================= REGISTER FORM =================
                    // 1. Nama Lengkap
                    OutlinedTextField(
                        value = regName,
                        onValueChange = {
                            regName = it
                            errorMessage = null
                        },
                        label = { Text("Nama Lengkap") },
                        placeholder = { Text("Contoh: Ustadz Ahmad Fauzi") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = IslamicEmerald)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_name_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 2. Unit Kerja (Hanya 1 pilihan tetap: SRT 1 KAB KEDIRI)
                    OutlinedTextField(
                        value = regUnitKerja,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Unit Kerja (Tetap)") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Business, contentDescription = null, tint = IslamicEmerald)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_unit_kerja_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3. Jabatan Dropdown (Wali Asuh, Wali Asrama, Ustadz / Ustadzah)
                    ExposedDropdownMenuBox(
                        expanded = isJabatanExpanded,
                        onExpandedChange = { isJabatanExpanded = !isJabatanExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = regJabatan,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Pilih Jabatan") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Work, contentDescription = null, tint = IslamicEmerald)
                            },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = isJabatanExpanded)
                            },
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                .fillMaxWidth()
                                .testTag("reg_jabatan_dropdown"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        ExposedDropdownMenu(
                            expanded = isJabatanExpanded,
                            onDismissRequest = { isJabatanExpanded = false }
                        ) {
                            AppUser.JABATAN_OPTIONS.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        regJabatan = option
                                        isJabatanExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4. Username
                    OutlinedTextField(
                        value = regUsername,
                        onValueChange = {
                            regUsername = it.filter { char -> !char.isWhitespace() }
                            errorMessage = null
                        },
                        label = { Text("Username") },
                        placeholder = { Text("huruf kecil / angka tanpa spasi") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = IslamicEmerald)
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_username_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 5. Password
                    OutlinedTextField(
                        value = regPassword,
                        onValueChange = {
                            regPassword = it
                            errorMessage = null
                        },
                        label = { Text("Password") },
                        placeholder = { Text("Minimal 4 karakter") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = IslamicEmerald)
                        },
                        trailingIcon = {
                            IconButton(onClick = { isRegPasswordVisible = !isRegPasswordVisible }) {
                                Icon(
                                    imageVector = if (isRegPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (isRegPasswordVisible) "Sembunyikan password" else "Tampilkan password"
                                )
                            }
                        },
                        visualTransformation = if (isRegPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_password_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 6. Konfirmasi Password
                    OutlinedTextField(
                        value = regConfirmPassword,
                        onValueChange = {
                            regConfirmPassword = it
                            errorMessage = null
                        },
                        label = { Text("Konfirmasi Password") },
                        placeholder = { Text("Ulangi password") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = IslamicEmerald)
                        },
                        visualTransformation = if (isRegPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("reg_confirm_password_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Submit Registration Button
                    Button(
                        onClick = {
                            if (regName.isBlank()) {
                                errorMessage = "Nama lengkap wajib diisi"
                                return@Button
                            }
                            if (regUsername.isBlank()) {
                                errorMessage = "Username wajib diisi"
                                return@Button
                            }
                            if (regPassword.length < 4) {
                                errorMessage = "Password minimal 4 karakter"
                                return@Button
                            }
                            if (regPassword != regConfirmPassword) {
                                errorMessage = "Konfirmasi password tidak cocok"
                                return@Button
                            }

                            isLoading = true
                            errorMessage = null
                            val newUser = AppUser(
                                username = regUsername.trim().lowercase(),
                                name = regName.trim(),
                                unitKerja = regUnitKerja,
                                jabatan = regJabatan,
                                password = regPassword,
                                status = AppUser.STATUS_MENUNGGU,
                                role = AppUser.ROLE_USER
                            )

                            scope.launch {
                                val result = userRepository.register(newUser)
                                isLoading = false
                                result.fold(
                                    onSuccess = {
                                        showRegisterSuccessDialog = true
                                    },
                                    onFailure = { err ->
                                        errorMessage = err.message ?: "Gagal melakukan pendaftaran"
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("reg_submit_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IslamicEmerald,
                            contentColor = Color.White
                        ),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Daftar Akun",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(
                        onClick = {
                            isRegisterMode = false
                            errorMessage = null
                        },
                        modifier = Modifier.testTag("back_to_login_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = IslamicEmerald
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sudah punya akun? Masuk",
                                color = IslamicEmerald,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "SRT 1 KEDIRI © 2026 • Pendidikan Berbasis Kasih & Al-Qur'an",
            fontSize = 11.sp,
            color = Color(0xFF86968B),
            modifier = Modifier.padding(bottom = 20.dp)
        )
    }

    // Success Registration Dialog
    if (showRegisterSuccessDialog) {
        AlertDialog(
            onDismissRequest = {
                showRegisterSuccessDialog = false
                isRegisterMode = false
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Pendaftaran Berhasil!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Akun Anda (@${regUsername.trim().lowercase()}) telah terdaftar dengan status Menunggu Persetujuan.\n\nAkun Anda akan ditinjau oleh Administrator SRT 1 Kediri terlebih dahulu sebelum dapat digunakan untuk login.",
                    fontSize = 13.5.sp,
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRegisterSuccessDialog = false
                        isRegisterMode = false
                        loginUsername = regUsername
                        loginPassword = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicEmerald)
                ) {
                    Text("Mengerti & Masuk")
                }
            }
        )
    }
}
