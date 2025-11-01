package com.example.lingora_fe.core.ui.layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Window size classes for responsive design
 */
enum class WindowSizeClass {
    Small,      // Small phones (default < 600dp)
    Medium,     // Medium phones and small tablets (600dp - 840dp)
    Large       // Tablets and large screens (> 840dp)
}

/**
 * Device orientation
 */
enum class DeviceOrientation {
    Portrait,
    Landscape
}

/**
 * Window size information
 */
data class WindowSize(
    val width: WindowSizeClass,
    val height: WindowSizeClass,
    val orientation: DeviceOrientation
) {
    val isSmallPhone: Boolean
        get() = width == WindowSizeClass.Small && height == WindowSizeClass.Small

    val isMediumPhone: Boolean
        get() = width == WindowSizeClass.Medium && height == WindowSizeClass.Small ||
                width == WindowSizeClass.Small && height == WindowSizeClass.Medium

    val isTablet: Boolean
        get() = width == WindowSizeClass.Large || height == WindowSizeClass.Large

    val isPortrait: Boolean
        get() = orientation == DeviceOrientation.Portrait

    val isLandscape: Boolean
        get() = orientation == DeviceOrientation.Landscape
}

/**
 * Responsive dimensions based on window size
 */
data class ResponsiveDimensions(
    val windowSize: WindowSize,
    // Padding
    val paddingSmall: Dp,
    val paddingMedium: Dp,
    val paddingLarge: Dp,
    val paddingExtraLarge: Dp,
    // Spacing
    val spacingSmall: Dp,
    val spacingMedium: Dp,
    val spacingLarge: Dp,
    val spacingExtraLarge: Dp,
    // Card
    val cardPadding: Dp,
    val cardElevation: Dp,
    val cardCornerRadius: Dp,
    // Button
    val buttonHeight: Dp,
    val buttonCornerRadius: Dp,
    // Icon sizes
    val iconSmall: Dp,
    val iconMedium: Dp,
    val iconLarge: Dp,
    val iconExtraLarge: Dp
) {
    companion object {
        @Composable
        fun current(): ResponsiveDimensions {
            val windowSize = rememberWindowSize()
            return remember(windowSize) {
                when {
                    windowSize.isSmallPhone -> smallPhoneDimensions(windowSize)
                    windowSize.isMediumPhone -> mediumPhoneDimensions(windowSize)
                    windowSize.isTablet -> tabletDimensions(windowSize)
                    else -> smallPhoneDimensions(windowSize)
                }
            }
        }

        private fun smallPhoneDimensions(windowSize: WindowSize): ResponsiveDimensions {
            return ResponsiveDimensions(
                windowSize = windowSize,
                paddingSmall = 8.dp,
                paddingMedium = 16.dp,
                paddingLarge = 24.dp,
                paddingExtraLarge = 32.dp,
                spacingSmall = 4.dp,
                spacingMedium = 8.dp,
                spacingLarge = 16.dp,
                spacingExtraLarge = 24.dp,
                cardPadding = 16.dp,
                cardElevation = 2.dp,
                cardCornerRadius = 12.dp,
                buttonHeight = 48.dp,
                buttonCornerRadius = 12.dp,
                iconSmall = 16.dp,
                iconMedium = 24.dp,
                iconLarge = 32.dp,
                iconExtraLarge = 48.dp
            )
        }

        private fun mediumPhoneDimensions(windowSize: WindowSize): ResponsiveDimensions {
            return ResponsiveDimensions(
                windowSize = windowSize,
                paddingSmall = 12.dp,
                paddingMedium = 20.dp,
                paddingLarge = 28.dp,
                paddingExtraLarge = 40.dp,
                spacingSmall = 6.dp,
                spacingMedium = 12.dp,
                spacingLarge = 20.dp,
                spacingExtraLarge = 32.dp,
                cardPadding = 20.dp,
                cardElevation = 3.dp,
                cardCornerRadius = 16.dp,
                buttonHeight = 52.dp,
                buttonCornerRadius = 14.dp,
                iconSmall = 20.dp,
                iconMedium = 28.dp,
                iconLarge = 40.dp,
                iconExtraLarge = 56.dp
            )
        }

        private fun tabletDimensions(windowSize: WindowSize): ResponsiveDimensions {
            return ResponsiveDimensions(
                windowSize = windowSize,
                paddingSmall = 16.dp,
                paddingMedium = 24.dp,
                paddingLarge = 32.dp,
                paddingExtraLarge = 48.dp,
                spacingSmall = 8.dp,
                spacingMedium = 16.dp,
                spacingLarge = 24.dp,
                spacingExtraLarge = 40.dp,
                cardPadding = 24.dp,
                cardElevation = 4.dp,
                cardCornerRadius = 20.dp,
                buttonHeight = 56.dp,
                buttonCornerRadius = 16.dp,
                iconSmall = 24.dp,
                iconMedium = 32.dp,
                iconLarge = 48.dp,
                iconExtraLarge = 64.dp
            )
        }
    }
}

/**
 * Calculate window size class based on width in dp
 */
private fun getWindowSizeClass(widthDp: Float): WindowSizeClass {
    return when {
        widthDp < 600f -> WindowSizeClass.Small
        widthDp < 840f -> WindowSizeClass.Medium
        else -> WindowSizeClass.Large
    }
}

/**
 * Calculate device orientation
 */
private fun getDeviceOrientation(widthDp: Float, heightDp: Float): DeviceOrientation {
    return if (heightDp > widthDp) DeviceOrientation.Portrait else DeviceOrientation.Landscape
}

/**
 * Remember current window size
 */
@Composable
fun rememberWindowSize(): WindowSize {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp.toFloat()
    val heightDp = configuration.screenHeightDp.toFloat()

    return remember(configuration) {
        WindowSize(
            width = getWindowSizeClass(widthDp),
            height = getWindowSizeClass(heightDp),
            orientation = getDeviceOrientation(widthDp, heightDp)
        )
    }
}

/**
 * CompositionLocal for WindowSize
 */
val LocalWindowSize = compositionLocalOf<WindowSize> {
    error("No WindowSize provided")
}

/**
 * CompositionLocal for ResponsiveDimensions
 */
val LocalResponsiveDimensions = compositionLocalOf<ResponsiveDimensions> {
    error("No ResponsiveDimensions provided")
}

/**
 * Provider for responsive layout
 */
@Composable
fun ResponsiveLayout(
    content: @Composable () -> Unit
) {
    val windowSize = rememberWindowSize()
    val dimensions = ResponsiveDimensions.current()

    CompositionLocalProvider(
        LocalWindowSize provides windowSize,
        LocalResponsiveDimensions provides dimensions
    ) {
        content()
    }
}

