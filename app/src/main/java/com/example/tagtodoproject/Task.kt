package com.example.tagtodoproject

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Task(
    var name: String = "",
    var date: String = "",
    var tags: String = "",
    var category: String = "",
    var isCompleted: Boolean = false
) {
    @Exclude
    var documentId: String = ""

    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "date" to date,
            "tags" to tags,
            "category" to category,
            "isCompleted" to isCompleted
        )
    }
}
