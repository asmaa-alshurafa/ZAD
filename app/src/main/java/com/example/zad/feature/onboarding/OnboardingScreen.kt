package com.example.zad.feature.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.zad.R
import kotlinx.coroutines.launch

private data class OnboardingPage(val titleRes: Int, val bodyRes: Int, val icon: ImageVector)

private val pages = listOf(
    OnboardingPage(R.string.onboarding_title_1, R.string.onboarding_body_1, Icons.AutoMirrored.Filled.MenuBook),
    OnboardingPage(R.string.onboarding_title_2, R.string.onboarding_body_2, Icons.Default.VolunteerActivism),
    OnboardingPage(R.string.onboarding_title_3, R.string.onboarding_body_3, Icons.Default.Forum)
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onSkip: () -> Unit, onComplete: () -> Unit, modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(initialPage = 0) { pages.size }
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
                TextButton(onClick = onSkip) { Text(stringResource(R.string.onboarding_skip)) }
            }
        }
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { pageIndex ->
            val page = pages[pageIndex]
            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(280.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Brush.linearGradient(listOf(Color(0xFF0F766E), Color(0xFF14B8A6)))),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(184.dp).shadow(28.dp, CircleShape, ambientColor = Color(0xAA14B8A6), spotColor = Color(0xAA14B8A6)),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.18f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Surface(modifier = Modifier.size(132.dp), shape = CircleShape, color = Color.White.copy(alpha = 0.16f)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(page.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(76.dp))
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(26.dp))
                Text(stringResource(page.titleRes), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                Spacer(Modifier.height(14.dp))
                Text(stringResource(page.bodyRes), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 22.dp), horizontalArrangement = Arrangement.Center) {
            pages.indices.forEach { index ->
                val selected = pagerState.currentPage == index
                val indicatorWidth by animateDpAsState(
                    targetValue = if (selected) 28.dp else 8.dp,
                    animationSpec = spring(stiffness = 500f),
                    label = "onboarding-indicator-width"
                )
                Surface(
                    modifier = Modifier.padding(horizontal = 4.dp).width(indicatorWidth).height(8.dp),
                    shape = RoundedCornerShape(50),
                    color = if (selected) Color(0xFF0F766E) else MaterialTheme.colorScheme.outlineVariant
                ) {}
            }
        }
        Button(
            onClick = {
                if (!pagerState.isScrollInProgress) {
                    if (pagerState.currentPage == pages.lastIndex) onComplete()
                    else scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                }
            },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E))
        ) {
            Text(stringResource(if (pagerState.currentPage == pages.lastIndex) R.string.onboarding_start else R.string.onboarding_next))
        }
    }
}