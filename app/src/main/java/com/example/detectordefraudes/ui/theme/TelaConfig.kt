package com.example.detectordefraudes.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.core.content.ContextCompat
import com.example.detectordefraudes.ui.theme.FuturisticAccent
import com.example.detectordefraudes.ui.theme.FuturisticCard
import com.example.detectordefraudes.ui.theme.FuturisticDarkBackground
import com.example.detectordefraudes.ui.theme.FuturisticTextPrimary
import com.example.detectordefraudes.ui.theme.FuturisticTextSecondary
import com.example.detectordefraudes.util.BackgroundToggle

@Composable
fun TelaConfig(navController: NavController) {
    val context = LocalContext.current
    var protecaoAtiva by rememberSaveable { mutableStateOf(false) }
    var mostrarDialog by remember { mutableStateOf(false) }

    val requestPerms = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val allGranted = result.values.all { it }
        if (allGranted) {
            BackgroundToggle.enable(context)
            protecaoAtiva = true
        } else {
            protecaoAtiva = false
        }
    }

    fun pedirPermissoesOuAtivar() {
        val perms = buildList {
            add(Manifest.permission.RECEIVE_SMS)
            // READ_SMS só se realmente precisar ler histórico; para SMS_RECEIVED, na maioria das ROMs não é necessário.
            // add(Manifest.permission.READ_SMS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        val faltando = perms.any { p ->
            ContextCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_GRANTED
        }
        if (faltando) {
            requestPerms.launch(perms.toTypedArray())
        } else {
            BackgroundToggle.enable(context)
            protecaoAtiva = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FuturisticDarkBackground)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Configurações", color = FuturisticTextPrimary, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        // Card do toggle de proteção em segundo plano
        Card(
            colors = CardDefaults.cardColors(containerColor = FuturisticCard),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Proteção em segundo plano", color = FuturisticTextPrimary)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Ao ativar, o app monitorará automaticamente novos SMS e exibirá uma notificação persistente. Tudo é processado localmente e pode ser desligado a qualquer momento.",
                            color = FuturisticTextSecondary
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = protecaoAtiva,
                        onCheckedChange = { checked ->
                            if (checked) {
                                mostrarDialog = true
                            } else {
                                BackgroundToggle.disable(context)
                                protecaoAtiva = false
                            }
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("home") },
            colors = ButtonDefaults.buttonColors(containerColor = FuturisticAccent)
        ) { Text("Voltar ao Menu", color = Color.Black) }
    }

    if (mostrarDialog) {
        AlertDialog(
            onDismissRequest = { mostrarDialog = false },
            title = { Text("Ativar proteção") },
            text = { Text("Ao ativar, o app monitorará SMS novos automaticamente e exibirá uma notificação persistente. O processamento é 100% local.") },
            confirmButton = {
                TextButton(onClick = {
                    mostrarDialog = false
                    pedirPermissoesOuAtivar()
                }) { Text("Ativar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialog = false }) { Text("Cancelar") }
            }
        )
    }
}
