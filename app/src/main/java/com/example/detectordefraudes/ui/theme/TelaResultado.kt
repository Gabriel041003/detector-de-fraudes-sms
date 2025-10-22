package com.example.detectordefraudes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.detectordefraudes.ui.theme.*
import com.example.detectordefraudes.viewmodel.DenunciaViewModel

@Composable
fun TelaResultado(
    navController: NavController,
    denunciaViewModel: DenunciaViewModel = viewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FuturisticDarkBackground)
            .padding(16.dp)
    ) {
        Button(
            onClick = { navController.navigate("home") },
            modifier = Modifier.padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = FuturisticAccent)
        ) {
            Text("Voltar ao Menu", color = androidx.compose.ui.graphics.Color.Black)
        }

        Text(
            text = "Resultados da Análise",
            style = MaterialTheme.typography.headlineSmall,
            color = FuturisticTextPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (denunciaViewModel.denuncias.isEmpty()) {
            Text(
                text = "Nenhuma denúncia enviada ainda.",
                color = FuturisticTextSecondary
            )
        } else {
            denunciaViewModel.denuncias.forEachIndexed { index, denuncia ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = FuturisticCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Denúncia #${index + 1}",
                            color = FuturisticTextPrimary,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = denuncia.texto,
                            color = FuturisticTextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Status: ${denuncia.status} (Confiança: ${"%.0f".format((denuncia.probabilidade ?: 0f) * 100)}%)",
                            color = if (denuncia.status.contains("fraude", ignoreCase = true))
                                FuturisticAccent else FuturisticTextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                // Aqui futuramente: detalhes da denúncia
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FuturisticAccent)
                        ) {
                            Text("Ver Detalhes", color = androidx.compose.ui.graphics.Color.Black)
                        }
                    }
                }
            }
        }
    }
}
