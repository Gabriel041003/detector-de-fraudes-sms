package com.example.detectordefraudes.ui






import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun TelaLogin(navController: NavController) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text("Bem-vindo ao FrauDetecta")
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = {
                navController.navigate("home")
            }) {
                Text("Entrar")
            }
        }
    }
}
