package com.example.zad.model

import com.google.firebase.Timestamp

data class AppNotification(
    val id: String = "",
    val bookId: String = "",
    val titleAr: String = "",
    val titleEn: String = "",
    val bodyAr: String = "",
    val bodyEn: String = "",
    val read: Boolean = false,
    val createdAt: Timestamp = Timestamp(0, 0)
)