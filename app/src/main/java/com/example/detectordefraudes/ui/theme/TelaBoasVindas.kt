package com.example.detectordefraudes.ui

import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.detectordefraudes.ui.theme.*

@Composable
fun TelaBoasVindas(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FuturisticDarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "FrauDetecta",
                color = FuturisticTextPrimary,
                fontSize = 32.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { navController.navigate("onboarding") },
                colors = ButtonDefaults.buttonColors(containerColor = FuturisticAccent)
            ) {
                Text("Começar", color = Color.Black)
            }
        }
    }
}
