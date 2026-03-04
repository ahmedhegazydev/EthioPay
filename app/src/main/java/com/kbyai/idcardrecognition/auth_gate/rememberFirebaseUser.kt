package com.kbyai.idcardrecognition.auth_gate

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

@Composable
fun rememberFirebaseUser(): State<FirebaseUser?> {
    val auth = remember { FirebaseAuth.getInstance() }
    return produceState(initialValue = auth.currentUser, auth) {
        val listener = FirebaseAuth.AuthStateListener {
            value = it.currentUser
        }
        auth.addAuthStateListener(listener)
        awaitDispose { auth.removeAuthStateListener(listener) }
    }
}
