package com.kbyai.idcardrecognition.main

import android.content.Context
import androidx.navigation.NavHostController
import com.kbyai.idcardrecognition.payment.demo.getSavedIdHash
import com.kbyai.idcardrecognition.payment.demo.saveKycHash
import com.kbyai.idcardrecognition.payment.idcardrecognition.canonicalDocNumber
import com.kbyai.idcardrecognition.payment.models.Screen
import org.json.JSONObject
import java.security.MessageDigest
import java.util.Locale

suspend fun handleKycResult(
    context: Context,
    navController: NavHostController,
    json: String,
    showScanDebug: (String) -> Unit,
    showError: (String?) -> Unit,
) {
    try {
        val obj = JSONObject(json)

        val idNumber = obj.optString("Document Number", "").trim()
        val givenAr = obj.optString("Given Names-Arabic (Egypt)", "").trim()
        val surAr = obj.optString("Surname-Arabic (Egypt)", "").trim()
        val dobAr = obj.optString("Date of Birth-Arabic (Egypt)", "").trim()

        val fullName = listOf(surAr, givenAr).filter { it.isNotBlank() }.joinToString(" ")
        val dob = dobAr

        if (idNumber.isBlank()) {
            showError("No Document Number detected. Please scan again.")
            navController.popBackStack()
            return
        }

        val docId = canonicalDocNumber(idNumber)
        if (docId.length < 6) {
            showError("Document Number not clear. Please scan again.")
            navController.popBackStack()
            return
        }

        val newHash = buildIdHash(idNumber = docId, fullName = "", dob = "")
        val saved = getSavedIdHash(context)

        showScanDebug(
            """
            Document Number: $idNumber
            Canonical DocId: $docId
            Full Name: $fullName
            DOB: $dob
            
            New Hash: $newHash
            Saved Hash: ${saved ?: "(none)"}
            """.trimIndent()
        )

        if (saved == null) {
            saveKycHash(context, newHash)
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Kyc.route) { inclusive = true }
                launchSingleTop = true
            }
            return
        }

        if (saved != newHash) {
            showError("ID mismatch: البيانات لا تطابق البطاقة المسجلة")
            navController.popBackStack()
            return
        }

        navController.navigate(Screen.Home.route) {
            popUpTo(Screen.Kyc.route) { inclusive = true }
            launchSingleTop = true
        }

    } catch (e: Exception) {
        showError("KYC parse error: ${e.message}")
        navController.popBackStack()
    }
}


private fun normalize(s: String?): String {
    return (s ?: "")
        .trim()
        .lowercase(Locale.ROOT)
        .replace("\\s+".toRegex(), " ")
}

fun buildIdHash(
    idNumber: String?,
    fullName: String?,
    dob: String? // "YYYY-MM-DD" مثلاً
): String {
    val payload = listOf(
        normalize(idNumber),
        normalize(fullName),
        normalize(dob),
    ).joinToString("|")

    val bytes = MessageDigest.getInstance("SHA-256").digest(payload.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}