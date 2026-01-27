package com.example.ethiopay.payment.stripe

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

suspend fun postJson(url: String, json: String): String {
    val client = okhttp3.OkHttpClient()
    val body = json
        .toRequestBody("application/json".toMediaTypeOrNull())
    val req = okhttp3.Request.Builder().url(url).post(body).build()
    return client.newCall(req).execute().use { it.body?.string().orEmpty() }
}
