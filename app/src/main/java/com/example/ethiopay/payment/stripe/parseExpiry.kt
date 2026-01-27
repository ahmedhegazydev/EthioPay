package com.example.ethiopay.payment.stripe

fun parseExpiry(s: String?): Pair<Int?, Int?> {
    if (s.isNullOrBlank()) return null to null
    val parts = s.replace(" ", "").split("/", "-")
    if (parts.size < 2) return null to null
    val mm = parts[0].toIntOrNull()
    var yy = parts[1].toIntOrNull()
    if (yy != null && yy < 100) yy += 2000
    return mm to yy
}
