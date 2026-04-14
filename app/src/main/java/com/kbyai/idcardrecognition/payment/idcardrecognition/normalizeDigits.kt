package com.kbyai.idcardrecognition.payment.idcardrecognition

fun normalizeDigits(s: String): String {
    val map = mapOf(
        '٠' to '0', '١' to '1', '٢' to '2', '٣' to '3', '٤' to '4',
        '٥' to '5', '٦' to '6', '٧' to '7', '٨' to '8', '٩' to '9',
        '۰' to '0', '۱' to '1', '۲' to '2', '۳' to '3', '۴' to '4',
        '۵' to '5', '۶' to '6', '۷' to '7', '۸' to '8', '۹' to '9',
    )
    return s.map { map[it] ?: it }.joinToString("")
}

fun canonicalDocNumber(raw: String): String {
    val d = normalizeDigits(raw)
        .uppercase()
        .trim()

    // شيل أي حاجة مش حرف/رقم
    return d.filter { it.isLetterOrDigit() }
}
