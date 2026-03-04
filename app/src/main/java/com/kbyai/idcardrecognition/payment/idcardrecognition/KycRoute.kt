package com.kbyai.idcardrecognition.payment.idcardrecognition

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.kbyai.idcardrecognition.payment.models.Screen

@Composable
fun KycRoute(
    navController: NavHostController,
    onResult: (String) -> Unit
) {
    val context = LocalContext.current
    var launched by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { res ->
        if (res.resultCode == Activity.RESULT_OK) {
            val json = res.data?.getStringExtra("result_json").orEmpty()
            onResult(json)
        } else {
//            navController.navigate(Screen.Home.route) {
//                popUpTo(Screen.Kyc.route) { inclusive = true }
//                launchSingleTop = true
//            }
            navController.navigate(Screen.Face.route) {
                popUpTo(Screen.Kyc.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(Unit) {
        if (!launched) {
            launched = true
            launcher.launch(Intent(context, CameraActivityKt::class.java))
        }
    }

    // UI بسيط أثناء فتح الكاميرا
    Text("Opening ID scan...")
}

