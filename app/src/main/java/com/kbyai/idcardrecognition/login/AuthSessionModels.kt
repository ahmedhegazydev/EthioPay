package com.kbyai.idcardrecognition.login

data class SavedAuthSnapshot(
    val prismaUserId: String? = null,
    val firebaseUid: String? = null,
)

data class FirebaseIdentity(
    val uid: String,
    val phoneNumber: String?,
)

data class FirebaseAuthExchangeRequest(
    val firebaseUid: String,
    val phone: String?,
)

data class FirebaseAuthExchangeResponse(
    val userId: String,
)
