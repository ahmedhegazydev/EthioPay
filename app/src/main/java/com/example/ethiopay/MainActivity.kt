package com.example.ethiopay

import HomeScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ethiopay.login.FirebasePhoneAuthScreen
import com.example.ethiopay.scan.Screen
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            val start = remember {
                if (FirebaseAuth.getInstance().currentUser != null) Screen.Home.route
                else Screen.Login.route
            }

            NavHost(
                navController = navController,
//                startDestination = start
//                startDestination = Screen.Login.route
                startDestination = Screen.Home.route
            ) {
                composable(Screen.Login.route) {
                    FirebasePhoneAuthScreen(
                        onLoginSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Home.route) {
                    HomeScreen(
//                        onLogout = {
//                            FirebaseAuth.getInstance().signOut()
//                            navController.navigate(Screen.Login.route) {
//                                popUpTo(Screen.Home.route) { inclusive = true }
//                            }
//                        }
                    )
                }
            }
        }
    }
}
