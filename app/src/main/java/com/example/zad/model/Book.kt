package com.example.zad.model

import com.google.firebase.firestore.Exclude
import java.util.UUID

enum class ListingType {
    SALE,
    DONATION
}

enum class BookCondition(val arabicLabel: String) {
    LIKE_NEW("شبه جديد"),
    GOOD("جيد"),
    USED("مستعمل"),
    VERY_GOOD("جيد جدًا"),
    ACCEPTABLE("مقبول");

    @get:Exclude
    val label: String get() = arabicLabel
}

data class Book(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val major: String = "",
    val academicLevel: String = "",
    val price: Double? = null,
    val listingType: ListingType = ListingType.SALE,
    val condition: BookCondition = BookCondition.GOOD,
    val contactPhone: String = "",
    val description: String = "",
    val ownerUid: String = "",
    val isCompleted: Boolean = false
)

fun String.toWesternDigits(): String = map { character ->
    when (character) {
        in '٠'..'٩' -> ('0'.code + character.code - '٠'.code).toChar()
        in '۰'..'۹' -> ('0'.code + character.code - '۰'.code).toChar()
        else -> character
    }
}.joinToString("")