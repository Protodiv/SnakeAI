package ua.snakeai.app.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.dp
import moe.tlaster.precompose.navigation.SwipeProperties
import moe.tlaster.precompose.navigation.transition.NavTransition

// Directional swipe transitions
fun SlideNavTransition() = NavTransition(
    createTransition = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
    destroyTransition = fadeOut() + slideOutHorizontally(targetOffsetX = { -it }),
    pauseTransition = fadeOut() + slideOutHorizontally(targetOffsetX = { it / 2 }),
    resumeTransition = fadeIn() + slideInHorizontally(initialOffsetX = { -it / 2 }),
    enterTargetContentZIndex = 1f,
    exitTargetContentZIndex = 0f
)

// Playful bounce transitions
fun BounceNavTransition() = NavTransition(
    createTransition = scaleIn(
        initialScale = 0.8f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    ) + fadeIn(),
    destroyTransition = scaleOut(
        targetScale = 1.2f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
    ) + fadeOut(),
    pauseTransition = fadeOut() + scaleOut(targetScale = 1.1f),
    resumeTransition = fadeIn() + scaleIn(initialScale = 0.9f),
    enterTargetContentZIndex = 1f,
    exitTargetContentZIndex = 0f
)

// Zoom emphasis transitions
fun ZoomNavTransition() = NavTransition(
    createTransition = fadeIn() + scaleIn(initialScale = 0.7f),
    destroyTransition = fadeOut() + scaleOut(targetScale = 1.3f),
    pauseTransition = fadeOut() + scaleOut(targetScale = 1.2f),
    resumeTransition = fadeIn() + scaleIn(initialScale = 0.8f),
    enterTargetContentZIndex = 1f,
    exitTargetContentZIndex = 0f
)

// Balanced default (your current setup)
val DefaultSwipe = SwipeProperties(
    positionalThreshold = { distance -> distance * 0.5f },
    velocityThreshold = { 56.dp.toPx() }
)

// More sensitive (shorter swipe distance, lower velocity)
val EasySwipe = SwipeProperties(
    positionalThreshold = { distance -> distance * 0.25f }, // only 25% of distance
    velocityThreshold = { 32.dp.toPx() } // lower velocity threshold
)

// More strict (requires longer swipe and faster fling)
val FirmSwipe = SwipeProperties(
    positionalThreshold = { distance -> distance * 0.75f }, // 75% of distance
    velocityThreshold = { 96.dp.toPx() } // higher velocity threshold
)

// Natural fling (closer to iOS-style swipe)
val FluidSwipe = SwipeProperties(
    positionalThreshold = { distance -> distance * 0.4f },
    velocityThreshold = { 64.dp.toPx() }
)

// Playful bounce (pairs well with BounceNavTransition)
val BouncySwipe = SwipeProperties(
    positionalThreshold = { distance -> distance * 0.3f },
    velocityThreshold = { 48.dp.toPx() }
)
