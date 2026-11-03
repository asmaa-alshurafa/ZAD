package com.example.zad.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.zad.R
import com.example.zad.model.AuthState
import com.example.zad.viewmodel.AuthViewModel

@Composable
fun SignUpScreen(
    viewModel: AuthViewModel,
    onAuthenticated: () -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authState by viewModel.authState.collectAsState()
    var fullName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var major by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    val loading = authState == AuthState.Loading

    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) onAuthenticated()
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(R.string.signup_title), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.signup_subtitle), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp, bottom = 18.dp))
        SignUpField(R.string.full_name, fullName, { fullName = it })
        SignUpField(R.string.email, email, { email = it }, KeyboardType.Email)
        SignUpField(R.string.major, major, { major = it })
        SignUpField(R.string.whatsapp_number, phone, { phone = it }, KeyboardType.Phone)
        SignUpField(R.string.password, password, { password = it }, KeyboardType.Password)
        if (authState is AuthState.Error) {
            Text(stringResource((authState as AuthState.Error).message), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 10.dp))
        }
        Button(
            onClick = { viewModel.signUp(fullName, email, major, phone, password) },
            enabled = !loading && listOf(fullName, email, major, phone, password).all(String::isNotBlank),
            modifier = Modifier.fillMaxWidth().height(54.dp).padding(top = 12.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (loading) CircularProgressIndicator(modifier = Modifier.height(22.dp), strokeWidth = 2.dp)
            else Text(stringResource(R.string.signup_submit))
        }
        TextButton(onClick = onLogin, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.signup_login_prompt))
        }
    }
}

@Composable
private fun SignUpField(
    labelRes: Int,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(stringResource(labelRes)) },
        modifier = Modifier.fillMaxWidth().padding(top = 9.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp)
    )
}