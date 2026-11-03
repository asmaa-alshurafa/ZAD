package com.example.zad.feature.bookdetails

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.zad.R
import com.example.zad.model.Book
import com.example.zad.model.BookCondition
import com.example.zad.model.ListingType
import java.util.Locale

@Composable
fun BookDetailsScreen(
    book: Book,
    isSaved: Boolean,
    onBack: () -> Unit,
    onToggleSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val conditionLabel = when (book.condition) {
        BookCondition.LIKE_NEW -> stringResource(R.string.condition_like_new)
        BookCondition.GOOD -> stringResource(R.string.condition_good)
        BookCondition.USED -> stringResource(R.string.condition_used)
        BookCondition.VERY_GOOD -> stringResource(R.string.condition_very_good)
        BookCondition.ACCEPTABLE -> stringResource(R.string.condition_acceptable)
    }
    val listingLabel = stringResource(if (book.listingType == ListingType.SALE) R.string.listing_sale else R.string.listing_donation)

    Column(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onBack) { Text(stringResource(R.string.book_back)) }
            TextButton(onClick = onToggleSaved) {
                Text(stringResource(if (isSaved) R.string.remove_saved else R.string.save_book))
            }
        }
        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (book.listingType == ListingType.SALE) Color(0xFFFEF3C7) else Color(0xFFDCFCE7)
                        ) {
                            Text(
                                listingLabel,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = if (book.listingType == ListingType.SALE) Color(0xFFB45309) else Color(0xFF15803D),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            if (book.listingType == ListingType.SALE) "${String.format(Locale.US, "%.2f", book.price ?: 0.0)} ₪" else stringResource(R.string.free),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(book.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    if (book.academicLevel.isNotBlank()) {
                        Text(book.academicLevel, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(book.major, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(22.dp))
            Text(stringResource(R.string.book_specs), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    DetailRow(stringResource(R.string.major), book.major, stringResource(R.string.academic_year_level), book.academicLevel.ifBlank { stringResource(R.string.unspecified) })
                    DetailRow(stringResource(R.string.book_condition), conditionLabel, stringResource(R.string.listing_type), listingLabel)
                    DetailRow(stringResource(R.string.phone), book.contactPhone, "", "")
                }
            }
            Spacer(Modifier.height(22.dp))
            Text(stringResource(R.string.book_notes), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                text = book.description.ifBlank { stringResource(R.string.no_description) },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
            Button(
                onClick = {
                    val clean = book.contactPhone.replace(Regex("[^0-9]"), "")
                    val message = Uri.encode(context.getString(R.string.whatsapp_message, book.title))
                    val uri = Uri.parse("https://api.whatsapp.com/send?phone=$clean&text=$message")
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri).setPackage("com.whatsapp"))
                    } catch (_: ActivityNotFoundException) {
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) { Text(stringResource(R.string.contact_whatsapp)) }
        }
    }
}

@Composable
private fun DetailRow(firstLabel: String, firstValue: String, secondLabel: String, secondValue: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        DetailItem(firstLabel, firstValue, Modifier.weight(1f))
        if (secondLabel.isNotBlank()) DetailItem(secondLabel, secondValue, Modifier.weight(1f))
    }
}

@Composable
private fun DetailItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
    }
}