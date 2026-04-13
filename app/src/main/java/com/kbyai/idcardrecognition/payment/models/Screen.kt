package com.kbyai.idcardrecognition.payment.models

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Gate : Screen("gate")
    object Login : Screen("login")
    object Home : Screen("home")
    object Face : Screen("face")
    object Kyc :  Screen("kyc")
}