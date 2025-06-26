package com.youcef_bounaas.athlo.Authentication.presentation


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.youcef_bounaas.athlo.ui.theme.AthloTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.youcef_bounaas.athlo.Navigation.presentation.NavDestination


@Composable
fun AuthScreen(navController: NavController) {
Column (
    modifier = Modifier.fillMaxSize()
        .padding(16.dp),
    verticalArrangement = Arrangement.SpaceBetween,
    horizontalAlignment = Alignment.CenterHorizontally
){


   Card (
        modifier = Modifier
            .fillMaxWidth()
            .weight(4f)
    ) {


    }

    Spacer(modifier = Modifier.height(24.dp))

    Column(
        modifier = Modifier


    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),

            onClick = { navController.navigate(NavDestination.SignUp.route) },
        ) {
            Text(
                "Join for free",

            )
        }

        Button(
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor =  MaterialTheme.colorScheme.primary
            ),
            onClick = {
                navController.navigate(NavDestination.Login.route)
            },
        ) {
            Text("Log In")
        }

    }


}

}













@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    AthloTheme {
        AuthScreen(navController = NavController(LocalContext.current))

    }
}