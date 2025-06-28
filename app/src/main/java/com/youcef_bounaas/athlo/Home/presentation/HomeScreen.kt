package com.youcef_bounaas.athlo.Home.presentation

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.youcef_bounaas.athlo.UserInfo.data.Profile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage

import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSignOut: () -> Unit,
    supabase: SupabaseClient
) {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) Color(0xFF1A1A1A) else Color(0xFFF8F9FA)
    val surfaceColor = if (isDarkTheme) Color(0xFF2D2D2D) else Color.White
    val onSurfaceColor = if (isDarkTheme) Color.White else Color.Black
    val onSurfaceVariant = if (isDarkTheme) Color(0xFFBBBBBB) else Color(0xFF666666)
    val primaryColor = MaterialTheme.colorScheme.primary
    val borderColor = if (isDarkTheme) Color(0xFF404040) else Color(0xFFE8E8E8)

    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val imageUri = remember { mutableStateOf<Uri?>(null) }
    var avatarUrl by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) imageUri.value = uri
    }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                val userId = supabase.auth.currentUserOrNull()?.id
                if (userId != null) {
                    val result = supabase.from("profiles")
                        .select {
                            filter { eq("id", userId) }
                        }
                        .decodeSingle<Profile>()

                    firstName = result.first_name
                    lastName = result.last_name
                    birthday = result.birthday
                    gender = result.gender
                    email = result.email
                    avatarUrl = result.avatar_url
                }
            } catch (e: Exception) {
                Log.e("Supabase", "Error loading profile: ${e.message}")
            }
        }
    }

    fun uriToByteArray(context: Context, uri: Uri): ByteArray {
        return context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: throw IllegalArgumentException("Unable to read URI")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Transparent,
                    modifier = Modifier.size(24.dp)
                )
            }
            IconButton(onClick = {
                coroutineScope.launch {
                    try {
                        val userId = supabase.auth.currentUserOrNull()?.id ?: return@launch
                        imageUri.value?.let { uri ->
                            val bytes = uriToByteArray(context, uri)
                            val path = "$userId.jpg"
                            supabase.storage.from("avatars").upload(path, bytes) {
                                upsert = true
                            }
                            avatarUrl = supabase.storage.from("avatars").publicUrl(path)
                        }

                        supabase.from("profiles").update(
                            mapOf(
                                "first_name" to firstName,
                                "last_name" to lastName,
                                "birthday" to birthday,
                                "gender" to gender,
                                "email" to email,
                                "avatar_url" to avatarUrl
                            )
                        ) {
                            filter { eq("id", userId) }
                        }

                        Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.e("Supabase", "Error: ${e.message}")
                        Toast.makeText(context, "Update failed", Toast.LENGTH_LONG).show()
                    }
                }
            }) {
                Icon(Icons.Default.Save, contentDescription = null, tint = onSurfaceColor)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(3.dp, primaryColor, CircleShape)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                val painter = rememberAsyncImagePainter(imageUri.value ?: avatarUrl)
                if (imageUri.value != null || avatarUrl != null) {
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color(0xFFFFDBE0)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("$firstName $lastName", color = onSurfaceColor, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            ProfileCard(isDarkTheme, surfaceColor, borderColor) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    ProfileTextField("First Name", firstName, { firstName = it }, false, null, isDarkTheme, onSurfaceColor, onSurfaceVariant)
                    ProfileTextField("Last Name", lastName, { lastName = it }, false, null, isDarkTheme, onSurfaceColor, onSurfaceVariant)

                    val datePickerState = rememberDatePickerState()
                    var showPicker by remember { mutableStateOf(false) }
                    if (showPicker) {
                        DatePickerDialog(
                            onDismissRequest = { showPicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    showPicker = false
                                    datePickerState.selectedDateMillis?.let {
                                        birthday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                                            Date(it)
                                        )
                                    }
                                }) { Text("OK") }
                            },
                            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("Cancel") } }
                        ) { DatePicker(state = datePickerState) }
                    }
                    ProfileTextField("Birthday", birthday, {}, true, {
                        IconButton(onClick = { showPicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                        }
                    }, isDarkTheme, onSurfaceColor, onSurfaceVariant)

                    var expanded by remember { mutableStateOf(false) }
                    val genderOptions = listOf("Male", "Female")
                    ExposedDropdownMenuBox(expanded, onExpandedChange = { expanded = !expanded }) {
                        ProfileTextField("Gender", gender, {}, true, null, isDarkTheme, onSurfaceColor, onSurfaceVariant, Modifier.menuAnchor())
                        ExposedDropdownMenu(expanded, onDismissRequest = { expanded = false }) {
                            genderOptions.forEach {
                                DropdownMenuItem({ Text(it) }, onClick = {
                                    gender = it
                                    expanded = false
                                })
                            }
                        }
                    }

                    ProfileTextField("Email", email, {}, true, null, isDarkTheme, onSurfaceColor, onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        supabase.auth.signOut()
                        onSignOut()
                    } catch (_: Exception) {
                        onSignOut()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            border = BorderStroke(1.dp, primaryColor)
        ) {
            Text("Sign Out", color = primaryColor)
        }
    }
}


@Composable
fun ProfileCard(
    isDarkTheme: Boolean,
    surfaceColor: Color,
    borderColor: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        ),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDarkTheme) 0.dp else 2.dp
        )
    ) {
        Box(
            modifier = Modifier.padding(20.dp)
        ) {
            content()
        }
    }
}

@Composable
fun ProfileTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    readOnly: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    isDarkTheme: Boolean,
    onSurfaceColor: Color,
    onSurfaceVariant: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            color = onSurfaceVariant,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            readOnly = readOnly,
            textStyle = LocalTextStyle.current.copy(
                color = onSurfaceColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            ),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Transparent, RoundedCornerShape(8.dp))
                        .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        innerTextField()
                    }
                    if (trailingIcon != null) {
                        trailingIcon()
                    }
                }
            }
        )


    }



}


@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MaterialTheme {
        HomeScreen(
            onSignOut = {},
            supabase = koinInject()
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenDarkPreview() {
    MaterialTheme {
        HomeScreen(
            onSignOut = {},
            supabase = koinInject()
        )
    }
}