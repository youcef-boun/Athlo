package com.youcef_bounaas.athlo.Authentication.presentation


import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.youcef_bounaas.athlo.ui.theme.AthloTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.youcef_bounaas.athlo.Navigation.presentation.NavDestination

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.youcef_bounaas.athlo.R


@Composable
fun AuthScreen(navController: NavController) {
    // Define onboarding slides data
    val onboardingSlides = listOf(
        OnboardingSlide(
            imageRes = R.drawable.a, // Replace with your actual drawable resources
            title = "Track your active life in one place.",
            subtitle = "Connect and compete with friends"
        ),
        OnboardingSlide(
            imageRes = R.drawable.b,
            title = "Make progress toward goals.",
            subtitle = "Set and achieve your fitness milestones"
        ),
        OnboardingSlide(
            imageRes = R.drawable.c,
            title = "Connect with your community.",
            subtitle = "Sh" +
                    "are activities and get motivated"
        )
    )

    val pagerState = rememberPagerState(pageCount = { onboardingSlides.size })

    Column(
        modifier = Modifier.fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(4f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Horizontal Pager for sliding between onboarding screens
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    OnboardingSlideContent(slide = onboardingSlides[page])
                }

                // Page indicators
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(onboardingSlides.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 12.dp else 8.dp)
                                .background(
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                                    },
                                    shape = CircleShape
                                )
                                .animateContentSize()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate(NavDestination.SignUp.route) },
            ) {
                Text("Join for free")
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                onClick = {
                    navController.navigate(NavDestination.Login.route)
                },
            ) {
                Text("Log In")
            }
        }
    }
}

@Composable
fun OnboardingSlideContent(slide: OnboardingSlide) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background image
        Image(
            painter = painterResource(id = slide.imageRes),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Dark overlay for better text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )

        // Text content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = slide.title,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )

            if (slide.subtitle.isNotEmpty()) {
                Text(
                    text = slide.subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

data class OnboardingSlide(
    val imageRes: Int,
    val title: String,
    val subtitle: String = ""
)










@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    AthloTheme {
        AuthScreen(navController = NavController(LocalContext.current))

    }
}