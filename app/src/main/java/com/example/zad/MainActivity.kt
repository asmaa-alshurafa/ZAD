package com.example.zad

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.zad.feature.addbook.AddBookScreen
import com.example.zad.feature.auth.LoginScreen
import com.example.zad.feature.auth.SignUpScreen
import com.example.zad.feature.bookdetails.BookDetailsScreen
import com.example.zad.feature.home.HomeScreen
import com.example.zad.feature.library.MyListingsScreen
import com.example.zad.feature.library.SavedScreen
import com.example.zad.feature.onboarding.OnboardingScreen
import com.example.zad.feature.profile.ProfileScreen
import com.example.zad.feature.splash.SplashScreen
import com.example.zad.model.AuthState
import com.example.zad.model.Book
import com.example.zad.ui.theme.ZADTheme
import com.example.zad.viewmodel.AuthViewModel
import com.example.zad.viewmodel.BookViewModel
import com.example.zad.viewmodel.NotificationViewModel
import java.util.Locale

private enum class AppRoute { SPLASH, ONBOARDING, LOGIN, SIGN_UP, MAIN, DETAILS, ADD_BOOK }

private enum class AppTab(val labelRes: Int) {
    HOME(R.string.tab_home),
    MY_LISTINGS(R.string.tab_listings),
    SAVED(R.string.tab_saved),
    PROFILE(R.string.tab_profile)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val preferences = remember {
                getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
            }
            var currentLanguage by remember {
                mutableStateOf(preferences.getString(PREFERENCE_LANGUAGE, "ar") ?: "ar")
            }
            val localizedContext = remember(currentLanguage) {
                val configuration = Configuration(resources.configuration).apply {
                    setLocale(Locale.forLanguageTag(currentLanguage))
                    setLayoutDirection(Locale.forLanguageTag(currentLanguage))
                }
                createConfigurationContext(configuration)
            }
            val layoutDirection = if (currentLanguage == "ar") LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(
                LocalContext provides localizedContext,
                LocalConfiguration provides localizedContext.resources.configuration,
                LocalLayoutDirection provides layoutDirection
            ) {
                var darkModeEnabled by rememberSaveable { mutableStateOf(false) }
                ZADTheme(darkTheme = darkModeEnabled) {
                    val authViewModel: AuthViewModel = viewModel()
                    val bookViewModel: BookViewModel = viewModel()
                    val notificationViewModel: NotificationViewModel = viewModel()
                    val authState by authViewModel.authState.collectAsState()
                    val currentUser by authViewModel.currentUser.collectAsState()
                    val sessionResolved by authViewModel.sessionResolved.collectAsState()
                    val hasCurrentSession by authViewModel.hasCurrentSession.collectAsState()
                    val allBooks by bookViewModel.books.collectAsState()
                    val savedIds by bookViewModel.savedBookIds.collectAsState()
                    val bookError by bookViewModel.errorMessage.collectAsState()
                    val bookSuccess by bookViewModel.successMessage.collectAsState()
                    val notifications by notificationViewModel.notifications.collectAsState()
                    val notificationError by notificationViewModel.errorMessage.collectAsState()
                    val snackbarHostState = remember { SnackbarHostState() }
                    var route by remember { mutableStateOf(AppRoute.SPLASH) }
                    var selectedTab by remember { mutableStateOf(AppTab.HOME) }
                    var selectedBook by remember { mutableStateOf<Book?>(null) }
                    var splashFinished by remember { mutableStateOf(false) }
                    val bookErrorText = bookError?.let { stringResource(it) }
                    val bookSuccessText = bookSuccess?.let { stringResource(it) }
                    val notificationErrorText = notificationError?.let { stringResource(it) }

                    LaunchedEffect(splashFinished, sessionResolved, hasCurrentSession, authState) {
                        if (splashFinished && sessionResolved) {
                            route = when {
                                hasCurrentSession || authState == AuthState.Guest -> AppRoute.MAIN
                                preferences.getBoolean(PREFERENCE_ONBOARDING_COMPLETE, false) -> AppRoute.LOGIN
                                else -> AppRoute.ONBOARDING
                            }
                        }
                    }

                    LaunchedEffect(authState) {
                        when (val state = authState) {
                            is AuthState.Authenticated -> bookViewModel.setCurrentUser(state.user.uid)
                            AuthState.Guest -> bookViewModel.setCurrentUser("guest")
                            AuthState.Idle -> bookViewModel.setCurrentUser(null)
                            else -> Unit
                        }
                    }
                    LaunchedEffect(currentUser?.uid, authState) {
                        notificationViewModel.setCurrentUser(
                            if (authState is AuthState.Authenticated) currentUser?.uid else null
                        )
                    }
                    LaunchedEffect(bookErrorText) {
                        bookErrorText?.let {
                            snackbarHostState.showSnackbar(it)
                            bookViewModel.clearError()
                        }
                    }
                    LaunchedEffect(bookSuccessText) {
                        bookSuccessText?.let {
                            snackbarHostState.showSnackbar(it)
                            bookViewModel.clearSuccessMessage()
                        }
                    }
                    LaunchedEffect(notificationErrorText) {
                        notificationErrorText?.let {
                            snackbarHostState.showSnackbar(it)
                            notificationViewModel.clearError()
                        }
                    }

                    val canNavigateBack = route in setOf(
                        AppRoute.ONBOARDING, AppRoute.LOGIN, AppRoute.SIGN_UP,
                        AppRoute.DETAILS, AppRoute.ADD_BOOK
                    ) || (route == AppRoute.MAIN && selectedTab != AppTab.HOME)
                    BackHandler(enabled = canNavigateBack) {
                        when (route) {
                            AppRoute.ONBOARDING -> route = AppRoute.LOGIN
                            AppRoute.LOGIN -> route = AppRoute.ONBOARDING
                            AppRoute.SIGN_UP -> route = AppRoute.LOGIN
                            AppRoute.DETAILS -> { selectedBook = null; route = AppRoute.MAIN }
                            AppRoute.ADD_BOOK -> route = AppRoute.MAIN
                            AppRoute.MAIN -> selectedTab = AppTab.HOME
                            AppRoute.SPLASH -> Unit
                        }
                    }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        bottomBar = {
                            if (route == AppRoute.MAIN) {
                                NavigationBar {
                                    AppTab.entries.forEach { tab ->
                                        val label = stringResource(tab.labelRes)
                                        NavigationBarItem(
                                            selected = selectedTab == tab,
                                            onClick = { selectedTab = tab },
                                            icon = {
                                                Icon(
                                                    imageVector = when (tab) {
                                                        AppTab.HOME -> Icons.Default.Home
                                                        AppTab.MY_LISTINGS -> Icons.AutoMirrored.Filled.List
                                                        AppTab.SAVED -> Icons.Default.Favorite
                                                        AppTab.PROFILE -> Icons.Default.Person
                                                    },
                                                    contentDescription = label
                                                )
                                            },
                                            label = { Text(label) }
                                        )
                                    }
                                }
                            }
                        },
                        floatingActionButton = {
                            if (route == AppRoute.MAIN && selectedTab == AppTab.HOME) {
                                ExtendedFloatingActionButton(onClick = { route = AppRoute.ADD_BOOK }) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Text(stringResource(R.string.add_book), modifier = Modifier.padding(start = 8.dp))
                                }
                            }
                        }
                    ) { innerPadding ->
                        val contentModifier = Modifier.padding(innerPadding)
                        when (route) {
                            AppRoute.SPLASH -> SplashScreen(
                                onSplashFinished = { splashFinished = true },
                                modifier = contentModifier
                            )
                            AppRoute.ONBOARDING -> OnboardingScreen(
                                onSkip = {
                                    preferences.edit().putBoolean(PREFERENCE_ONBOARDING_COMPLETE, true).apply()
                                    route = AppRoute.LOGIN
                                },
                                onComplete = {
                                    preferences.edit().putBoolean(PREFERENCE_ONBOARDING_COMPLETE, true).apply()
                                    route = AppRoute.LOGIN
                                },
                                modifier = contentModifier
                            )
                            AppRoute.LOGIN -> LoginScreen(
                                viewModel = authViewModel,
                                onAuthenticated = { route = AppRoute.MAIN },
                                onGuest = { selectedTab = AppTab.HOME; route = AppRoute.MAIN },
                                onRegister = { route = AppRoute.SIGN_UP },
                                modifier = contentModifier
                            )
                            AppRoute.SIGN_UP -> SignUpScreen(
                                viewModel = authViewModel,
                                onAuthenticated = { route = AppRoute.MAIN },
                                onLogin = { route = AppRoute.LOGIN },
                                modifier = contentModifier
                            )
                            AppRoute.MAIN -> {
                                val uid = currentUser?.uid ?: if (authState == AuthState.Guest) "guest" else ""
                                when (selectedTab) {
                                    AppTab.HOME -> HomeScreen(
                                        viewModel = bookViewModel,
                                        user = currentUser,
                                        notifications = notifications,
                                        onMarkNotificationRead = notificationViewModel::markAsRead,
                                        currentLanguage = currentLanguage,
                                        onLanguageChange = { language ->
                                            currentLanguage = language
                                            preferences.edit().putString(PREFERENCE_LANGUAGE, language).apply()
                                        },
                                        onBookClick = { selectedBook = it; route = AppRoute.DETAILS },
                                        modifier = contentModifier
                                    )
                                    AppTab.MY_LISTINGS -> MyListingsScreen(
                                        books = allBooks.filter { it.ownerUid == uid && uid.isNotBlank() },
                                        savedIds = savedIds,
                                        onBookClick = { selectedBook = it; route = AppRoute.DETAILS },
                                        onToggleSaved = bookViewModel::toggleSaveBook,
                                        onToggleDelivered = bookViewModel::markAsDelivered,
                                        onDelete = bookViewModel::deleteBook,
                                        modifier = contentModifier
                                    )
                                    AppTab.SAVED -> SavedScreen(
                                        books = allBooks.filter { it.id in savedIds },
                                        onBookClick = { selectedBook = it; route = AppRoute.DETAILS },
                                        onRemove = bookViewModel::toggleSaveBook,
                                        modifier = contentModifier
                                    )
                                    AppTab.PROFILE -> ProfileScreen(
                                        user = currentUser,
                                        sharedBookCount = allBooks.count { it.ownerUid == uid && uid.isNotBlank() },
                                        soldBookCount = allBooks.count { it.ownerUid == uid && uid.isNotBlank() && it.isCompleted },
                                        darkModeEnabled = darkModeEnabled,
                                        onDarkModeChange = { darkModeEnabled = it },
                                        currentLanguage = currentLanguage,
                                        onLanguageChange = { language ->
                                            currentLanguage = language
                                            preferences.edit().putString(PREFERENCE_LANGUAGE, language).apply()
                                        },
                                        onSignOut = { authViewModel.signOut(); route = AppRoute.LOGIN },
                                        onSignIn = { route = AppRoute.LOGIN },
                                        modifier = contentModifier
                                    )
                                }
                            }
                            AppRoute.DETAILS -> selectedBook?.let { book ->
                                BookDetailsScreen(
                                    book = book,
                                    isSaved = book.id in savedIds,
                                    onBack = { route = AppRoute.MAIN; selectedBook = null },
                                    onToggleSaved = { bookViewModel.toggleSaveBook(book) },
                                    modifier = contentModifier
                                )
                            }
                            AppRoute.ADD_BOOK -> AddBookScreen(
                                user = currentUser,
                                onCancel = { route = AppRoute.MAIN },
                                onCreateBook = {
                                    bookViewModel.addBook(it)
                                    selectedTab = AppTab.MY_LISTINGS
                                    route = AppRoute.MAIN
                                },
                                modifier = contentModifier
                            )
                        }
                    }
                }
            }
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "zad_preferences"
        const val PREFERENCE_LANGUAGE = "language"
        const val PREFERENCE_ONBOARDING_COMPLETE = "onboarding_complete"
    }
}