package com.example.ethiopay.payment.stripe

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

suspend fun fetchSetupIntentClientSecret(): String {
    val url = "https://stripe-backend-smjf.onrender.com/create-setup-intent"
    val conn = URL(url).openConnection() as HttpURLConnection
    conn.requestMethod = "POST"
    conn.setRequestProperty("Content-Type", "application/json")
    conn.doOutput = true
    conn.outputStream.use { it.write("{}".toByteArray()) }

    val body = conn.inputStream.bufferedReader().readText()
    val json = JSONObject(body)
    return json.getString("clientSecret")
}

fun fetchClientSecret(): String {
    val url = URL("https://stripe-backend-smjf.onrender.com/create-payment-intent")
    val conn = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        setRequestProperty("Content-Type", "application/json")
        doOutput = true
    }
    conn.outputStream.use { os ->
        // مش محتاج body لو backend ثابت، بس نخليه فاضي
        os.write("{}".toByteArray())
    }
    val code = conn.responseCode
    val body = (if (code in 200..299) conn.inputStream else conn.errorStream)
        .bufferedReader().readText()

    if (code !in 200..299) error("Backend error $code: $body")

    val json = JSONObject(body)
    return json.getString("clientSecret")
}