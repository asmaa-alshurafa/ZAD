package com.example.zad.repository

import android.content.res.Configuration
import androidx.annotation.StringRes
import com.example.zad.R
import com.example.zad.model.AppNotification
import com.example.zad.model.Book
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.tasks.await
import java.util.Locale

class NotificationRepository(
    private val firestoreProvider: () -> FirebaseFirestore = { FirebaseFirestore.getInstance() }
) {
    private val firestore by lazy(LazyThreadSafetyMode.SYNCHRONIZED, firestoreProvider)

    fun listenToUserNotifications(
        uid: String,
        onNotificationsChanged: (List<AppNotification>) -> Unit,
        onError: () -> Unit
    ): ListenerRegistration = firestore.collection("users").document(uid)
        .collection(NOTIFICATIONS_COLLECTION)
        .addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError()
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener
            onNotificationsChanged(
                snapshot.documents.mapNotNull { document ->
                    document.toObject(AppNotification::class.java)?.copy(id = document.id)
                }.sortedByDescending { it.createdAt }
            )
        }

    suspend fun markAsRead(uid: String, notificationId: String) {
        firestore.collection("users").document(uid)
            .collection(NOTIFICATIONS_COLLECTION).document(notificationId)
            .update("read", true).await()
    }

    suspend fun notifyStudentsInMajor(book: Book) {
        if (book.major.isBlank()) return
        val students = firestore.collection("users")
            .whereEqualTo("major", book.major)
            .get().await().documents
            .filterNot { it.id == book.ownerUid }
        students.chunked(MAX_BATCH_SIZE).forEach { batchStudents ->
            val batch = firestore.batch()
            batchStudents.forEach { student ->
                val notification = student.reference.collection(NOTIFICATIONS_COLLECTION).document()
                batch.set(notification, mapOf(
                    "bookId" to book.id,
                    "titleAr" to localizedString("ar", R.string.notification_new_book_title),
                    "titleEn" to localizedString("en", R.string.notification_new_book_title),
                    "bodyAr" to localizedString("ar", R.string.notification_new_book_body, book.title),
                    "bodyEn" to localizedString("en", R.string.notification_new_book_body, book.title),
                    "read" to false,
                    "createdAt" to FieldValue.serverTimestamp()
                ))
            }
            batch.commit().await()
        }
    }

    private companion object {
        const val NOTIFICATIONS_COLLECTION = "notifications"
        const val MAX_BATCH_SIZE = 450
    }

    private fun localizedString(language: String, @StringRes resourceId: Int, vararg formatArgs: Any): String {
        val context = FirebaseApp.getInstance().applicationContext
        val configuration = Configuration(context.resources.configuration).apply {
            setLocale(Locale.forLanguageTag(language))
        }
        return context.createConfigurationContext(configuration).getString(resourceId, *formatArgs)
    }
}