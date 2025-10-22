package com.example.detectordefraudes

import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.detectordefraudes.ui.theme.*
import com.example.detectordefraudes.viewmodel.DenunciaViewModel

@Composable
fun TelaDenuncia(
    navController: NavController,
    denunciaViewModel: DenunciaViewModel = viewModel()
) {
    var mensagem by remember { mutableStateOf(TextFieldValue("")) }
    val mensagensEnviadas = remember { mutableStateListOf<String>() }
    var imagemUri by remember { mutableStateOf<Uri?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imagemUri = uri
    }

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
            Text("Voltar ao Menu", color = Color.Black)
        }

        Text(
            text = "Chat de Denúncia",
            color = FuturisticTextPrimary,
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        ) {
            mensagensEnviadas.forEach {
                Text(
                    text = it,
                    color = FuturisticTextSecondary,
                    modifier = Modifier
                        .padding(8.dp)
                        .background(FuturisticCard, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                )
            }

            imagemUri?.let {
                Image(
                    painter = rememberAsyncImagePainter(model = it),
                    contentDescription = "Imagem Anexada",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(8.dp)
                        .background(FuturisticCard, RoundedCornerShape(12.dp))
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                imagePickerLauncher.launch("image/*")
            }) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Anexar Imagem",
                    tint = FuturisticAccent
                )
            }

            TextField(
                value = mensagem,
                onValueChange = { mensagem = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Digite sua denúncia...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = FuturisticCard,
                    unfocusedContainerColor = FuturisticCard,
                    focusedTextColor = FuturisticTextPrimary,
                    unfocusedTextColor = FuturisticTextPrimary,
                    cursorColor = FuturisticAccent
                )
            )

            IconButton(onClick = {
                if (mensagem.text.isNotBlank()) {
                    mensagensEnviadas.add("Você: ${mensagem.text}")
                    denunciaViewModel.analisarDenuncia(mensagem.text) // ✅ IA local agora
                    imagemUri = null
                    mensagem = TextFieldValue("")

                    Handler(Looper.getMainLooper()).postDelayed({
                        mensagensEnviadas.add("Sistema: Sua denúncia foi recebida e está em análise.")
                    }, 500)
                }
            }) {
                Icon(
                    imageVector = Icons.Filled.Send,
                    contentDescription = "Enviar",
                    tint = FuturisticAccent
                )
            }
        }
    }
}

