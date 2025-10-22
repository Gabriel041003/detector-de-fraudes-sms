package com.example.detectordefraudes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.navigation.compose.rememberNavController
import com.example.detectordefraudes.ui.NavGraph
import com.example.detectordefraudes.viewmodel.DenunciaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val denunciaViewModel: DenunciaViewModel by viewModels()

            Surface(color = MaterialTheme.colorScheme.background) {
                // 🔧 usa seu grafo de navegação normal
                NavGraph(navController = navController, denunciaViewModel = denunciaViewModel)
            }
        }
    }
}

