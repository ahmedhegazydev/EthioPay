package com.example.ethiopay.scan

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Home : Screen("home")
}
