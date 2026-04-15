package com.eathioPay.demo.payment.stripe

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

private val httpClient = OkHttpClient()

suspend fun postJson(url: String, json: String): String = withContext(Dispatchers.IO) {
    val body = json.toRequestBody("application/json".toMediaType())
    val req = Request.Builder().url(url).post(body).build()
    httpClient.newCall(req).execute().use { resp ->
        resp.body?.string().orEmpty()
    }
}
