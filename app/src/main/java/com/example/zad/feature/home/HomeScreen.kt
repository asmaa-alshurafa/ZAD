package com.example.zad.feature.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.zad.R
import com.example.zad.model.AppNotification
import com.example.zad.model.Book
import com.example.zad.model.ListingType
import com.example.zad.model.User
import com.example.zad.ui.components.BookCard
import com.example.zad.viewmodel.BookViewModel

@Composable
fun HomeScreen(
    viewModel: BookViewModel,
    user: User?,
    notifications: List<AppNotification>,
    onMarkNotificationRead: (String) -> Unit,
    currentLanguage: String,
    onLanguageChange: (String) -> Unit,
    onBookClick: (Book) -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val books by viewModel.books.collectAsState()
    val savedBookIds by viewModel.savedBookIds.collectAsState()
    var showNotifications by rememberSaveable { mutableStateOf(false) }
    val unreadCount = notifications.count { !it.read }
    val filteredBooks = books.filter { book ->
        (searchQuery.isBlank() || listOf(book.title, book.major, book.academicLevel)
            .any { it.contains(searchQuery.trim(), ignoreCase = true) }) &&
            (selectedFilter == null || book.listingType == selectedFilter)
    }

    Column(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 20.dp).padding(top = 14.dp, bottom = 10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(stringResource(R.string.brand_name), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    val firstName = user?.fullName?.trim()?.substringBefore(' ')?.takeIf(String::isNotBlank)
                    Text(
                        stringResource(R.string.home_greeting, firstName ?: stringResource(R.string.home_greeting_student)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { onLanguageChange(if (currentLanguage == "ar") "en" else "ar") }) {
                        Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(stringResource(if (currentLanguage == "ar") R.string.language_english else R.string.language_arabic))
                    }
                    IconButton(onClick = { showNotifications = true }) {
                        BadgedBox(badge = {
                            if (unreadCount > 0) {
                                Badge { Text(if (unreadCount > 99) "99+" else unreadCount.toString()) }
                            }
                        }) {
                            Icon(
                                Icons.Outlined.NotificationsNone,
                                contentDescription = stringResource(R.string.notifications),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Search, contentDescription = null, tint = Color(0xFF0F766E))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged,
                        modifier = Modifier.weight(1f).padding(horizontal = 12.dp, vertical = 16.dp),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (searchQuery.isEmpty()) {
                                    Text(stringResource(R.string.search_hint), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                                }
                                innerTextField()
                            }
                        }
                    )
                    AnimatedVisibility(
                        visible = searchQuery.isNotEmpty(),
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.clear_search), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = selectedFilter == null, onClick = { viewModel.onFilterSelected(null) }, label = { Text(stringResource(R.string.filter_all)) })
                FilterChip(selected = selectedFilter == ListingType.SALE, onClick = { viewModel.onFilterSelected(ListingType.SALE) }, label = { Text(stringResource(R.string.listing_sale)) })
                FilterChip(selected = selectedFilter == ListingType.DONATION, onClick = { viewModel.onFilterSelected(ListingType.DONATION) }, label = { Text(stringResource(R.string.listing_donation)) })
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.discover_books), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(pluralStringResource(R.plurals.book_count, filteredBooks.size, filteredBooks.size), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (filteredBooks.isEmpty()) {
            Column(modifier = Modifier.fillMaxWidth().padding(28.dp)) {
                Text(stringResource(R.string.no_books_found), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.search_try_again), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                TextButton(onClick = viewModel::clearSearchAndFilter) { Text(stringResource(R.string.clear_filters)) }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredBooks, key = Book::id) { book ->
                    BookCard(
                        book = book,
                        onClick = { onBookClick(book) },
                        isSaved = book.id in savedBookIds,
                        onToggleSaved = { viewModel.toggleSaveBook(book.id) }
                    )
                }
            }
        }
    }
    if (showNotifications) {
        AlertDialog(
            onDismissRequest = { showNotifications = false },
            title = { Text(stringResource(R.string.notifications)) },
            text = {
                if (notifications.isEmpty()) {
                    Text(stringResource(R.string.notifications_empty))
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(notifications, key = AppNotification::id) { notification ->
                            Column {
                                Text(
                                    if (currentLanguage == "ar") notification.titleAr else notification.titleEn,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    if (currentLanguage == "ar") notification.bodyAr else notification.bodyEn,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (!notification.read) {
                                    TextButton(onClick = { onMarkNotificationRead(notification.id) }) {
                                        Text(stringResource(R.string.notification_mark_read))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNotifications = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}