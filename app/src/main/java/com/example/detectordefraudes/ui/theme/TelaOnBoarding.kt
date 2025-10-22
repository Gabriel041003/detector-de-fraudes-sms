package com.example.detectordefraudes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.detectordefraudes.ui.theme.*
import androidx.compose.ui.graphics.Color


@Composable
fun TelaOnboarding(navController: NavController) {
    val paginas = listOf(
        "Bem-vindo! Este app ajuda você a identificar e evitar fraudes bancárias digitais.",
        "Nós analisamos mensagens, links e imagens suspeitas usando inteligência artificial.",
        "Você pode denunciar conteúdos manualmente ou receber alertas automáticos."
    )

    var paginaAtual by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FuturisticDarkBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = paginas[paginaAtual],
            color = FuturisticTextPrimary,
            fontSize = 20.sp,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (paginaAtual < paginas.lastIndex) {
                    paginaAtual++
                } else {
                    navController.navigate("home")
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = FuturisticAccent)
        ) {
            Text(
                text = if (paginaAtual < paginas.lastIndex) "Próximo" else "Começar agora",
                color = Color.Black
            )
        }
    }
}
