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

import kotlinx.coroutines.withContext
import java.io.IOException



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
    var profile by remember { mutableStateOf<Profile?>(null) }

    val context = LocalContext.current
    val imageUri = remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri.value = uri
    }


    LaunchedEffect(Unit) {

        coroutineScope.launch {
            try {
                val userId = supabase.auth.currentUserOrNull()?.id
                if (userId != null) {
                    val result = supabase.from("profiles")
                        .select {
                            filter{
                                eq(
                                    "id", userId
                                )
                            }
                        }



                        .decodeSingle<Profile>()

                    profile = result
                    firstName = result.first_name
                    lastName = result.last_name
                    birthday = result.birthday
                    gender = result.gender
                    email = result.email


                    profile = result
                }
            } catch (e: Exception) {
                Log.e("Supabase", "Error loading profile: ${e.message}")
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar with icons like in the screenshots
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* Handle back navigation */ }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Transparent,
                    modifier = Modifier.size(24.dp)
                )
            }


            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                IconButton(onClick = {
                    coroutineScope.launch {
                        try {
                            val userId = supabase.auth.currentUserOrNull()?.id ?: return@launch

                            supabase.from("profiles").update(
                                mapOf(
                                    "first_name" to firstName,
                                    "last_name" to lastName,
                                    "birthday" to birthday,
                                    "gender" to gender,
                                    "email" to email
                                )
                            ){
                                filter {
                                    eq("id", userId)
                                }
                            }

                            Toast.makeText(
                                context,
                                "Profile updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        } catch (e: Exception) {
                            Log.e("Supabase", "Update failed: ${e.message}")
                            Toast.makeText(
                                context,
                                "Update failed: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Save",
                        tint = onSurfaceColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Section
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            // Avatar with border like in screenshots
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(3.dp, primaryColor, CircleShape)
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0xFFFFDBE0))
                ) {
                    // Avatar content would go here
                    imageUri.value?.let { uri ->
                        Image(
                            painter = rememberAsyncImagePainter(model = uri),
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                        )
                    }

                }
            }

            Spacer(modifier = Modifier.height(12.dp))


            Text(
                text = "${profile?.first_name ?: ""} ${profile?.last_name ?: ""}".trim(),
                color = onSurfaceColor,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons similar to screenshots
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { launcher.launch("image/*") },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = primaryColor
                    ),
                    border = BorderStroke(1.dp, primaryColor),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "CHANGE AVATAR",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Form Fields in card-like containers
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile fields in clean cards
            ProfileCard(
                isDarkTheme = isDarkTheme,
                surfaceColor = surfaceColor,
                borderColor = borderColor
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    ProfileTextField(
                        label = "First Name",
                        value =  firstName,
                        onValueChange = {firstName = it },
                        isDarkTheme = isDarkTheme,
                        onSurfaceColor = onSurfaceColor,
                        onSurfaceVariant = onSurfaceVariant
                    )

                    ProfileTextField(
                        label = "Last Name",
                        value = lastName,
                        onValueChange = { lastName = it  },
                        isDarkTheme = isDarkTheme,
                        onSurfaceColor = onSurfaceColor,
                        onSurfaceVariant = onSurfaceVariant
                    )

                    val datePickerState = rememberDatePickerState()
                    var showDatePicker by remember { mutableStateOf(false) }

                    if (showDatePicker) {
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                TextButton(onClick = {
                                    showDatePicker = false
                                    val selectedDate = datePickerState.selectedDateMillis?.let {
                                        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date(it))
                                    }
                                    if (selectedDate != null) {
                                        birthday = selectedDate
                                    }
                                }) {
                                    Text("OK")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDatePicker = false }) {
                                    Text("Cancel")
                                }
                            }
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }

                    ProfileTextField(
                        label = "Birthday",
                        value = birthday,
                        onValueChange = { },
                        isDarkTheme = isDarkTheme,
                        onSurfaceColor = onSurfaceColor,
                        onSurfaceVariant = onSurfaceVariant,
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Select date")
                            }
                        }
                    )

                    var expanded by remember { mutableStateOf(false) }
                    val genderOptions = listOf("Male", "Female")

                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                        ProfileTextField(
                            label = "Gender",
                            value = gender,
                            onValueChange = {},
                            isDarkTheme = isDarkTheme,
                            onSurfaceColor = onSurfaceColor,
                            onSurfaceVariant = onSurfaceVariant,
                            readOnly = true,
                            modifier = Modifier.menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            genderOptions.forEach { selectionOption ->
                                DropdownMenuItem(
                                    text = { Text(selectionOption) },
                                    onClick = {
                                        gender = selectionOption
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }

                    ProfileTextField(
                        label = "Email",
                        value = email,
                        onValueChange = {},
                        isDarkTheme = isDarkTheme,
                        onSurfaceColor = onSurfaceColor,
                        onSurfaceVariant = onSurfaceVariant,
                        readOnly = true
                    )



                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Sign Out Button with proper styling
        Button(
            onClick = {
                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        supabase.auth.signOut()
                        onSignOut()
                    } catch (e: Exception) {
                        // Handle any errors during sign out
                        onSignOut() // Still navigate to auth screen even if sign out fails
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent
            ),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, primaryColor)
        ) {
            Text(
                text = "Sign Out",
                color = primaryColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
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

    suspend fun uriToByteArray(context: Context, uri: Uri): ByteArray {
        return withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            } ?: throw IOException("Unable to read image")
        }
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