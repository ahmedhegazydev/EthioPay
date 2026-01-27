package com.example.ethiopay.payment.models

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
}