package com.example.zad.feature.addbook

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.zad.R
import com.example.zad.model.Book
import com.example.zad.model.BookCondition
import com.example.zad.model.ListingType
import com.example.zad.model.User
import com.example.zad.model.toWesternDigits

@Composable
fun AddBookScreen(
    user: User?,
    onCancel: () -> Unit,
    onCreateBook: (Book) -> Unit,
    modifier: Modifier = Modifier
) {
    var listingType by rememberSaveable { mutableStateOf(ListingType.SALE) }
    var title by rememberSaveable { mutableStateOf("") }
    var major by rememberSaveable { mutableStateOf("") }
    var academicLevel by rememberSaveable { mutableStateOf("") }
    var condition by rememberSaveable { mutableStateOf(BookCondition.GOOD) }
    var price by rememberSaveable { mutableStateOf("") }
    var contactPhone by rememberSaveable(user?.uid) { mutableStateOf(user?.phone.orEmpty()) }
    var attemptedSave by rememberSaveable { mutableStateOf(false) }
    val normalizedPrice = price.toWesternDigits().replace('٫', '.').toDoubleOrNull()
    val validPrice = listingType == ListingType.DONATION || normalizedPrice?.let { it > 0 } == true
    val validContact = contactPhone.filter(Char::isDigit).length >= 8
    val requiredFieldsValid = title.isNotBlank() && major.isNotBlank()

    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onCancel) { Text(stringResource(R.string.cancel)) }
            Text(stringResource(R.string.add_book_title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
            Spacer(Modifier.padding(20.dp))
        }
        Text(stringResource(R.string.add_book_subtitle), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(18.dp))
        Text(stringResource(R.string.listing_type), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp).clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant).padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ListingType.entries.forEach { type ->
                val isSelected = listingType == type
                Surface(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable { listingType = type },
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = stringResource(if (type == ListingType.SALE) R.string.listing_sale else R.string.listing_donation),
                        modifier = Modifier.padding(vertical = 12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        FormField(R.string.book_title, title, { title = it }, attemptedSave && title.isBlank())
        FormField(R.string.academic_major, major, { major = it }, attemptedSave && major.isBlank())
        FormField(R.string.academic_level, academicLevel, { academicLevel = it }, false)
        AnimatedVisibility(
            visible = listingType == ListingType.SALE,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(modifier = Modifier.animateContentSize()) {
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it.filter(Char::isDigitOrDecimal) },
                    label = { Text(stringResource(R.string.price_shekels)) },
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    singleLine = true,
                    isError = attemptedSave && !validPrice,
                    shape = RoundedCornerShape(14.dp)
                )
                if (attemptedSave && !validPrice) {
                    Text(stringResource(R.string.price_invalid), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
        Text(stringResource(R.string.book_condition), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 16.dp))
        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(
                BookCondition.LIKE_NEW to R.string.condition_like_new,
                BookCondition.GOOD to R.string.condition_good,
                BookCondition.USED to R.string.condition_used
            ).forEach { (option, labelRes) ->
                FilterChip(selected = condition == option, onClick = { condition = option }, label = { Text(stringResource(labelRes)) })
            }
        }
        FormField(
            labelRes = R.string.whatsapp_number,
            value = contactPhone,
            onValueChange = { contactPhone = it },
            showError = attemptedSave && !validContact,
            errorRes = R.string.contact_invalid
        )
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = {
                attemptedSave = true
                if (requiredFieldsValid && validPrice && validContact) {
                    onCreateBook(
                        Book(
                            title = title.trim(),
                            major = major.trim(),
                            academicLevel = academicLevel.trim(),
                            price = if (listingType == ListingType.SALE) normalizedPrice else null,
                            listingType = listingType,
                            condition = condition,
                            contactPhone = contactPhone.toWesternDigits().trim()
                        )
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) { Text(stringResource(R.string.post_listing)) }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun FormField(
    labelRes: Int,
    value: String,
    onValueChange: (String) -> Unit,
    showError: Boolean,
    errorRes: Int = R.string.field_required
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(labelRes)) },
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        singleLine = true,
        isError = showError,
        shape = RoundedCornerShape(14.dp)
    )
    if (showError) Text(stringResource(errorRes), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
}

private fun Char.isDigitOrDecimal(): Boolean = isDigit() || this == '.' || this == '٫'