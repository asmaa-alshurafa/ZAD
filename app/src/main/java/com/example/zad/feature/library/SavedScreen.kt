package com.example.zad.feature.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.zad.R
import com.example.zad.model.Book
import com.example.zad.ui.components.BookCard

@Composable
fun SavedScreen(
    books: List<Book>,
    onBookClick: (Book) -> Unit,
    onRemove: (Book) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(stringResource(R.string.saved_books), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp))
        if (books.isEmpty()) {
            Text(stringResource(R.string.saved_empty), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(28.dp))
        } else {
            LazyColumn(
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(books, key = Book::id) { book ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        BookCard(book = book, onClick = { onBookClick(book) }, isSaved = true, onToggleSaved = { onRemove(book) })
                        OutlinedButton(onClick = { onRemove(book) }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.remove_saved))
                        }
                    }
                }
            }
        }
    }
}