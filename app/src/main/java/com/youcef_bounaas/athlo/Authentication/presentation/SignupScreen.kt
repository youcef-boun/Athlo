package com.youcef_bounaas.athlo.Authentication.presentation

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.youcef_bounaas.athlo.Navigation.presentation.NavDestination
import com.youcef_bounaas.athlo.R
import com.youcef_bounaas.athlo.ui.theme.AthloTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun SignupScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    val isButtonEnabled = email.isNotBlank() && password.isNotBlank() &&  password == confirmPassword

    val viewModel: AuthViewModel = koinViewModel()
    val signupResult by viewModel.signupResult.collectAsState()
    val context = LocalContext.current



    val focusManager = LocalFocusManager.current

    val buttonColor = if (isButtonEnabled) colorScheme.primary else colorScheme.onSurface.copy(alpha = 0.3f)
    val textColor = if (isButtonEnabled) colorScheme.onPrimary else colorScheme.onSurface.copy(alpha = 0.5f)





    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Back button
        IconButton(
            onClick = { /* Handle back navigation */ },
            modifier = Modifier.align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colorScheme.onBackground
            )
        }

        // Main content
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // Header
            Text(
                text = "Create an account",
                color = colorScheme.onBackground,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            ) {

            // Email field
           item {
               OutlinedTextField(
                   value = email,
                   onValueChange = { email = it },
                   placeholder = { Text("Email", color = Color.Gray) },
                   singleLine = true,
                   keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                   keyboardActions = KeyboardActions(
                       onNext = { focusManager.moveFocus(FocusDirection.Down) } // Move to Password field
                   ),
                   modifier = Modifier.fillMaxWidth(),
                   shape = RoundedCornerShape(16.dp),
                   colors = OutlinedTextFieldDefaults.colors(
                       unfocusedBorderColor = colorScheme.outline,
                       focusedBorderColor = colorScheme.primary,
                       unfocusedContainerColor = colorScheme.surface,
                       focusedContainerColor = colorScheme.surface,
                       cursorColor = colorScheme.primary,
                       unfocusedTextColor = colorScheme.onSurface,
                       focusedTextColor = colorScheme.onSurface
                   )
               )
           }
item {
    Spacer(modifier = Modifier.height(16.dp))
}

item {
    // Password field
    OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        visualTransformation = PasswordVisualTransformation(),
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(
            onNext = { focusManager.moveFocus(FocusDirection.Down) } // Move to Password field
        ),
        placeholder = { Text("Password", color = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = colorScheme.outline,
            focusedBorderColor = colorScheme.primary,
            unfocusedContainerColor = colorScheme.surface,
            focusedContainerColor = colorScheme.surface,
            cursorColor = colorScheme.primary,
            unfocusedTextColor = colorScheme.onSurface,
            focusedTextColor = colorScheme.onSurface
        )
    )
}
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

item {

    // Confirm Password field
    OutlinedTextField(
        value = confirmPassword,
        onValueChange = { confirmPassword = it },
        visualTransformation = PasswordVisualTransformation(),
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(
            onDone = { focusManager.clearFocus() } // Move to Password field
        ),
        placeholder = { Text("Confirm Password", color = Color.Gray) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = colorScheme.outline,
            focusedBorderColor = colorScheme.primary,
            unfocusedContainerColor = colorScheme.surface,
            focusedContainerColor = colorScheme.surface,
            cursorColor = colorScheme.primary,
            unfocusedTextColor = colorScheme.onSurface,
            focusedTextColor = colorScheme.onSurface
        )
    )


}

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                item {

                    // Continue button
                    Button(
                        onClick = {
                           // navController.navigate(NavDestination.UserInfo.route)
                            viewModel.signUp(email.trim(), password.trim())
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = isButtonEnabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor,
                            contentColor = textColor
                        )
                    ) {
                        Text(
                            text = "Sign up",
                            modifier = Modifier.padding(vertical = 8.dp),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

item {
    Spacer(modifier = Modifier.height(24.dp))
}


                item {
                    // Divider with "or" text
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = colorScheme.onBackground,
                            thickness = 1.dp
                        )
                        Text(
                            text = "or",
                            color = colorScheme.onBackground,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Divider(
                            modifier = Modifier.weight(1f),
                            color = colorScheme.onBackground,
                            thickness = 1.dp
                        )
                    }

                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }

item {

    // Social login buttons
    OutlinedButton(
        onClick = { /* Google login */ },
        modifier = Modifier.fillMaxWidth(),
        border = BorderStroke(1.dp, colorScheme.onBackground), // Uses theme outline color
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = colorScheme.onBackground, // Adapts text/icon color to theme
            containerColor = colorScheme.background // Keeps it outlined
        )

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.google_2504739),
                contentDescription = "Google logo",
                modifier = Modifier.size(20.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(8.dp)) // Adds spacing between icon and text
            Text(
                text = "Continue with Google",
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurface
            )
        }
    }
}


item {
    Spacer(modifier = Modifier.height(16.dp))
}


        }

        }
    }


    LaunchedEffect(signupResult) {
        signupResult?.let {
            it.fold(
                onSuccess = {
                    Toast.makeText(context, "✅ Account created", Toast.LENGTH_SHORT).show()
                    navController.navigate(NavDestination.UserInfo.route) {
                        popUpTo(NavDestination.SignUp.route) { inclusive = true }
                    }
                    viewModel.clearSignupResult()
                },
                onFailure = { error ->
                    Toast.makeText(context, error.message ?: "Sign-up failed", Toast.LENGTH_SHORT).show()
                    viewModel.clearSignupResult()
                }
            )
        }
    }

}


@Preview(showBackground = true)
@Composable
fun SignupScreenPreview() {
    AthloTheme {
        SignupScreen(navController = NavController(LocalContext.current))

    }
}