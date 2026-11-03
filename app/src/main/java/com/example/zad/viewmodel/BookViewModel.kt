package com.example.zad.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.zad.R
import com.example.zad.model.Book
import com.example.zad.model.BookCondition
import com.example.zad.model.ListingType
import com.example.zad.repository.NotificationRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class BookViewModel(
    private val firestoreProvider: () -> FirebaseFirestore = { FirebaseFirestore.getInstance() }
) : ViewModel() {
    private val firestore by lazy(LazyThreadSafetyMode.SYNCHRONIZED, firestoreProvider)
    private val notificationRepository by lazy { NotificationRepository(firestoreProvider) }
    private val listenerExecutor: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "zad-firestore-listener").apply { isDaemon = true }
    }
    private val seedBooks = listOf(
        Book(
            id = "seed-algorithms",
            title = "مقدمة في الخوارزميات",
            major = "علوم الحاسوب",
            academicLevel = "السنة الثانية",
            price = 32.0,
            listingType = ListingType.SALE,
            condition = BookCondition.VERY_GOOD,
            contactPhone = "+972505550101",
            description = "مرجع شامل لمفاهيم الخوارزميات وتحليلها.",
            ownerUid = "demo"
        ),
        Book(
            id = "seed-calculus",
            title = "أساسيات التفاضل والتكامل",
            major = "الرياضيات",
            academicLevel = "السنة الأولى",
            listingType = ListingType.DONATION,
            condition = BookCondition.GOOD,
            contactPhone = "+972505550102",
            description = "نسخة مناسبة للمقررات التمهيدية في الرياضيات.",
            ownerUid = "demo"
        ),
        Book(
            id = "seed-economics",
            title = "مبادئ الاقتصاد",
            major = "إدارة الأعمال",
            academicLevel = "السنة الأولى",
            price = 18.5,
            listingType = ListingType.SALE,
            condition = BookCondition.LIKE_NEW,
            contactPhone = "+972505550103",
            description = "كتاب تمهيدي في مبادئ الاقتصاد الجزئي والكلي.",
            ownerUid = "demo"
        )
    )

    private val _books = MutableStateFlow(seedBooks)
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow<ListingType?>(null)
    val selectedFilter: StateFlow<ListingType?> = _selectedFilter.asStateFlow()

    private val _savedBookIds = MutableStateFlow<Set<String>>(emptySet())
    val savedBookIds: StateFlow<Set<String>> = _savedBookIds.asStateFlow()

    private val _errorMessage = MutableStateFlow<Int?>(null)
    val errorMessage: StateFlow<Int?> = _errorMessage.asStateFlow()

    private val _successMessage = MutableStateFlow<Int?>(null)
    val successMessage: StateFlow<Int?> = _successMessage.asStateFlow()

    private var currentUserUid = ""
    private var booksListener: ListenerRegistration? = null
    private var savedBooksListener: ListenerRegistration? = null
    private var hasReceivedRemoteSnapshot = false

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                listenToRemoteFirestore()
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (error: Exception) {
                Log.w(TAG, "Could not attach the books listener; using local seed data", error)
                useLocalFallback()
            }
        }
    }

    val filteredBooks: List<Book>
        get() {
            val query = _searchQuery.value.trim()
            val filter = _selectedFilter.value
            return _books.value.filter { book ->
                (query.isBlank() || listOf(book.title, book.major, book.academicLevel)
                    .any { it.contains(query, ignoreCase = true) }) &&
                    (filter == null || book.listingType == filter)
            }
        }

    val myListings: List<Book>
        get() = _books.value.filter { it.ownerUid == currentUserUid && currentUserUid.isNotBlank() }

    val savedBooks: List<Book>
        get() = _books.value.filter { it.id in _savedBookIds.value }

    fun setCurrentUser(uid: String?) {
        val nextUid = uid.orEmpty()
        if (nextUid == currentUserUid) return
        currentUserUid = nextUid
        _savedBookIds.value = emptySet()
        viewModelScope.launch(Dispatchers.IO) {
            savedBooksListener?.remove()
            savedBooksListener = null
            if (nextUid == currentUserUid && nextUid.isNotBlank() && nextUid != "guest") {
                listenToSavedBooks(nextUid)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onFilterSelected(type: ListingType?) {
        _selectedFilter.value = type
    }

    fun clearSearchAndFilter() {
        _searchQuery.value = ""
        _selectedFilter.value = null
    }

    fun clearError() {
        _errorMessage.value = null
    }

    fun clearSuccessMessage() {
        _successMessage.value = null
    }

    fun addBook(book: Book) {
        val newBook = book.copy(
            id = UUID.randomUUID().toString(),
            ownerUid = currentUserUid.ifBlank { "guest" }
        )
        if (currentUserUid.isBlank() || currentUserUid == "guest") {
            _books.value = listOf(newBook) + _books.value
            _successMessage.value = R.string.listing_posted
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestore.collection("books").document(newBook.id).set(newBook).await()
                _successMessage.value = R.string.listing_posted
                try {
                    notificationRepository.notifyStudentsInMajor(newBook)
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (error: Exception) {
                    Log.w(TAG, "Could not notify students about the new listing", error)
                }
                _errorMessage.value = null
                _successMessage.value = R.string.listing_posted
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _books.value = listOf(newBook) + _books.value
                _errorMessage.value = R.string.listing_sync_error
            }
        }
    }

    fun toggleSaveBook(book: Book) {
        toggleSaveBook(book.id)
    }

    fun toggleSaveBook(bookId: String) {
        val isSaved = bookId in _savedBookIds.value
        _savedBookIds.value = if (isSaved) _savedBookIds.value - bookId else _savedBookIds.value + bookId
        if (currentUserUid.isBlank() || currentUserUid == "guest") return
        viewModelScope.launch(Dispatchers.IO) {
            val savedRef = firestore.collection("users").document(currentUserUid)
                .collection("savedBooks").document(bookId)
            try {
                if (isSaved) savedRef.delete().await()
                else savedRef.set(mapOf("bookId" to bookId)).await()
                _errorMessage.value = null
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _errorMessage.value = R.string.app_error_sync_saved
            }
        }
    }

    fun markAsDelivered(book: Book) {
        updateLocalBook(book.id) { it.copy(isCompleted = !it.isCompleted) }
        if (book.ownerUid.isNotBlank() && book.ownerUid != "guest") {
            updateRemoteBook(book.id, "isCompleted", !book.isCompleted)
        }
    }

    fun deleteBook(book: Book) {
        _books.value = _books.value.filterNot { it.id == book.id }
        if (book.id in _savedBookIds.value) _savedBookIds.value = _savedBookIds.value - book.id
        if (book.ownerUid != "guest" && book.ownerUid.isNotBlank()) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    firestore.collection("books").document(book.id).delete().await()
                    _errorMessage.value = null
                } catch (cancellation: CancellationException) {
                    throw cancellation
                } catch (_: Exception) {
                    _errorMessage.value = R.string.app_error_delete
                }
            }
        }
    }

    fun isSaved(book: Book): Boolean = book.id in _savedBookIds.value

    private fun listenToRemoteFirestore() {
        booksListener = firestore.collection("books").addSnapshotListener(listenerExecutor) { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Firestore books listener failed; retaining local data", error)
                if (!hasReceivedRemoteSnapshot) {
                    useLocalFallback()
                    _errorMessage.value = R.string.app_error_database
                }
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener
            if (snapshot.metadata.isFromCache && snapshot.isEmpty && !hasReceivedRemoteSnapshot) {
                useLocalFallback()
                return@addSnapshotListener
            }
            try {
                val remoteBooks = snapshot.documents.mapNotNull { document ->
                    document.toObject(Book::class.java)?.copy(id = document.id)
                }
                hasReceivedRemoteSnapshot = true
                _books.value = remoteBooks
                _errorMessage.value = null
            } catch (error: Exception) {
                Log.w(TAG, "Could not parse a Firestore books snapshot; retaining local data", error)
                if (!hasReceivedRemoteSnapshot) useLocalFallback()
            }
        }
    }

    private fun useLocalFallback() {
        val locallyCreatedBooks = _books.value.filter { it.ownerUid == "guest" }
        _books.value = (locallyCreatedBooks + seedBooks).distinctBy(Book::id)
    }

    private fun listenToSavedBooks(uid: String) {
        savedBooksListener = firestore.collection("users").document(uid)
            .collection("savedBooks").addSnapshotListener(listenerExecutor) { snapshot, error ->
                if (error == null && snapshot != null) {
                    _savedBookIds.value = snapshot.documents.map { it.id }.toSet()
                }
            }
    }

    private fun updateLocalBook(bookId: String, update: (Book) -> Book) {
        _books.value = _books.value.map { book -> if (book.id == bookId) update(book) else book }
    }

    private fun updateRemoteBook(bookId: String, field: String, value: Any) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestore.collection("books").document(bookId).update(field, value).await()
                _errorMessage.value = null
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: Exception) {
                _errorMessage.value = R.string.app_error_update
            }
        }
    }

    override fun onCleared() {
        listenerExecutor.execute {
            booksListener?.remove()
            savedBooksListener?.remove()
            listenerExecutor.shutdown()
        }
    }

    private companion object {
        const val TAG = "BookViewModel"
    }
}