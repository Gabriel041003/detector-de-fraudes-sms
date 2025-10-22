package com.example.detectordefraudes.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.detectordefraudes.TelaDenuncia
import com.example.detectordefraudes.TelaResultado

import com.example.detectordefraudes.viewmodel.DenunciaViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    denunciaViewModel: DenunciaViewModel
) {
    NavHost(navController = navController, startDestination = "boasvindas") {
        composable("login") { TelaLogin(navController) }
        composable("home") { TelaHome(navController) }

        composable("denuncia") {
            TelaDenuncia(
                navController = navController,
                denunciaViewModel = denunciaViewModel
            )
        }

        composable("resultado") {
            TelaResultado(
                navController = navController,
                denunciaViewModel = denunciaViewModel
            )
        }

        composable("configuracoes") { TelaConfig(navController) }
        composable("boasvindas") { TelaBoasVindas(navController) }
        composable("onboarding") { TelaOnboarding(navController) }
    }
}



