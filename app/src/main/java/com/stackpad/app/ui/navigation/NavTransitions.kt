package com.stackpad.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideIntoContainer
import androidx.compose.animation.slideOutOfContainer
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry

/** Screen changes: the new screen slides in a short way from the side while fading; going back reverses it. */
private const val SCREEN_MILLIS = 300
private const val SLIDE_DIVISOR = 5

private fun slideSpec() = tween<IntOffset>(SCREEN_MILLIS, easing = FastOutSlowInEasing)

internal val screenEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(SlideDirection.Start, slideSpec(), initialOffset = { it / SLIDE_DIVISOR }) +
        fadeIn(tween(SCREEN_MILLIS))
}

internal val screenExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(SlideDirection.Start, slideSpec(), targetOffset = { it / SLIDE_DIVISOR }) +
        fadeOut(tween(SCREEN_MILLIS / 2))
}

internal val screenPopEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(SlideDirection.End, slideSpec(), initialOffset = { it / SLIDE_DIVISOR }) +
        fadeIn(tween(SCREEN_MILLIS))
}

internal val screenPopExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(SlideDirection.End, slideSpec(), targetOffset = { it / SLIDE_DIVISOR }) +
        fadeOut(tween(SCREEN_MILLIS / 2))
}
