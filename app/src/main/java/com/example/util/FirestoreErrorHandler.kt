package com.example.util

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException
import org.json.JSONObject

enum class OperationType {
    GET, LIST, CREATE, UPDATE, DELETE
}

private const val TAG = "FirestoreError"

fun handleFirestoreError(exception: Exception, operationType: OperationType, path: String) {
    val json = JSONObject()
    json.put("operation", operationType.name)
    json.put("targetPath", path)

    if (exception is FirebaseFirestoreException) {
        json.put("firebaseErrorCode", exception.code.name)
        json.put("message", exception.message ?: "Unknown Firestore error")
    } else {
        json.put("errorType", exception.javaClass.simpleName)
        json.put("message", exception.message ?: "Unknown error")
    }

    Log.e(TAG, json.toString(), exception)
}
