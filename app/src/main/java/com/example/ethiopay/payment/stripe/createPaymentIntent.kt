package com.example.ethiopay.payment.stripe

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

 suspend fun createPaymentIntent(
    baseUrl: String,
    amount: Int,
    currency: String
): String = withContext(Dispatchers.IO) {
    val url = URL("$baseUrl/create-payment-intent")
    val conn = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        setRequestProperty("Content-Type", "application/json")
        doOutput = true
    }

    val body = JSONObject()
        .put("amount", amount)
        .put("currency", currency)
        .toString()

    conn.outputStream.use { it.write(body.toByteArray()) }

    val code = conn.responseCode
    val stream = if (code in 200..299) conn.inputStream else conn.errorStream
    val resp = stream.bufferedReader().readText()

    if (code !in 200..299) error("HTTP $code: $resp")

    val json = JSONObject(resp)
    json.getString("clientSecret")
}
