package com.eathioPay.demo.payment.demo

object DemoApiRaw { suspend fun postJson(url: String, body: String): String { return com.eathioPay.demo.payment.stripe.postJson(
    url,
    body
)
} }