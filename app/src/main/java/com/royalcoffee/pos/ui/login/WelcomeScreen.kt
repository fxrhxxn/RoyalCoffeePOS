package com.royalcoffee.pos.ui.login

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.royalcoffee.pos.ui.theme.CinzelFontFamily
import com.royalcoffee.pos.ui.theme.RoyalGold
import com.royalcoffee.pos.ui.theme.RoyalGreen
import com.royalcoffee.pos.ui.theme.RoyalGreenDark

/**
 * First screen displayed when Royal Coffee POS is opened
 * for the very first time.
 *
 * This screen is only shown when there are no users
 * registered in the local Room database.
 */
@Composable
fun WelcomeScreen(
    onGetStarted: () -> Unit
) {
    /*
     * These values are animated when the screen first appears.
     *
     * alpha:
     *   Controls transparency.
     *
     * scale:
     *   Controls the size of the content.
     *
     * Starting slightly transparent and smaller gives the page
     * a smooth entrance animation.
     */
    val contentAlpha = remember {
        Animatable(0f)
    }

    val contentScale = remember {
        Animatable(0.94f)
    }

    /*
     * LaunchedEffect runs when this screen first enters composition.
     */
    LaunchedEffect(Unit) {
        contentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 700,
                easing = FastOutSlowInEasing
            )
        )
    }

    LaunchedEffect(Unit) {
        contentScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 700,
                easing = FastOutSlowInEasing
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        RoyalGreenDark,
                        RoyalGreen
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 650.dp)
                .padding(horizontal = 48.dp)
                .alpha(contentAlpha.value)
                .scale(contentScale.value),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            /*
             * Decorative coffee icon.
             */
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = RoyalGold.copy(alpha = 0.15f)
            ) {
                Box(
                    modifier = Modifier.padding(28.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Coffee,
                        contentDescription = "Royal Coffee",
                        tint = RoyalGold,
                        modifier = Modifier.height(72.dp)
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            Text(
                text = "Welcome",
                style = MaterialTheme.typography.displayLarge,
                fontFamily = CinzelFontFamily,
                fontWeight = FontWeight.Bold,
                color = RoyalGold,
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = "Royal Coffee POS",
                style = MaterialTheme.typography.headlineMedium,
                fontFamily = CinzelFontFamily,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Text(
                text = "Set up your business and create your first administrator account to begin using the point-of-sale system.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(
                    alpha = 0.85f
                ),
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(36.dp)
            )

            Button(
                onClick = onGetStarted,
                modifier = Modifier
                    .height(56.dp)
                    .widthIn(min = 220.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RoyalGold,
                    contentColor = RoyalGreenDark
                )
            ) {
                Text(
                    text = "Get Started",
                    fontWeight = FontWeight.Bold
                )

                Spacer(
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Continue"
                )
            }
        }
    }
}