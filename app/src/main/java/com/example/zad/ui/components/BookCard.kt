package com.example.zad.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.zad.R
import com.example.zad.model.Book
import com.example.zad.model.BookCondition
import com.example.zad.model.ListingType
import com.example.zad.ui.theme.DonationBadgeBackground
import com.example.zad.ui.theme.DonationBadgeText
import com.example.zad.ui.theme.SaleBadgeBackground
import com.example.zad.ui.theme.SaleBadgeText
import java.util.Locale

@Composable
fun BookCard(
    book: Book,
    onClick: () -> Unit,
    isSaved: Boolean = false,
    onToggleSaved: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isSale = book.listingType == ListingType.SALE
    val conditionLabel = when (book.condition) {
        BookCondition.LIKE_NEW -> stringResource(R.string.condition_like_new)
        BookCondition.GOOD -> stringResource(R.string.condition_good)
        BookCondition.USED -> stringResource(R.string.condition_used)
        BookCondition.VERY_GOOD -> stringResource(R.string.condition_very_good)
        BookCondition.ACCEPTABLE -> stringResource(R.string.condition_acceptable)
    }
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (isSale) SaleBadgeBackground else DonationBadgeBackground
                ) {
                    Text(
                        text = if (isSale) {
                            "${stringResource(R.string.listing_sale)} · ${String.format(Locale.US, "%.2f", book.price ?: 0.0)} ₪"
                        } else stringResource(R.string.listing_donation),
                        color = if (isSale) SaleBadgeText else DonationBadgeText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
                IconButton(onClick = onToggleSaved) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = stringResource(if (isSaved) R.string.remove_saved else R.string.save_book),
                        tint = if (isSaved) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(text = book.major, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                if (book.academicLevel.isNotBlank()) {
                    Text(text = book.academicLevel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = "•", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(text = conditionLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}