package com.example.tagtodoproject

import com.google.firebase.firestore.PropertyName

data class Task(
    @get:PropertyName("id") @set:PropertyName("id") var id: Int = 0,
    @get:PropertyName("name") @set:PropertyName("name") var name: String = "",
    @get:PropertyName("date") @set:PropertyName("date") var date: String = "",
    @get:PropertyName("tags") @set:PropertyName("tags") var tags: String = "",
    @get:PropertyName("category") @set:PropertyName("category") var category: String = "",
    @get:PropertyName("isCompleted") @set:PropertyName("isCompleted") var isCompleted: Boolean = false
) {
    // Konstruktor kosong untuk Firebase
    constructor() : this(0, "", "", "", "", false)
}