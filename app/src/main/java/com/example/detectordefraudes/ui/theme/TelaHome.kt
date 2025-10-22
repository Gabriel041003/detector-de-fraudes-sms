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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.detectordefraudes.ui.theme.*
import com.example.detectordefraudes.util.BackgroundToggle
import androidx.compose.runtime.saveable.rememberSaveable


@Composable
fun TelaHome(navController: NavController) {
    val context = LocalContext.current

    // Estado do toggle (se quiser persistir entre aberturas do app, migre para DataStore)
    var protecaoAtiva by rememberSaveable { mutableStateOf(false) }

    // Launcher para pedir múltiplas permissões quando o usuário ativa a proteção
    val requestPermsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val allGranted = results.values.all { it }
        if (allGranted) {
            BackgroundToggle.enable(context)
            protecaoAtiva = true
        } else {
            protecaoAtiva = false
            // opcional: Snackbar/Toast explicando que sem permissões a proteção não será ativada
        }
    }

    fun pedirPermissoesOuAtivar() {
        val perms = buildList {
            add(Manifest.permission.RECEIVE_SMS)
            // READ_SMS só se realmente precisar ler conteúdo historico; para SMS_RECEIVED não é obrigatório na maioria dos casos
            // add(Manifest.permission.READ_SMS)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val faltando = perms.any { p ->
            ContextCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_GRANTED
        }

        if (faltando) {
            requestPermsLauncher.launch(perms.toTypedArray())
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
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Tela Principal",
            color = FuturisticTextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ---- Botões principais
        Button(
            onClick = { navController.navigate("resultado") },
            colors = ButtonDefaults.buttonColors(containerColor = FuturisticAccent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Ver resultado da análise", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { navController.navigate("denuncia") },
            colors = ButtonDefaults.buttonColors(containerColor = FuturisticAccent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Fazer denúncia", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = { navController.navigate("config") },
            colors = ButtonDefaults.buttonColors(containerColor = FuturisticAccent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Configurações", color = Color.Black)
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ---- Seção: Proteção em segundo plano (toggle)
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
                        Text(
                            "Proteção em segundo plano",
                            color = FuturisticTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Ao ativar, o app monitorará automaticamente novos SMS e exibirá uma notificação persistente. Tudo é processado localmente e pode ser desligado a qualquer momento.",
                            color = FuturisticTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = protecaoAtiva,
                        onCheckedChange = { checked ->
                            if (checked) {
                                pedirPermissoesOuAtivar()
                            } else {
                                BackgroundToggle.disable(context)
                                protecaoAtiva = false
                            }
                        }
                    )
                }
            }
        }
    }
}
