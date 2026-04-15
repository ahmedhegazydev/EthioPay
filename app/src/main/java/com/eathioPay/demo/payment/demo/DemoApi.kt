package com.eathioPay.demo.payment.demo

//import com.example.ethiopay.BuildConfig

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object DemoApi {
    // ✅ Emulator base URL
//    var baseUrl: String = "http://10.0.2.2:3000"
    var baseUrl: String = "https://stripe-backend-smjf.onrender.com"
//    var baseUrl: String =
//        if (BuildConfig.DEMO_MODE)
//            "http://10.0.2.2:3000"
//        else "https://stripe-backend-smjf.onrender.com" // أو production domain لاحقًا


    // ✅ Demo admin key (for /admin/* endpoints)
    var adminKey: String = "dev_admin_123"

    // -------------------- HTTP helpers (with optional headers) --------------------

    private suspend fun get(url: String, headers: Map<String, String> = emptyMap()): String =
        withContext(Dispatchers.IO) {
            requestWithRetry { doGet(url, headers) }
        }

    private fun doGet(url: String, headers: Map<String, String>): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Accept", "application/json")
            headers.forEach { (k, v) -> setRequestProperty(k, v) }

            // ✅ مهم
            connectTimeout = 20_000
            readTimeout = 40_000
        }

        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val body = stream.bufferedReader().use { it.readText() }
        if (code !in 200..299) throw RuntimeException(body)
        return body
    }

    private suspend fun post(
        url: String,
        jsonBody: String,
        headers: Map<String, String> = emptyMap()
    ): String = withContext(Dispatchers.IO) {
        requestWithRetry { doPost(url, jsonBody, headers) }
    }

    private fun doPost(url: String, jsonBody: String, headers: Map<String, String>): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
            headers.forEach { (k, v) -> setRequestProperty(k, v) }

            // ✅ مهم
            connectTimeout = 20_000
            readTimeout = 40_000
        }

        conn.outputStream.use { it.write(jsonBody.toByteArray(Charsets.UTF_8)) }

        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val body = stream.bufferedReader().use { it.readText() }
        if (code !in 200..299) throw RuntimeException(body)
        return body
    }

    suspend fun requestOtp(phone: String): OtpSessionDto {
        val body = JSONObject().apply {
            put("phone", phone)
        }.toString()
        val obj = JSONObject(post("$baseUrl/auth/request-otp", body))
        return OtpSessionDto(
            userId = obj.getString("userId"),
            phone = obj.getString("phone"),
            otpRequired = obj.optBoolean("otpRequired", true),
        )
    }

    suspend fun verifyOtp(phone: String, otp: String): AuthSessionDto {
        val body = JSONObject().apply {
            put("phone", phone)
            put("otp", otp)
        }.toString()
        val obj = JSONObject(post("$baseUrl/auth/verify-otp", body))
        return AuthSessionDto(
            userId = obj.getString("userId"),
            phone = obj.getString("phone"),
        )
    }

    // ✅ Retry بسيط مع backoff
    private suspend fun <T> requestWithRetry(
        maxRetries: Int = 1, // خليها 1 أو 2
        block: () -> T
    ): T {
        var last: Throwable? = null
        repeat(maxRetries + 1) { attempt ->
            try {
                return block()
            } catch (e: java.net.SocketTimeoutException) {
                last = e
            } catch (e: java.io.IOException) {
                last = e
            }
            // backoff بسيط
            if (attempt < maxRetries) kotlinx.coroutines.delay(1200L * (attempt + 1))
        }
        throw last ?: RuntimeException("Unknown network error")
    }

    // -------------------- User APIs --------------------

    suspend fun getWallet(userId: String): WalletDto {
        val res = get("$baseUrl/wallet/$userId")
        val obj = JSONObject(res)
        return WalletDto(
            userId = obj.getString("userId"),
            currency = obj.getString("currency"),
            balance = obj.getInt("balance")
        )
    }

    suspend fun getTransactions(userId: String): List<TxDto> {
        val res = get("$baseUrl/transactions/$userId")
        val obj = JSONObject(res)
        val arr = obj.getJSONArray("items")
        return (0 until arr.length()).map { i ->
            val t = arr.getJSONObject(i)
            TxDto(
                id = t.getString("id"),
                fromUserId = t.getString("fromUserId"),
                toUserId = t.getString("toUserId"),
                amount = t.getInt("amount"),
                currency = t.optString("currency", "ETB"),
                status = t.getString("status"),
                note = if (t.isNull("note")) null else t.getString("note"),
                createdAt = t.getString("createdAt")
            )
        }
    }

    suspend fun transfer(fromUserId: String, toUserId: String, amount: Int, note: String?): TxDto {
        val body = JSONObject().apply {
            put("fromUserId", fromUserId)
            put("toUserId", toUserId)
            put("amount", amount)
            if (!note.isNullOrBlank()) put("note", note)
        }.toString()

        val res = post("$baseUrl/transfer", body)
        val obj = JSONObject(res)
        val tx = obj.getJSONObject("tx")

        return TxDto(
            id = tx.getString("id"),
            fromUserId = tx.getString("fromUserId"),
            toUserId = tx.getString("toUserId"),
            amount = tx.getInt("amount"),
            currency = tx.optString("currency", "ETB"),
            status = tx.getString("status"),
            note = if (tx.isNull("note")) null else tx.getString("note"),
            createdAt = tx.getString("createdAt")
        )
    }

    suspend fun topup(userId: String, amount: Int): WalletDto {
        val body = JSONObject().apply { put("amount", amount) }.toString()
        val res = post("$baseUrl/wallet/$userId/topup", body)
        val obj = JSONObject(res)

        val balance = obj.getInt("balance")
        return WalletDto(userId = userId, currency = "ETB", balance = balance)
    }

    // -------------------- Admin APIs --------------------

    private fun adminHeaders(): Map<String, String> = mapOf("x-admin-key" to adminKey)

    suspend fun adminGetUsers(): List<AdminUserDto> {
        val res = get("$baseUrl/admin/users", headers = adminHeaders())
        val arr = JSONObject(res).getJSONArray("items")

        return (0 until arr.length()).map { i ->
            val u = arr.getJSONObject(i)
            AdminUserDto(
                id = u.getString("id"),
                phone = if (u.isNull("phone")) null else u.getString("phone"),
                kycApproved = u.getBoolean("kycApproved"),
                isFrozen = u.getBoolean("isFrozen"),
                createdAt = u.getString("createdAt")
            )
        }
    }

    suspend fun adminApproveKyc(userId: String) {
        post("$baseUrl/admin/users/$userId/kyc/approve", "{}", headers = adminHeaders())
    }

    suspend fun adminFreeze(userId: String) {
        post("$baseUrl/admin/users/$userId/freeze", "{}", headers = adminHeaders())
    }

    suspend fun adminUnfreeze(userId: String) {
        post("$baseUrl/admin/users/$userId/unfreeze", "{}", headers = adminHeaders())
    }

    suspend fun adminGetEvents(): List<AdminEventDto> {
        val res = get("$baseUrl/admin/events", headers = adminHeaders())
        val arr = JSONObject(res).getJSONArray("items")

        return (0 until arr.length()).map { i ->
            val e = arr.getJSONObject(i)
            AdminEventDto(
                id = e.getString("id"),
                action = e.getString("action"),
                targetId = if (e.isNull("targetId")) null else e.getString("targetId"),
                meta = if (e.isNull("meta")) null else e.getString("meta"),
                createdAt = e.getString("createdAt")
            )
        }
    }
}

// -------------------- DTOs --------------------

data class WalletDto(val userId: String, val currency: String, val balance: Int)

data class TxDto(
    val id: String,
    val fromUserId: String,
    val toUserId: String,
    val amount: Int,
    val currency: String,
    val status: String,
    val note: String?,
    val createdAt: String,
)

data class AdminUserDto(
    val id: String,
    val phone: String?,
    val kycApproved: Boolean,
    val isFrozen: Boolean,
    val createdAt: String
)

data class AdminEventDto(
    val id: String,
    val action: String,
    val targetId: String?,
    val meta: String?,
    val createdAt: String
)

data class OtpSessionDto(
    val userId: String,
    val phone: String,
    val otpRequired: Boolean,
)

data class AuthSessionDto(
    val userId: String,
    val phone: String,
)
