package com.example.zad.repository

import com.example.zad.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val providedAuth: FirebaseAuth? = null,
    private val providedFirestore: FirebaseFirestore? = null
) {
    private val auth: FirebaseAuth by lazy { providedAuth ?: FirebaseAuth.getInstance() }
    private val firestore: FirebaseFirestore by lazy { providedFirestore ?: FirebaseFirestore.getInstance() }

    val currentUid: String?
        get() = auth.currentUser?.uid

    suspend fun signIn(email: String, password: String): User {
        val firebaseUser = auth.signInWithEmailAndPassword(email.trim(), password).await().user
            ?: error("تعذر التحقق من الحساب")
        return User(uid = firebaseUser.uid, email = firebaseUser.email.orEmpty())
    }

    suspend fun signUp(fullName: String, email: String, major: String, phone: String, password: String): User {
        val firebaseUser = auth.createUserWithEmailAndPassword(email.trim(), password).await().user
            ?: error("تعذر إنشاء الحساب")
        val user = User(
            uid = firebaseUser.uid,
            fullName = fullName.trim(),
            email = email.trim(),
            major = major.trim(),
            phone = phone.trim()
        )
        return user
    }

    suspend fun persistUserProfile(user: User) {
        firestore.collection("users").document(user.uid).set(user).await()
    }

    suspend fun fetchUser(uid: String, emailFallback: String = ""): User {
        val snapshot = firestore.collection("users").document(uid).get().await()
        return snapshot.toObject(User::class.java)?.copy(uid = uid)
            ?: User(uid = uid, email = emailFallback)
    }

    fun signOut() {
        auth.signOut()
    }
}