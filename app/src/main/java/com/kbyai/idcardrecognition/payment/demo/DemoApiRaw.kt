package com.kbyai.idcardrecognition.payment.demo

object DemoApiRaw { suspend fun postJson(url: String, body: String): String { return com.kbyai.idcardrecognition.payment.stripe.postJson(
    url,
    body
)
} }