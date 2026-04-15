package com.eathioPay.demo.payment.models

sealed class Screen(val route: String) {
    object Gate : Screen("gate")
    object Login : Screen("login")
    object Home : Screen("home")
    object Face : Screen("face")

    object Kyc :  Screen("kyc")
//    object BioGate :  Screen("bio_gate")
}