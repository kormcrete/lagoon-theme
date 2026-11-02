package io.github.kormcrete.lagoon.compose

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import io.github.kormcrete.lagoon.compose.resources.Res
import io.github.kormcrete.lagoon.compose.resources.inter_bold
import io.github.kormcrete.lagoon.compose.resources.inter_medium
import io.github.kormcrete.lagoon.compose.resources.inter_regular
import io.github.kormcrete.lagoon.compose.resources.inter_semibold
import org.jetbrains.compose.resources.Font

/** The Lagoon palette. Identity: the same on every platform. */
@Immutable
data class LagoonColors(
    val ground: Color,
    val surface: Color,
    val surfaceMuted: Color,
    val track: Color,
    val ink: Color,
    val inkMuted: Color,
    val inkFaint: Color,
    val line: Color,
    val lineSoft: Color,
    val divider: Color,
    val primary: Color,
    val onPrimary: Color,
    val primaryStrong: Color,
    val primaryTint: Color,
    val selection: Color,
    val success: Color,
    val successTint: Color,
    val attention: Color,
    val attentionTint: Color,
    val danger: Color,
    val dangerTint: Color,
    val dangerLine: Color,
    val neutral: Color,
    val neutralTint: Color,
    val night: Color,
    val onNight: Color,
    val onNightMuted: Color,
    val nightAccent: Color,
    val scrim: Color,
    val shadow: Color,
    /** Soft background and ink pairs for avatar and category tiles, picked by [tile]. */
    val tiles: List<Pair<Color, Color>>,
) {
    /** The same key always gets the same tile colours. */
    fun tile(key: String): Pair<Color, Color> = tiles[(key.hashCode() and Int.MAX_VALUE) % tiles.size]

    companion object {
        val Light = with(LagoonTokens.Colors) {
            LagoonColors(
                ground = ground, surface = surface, surfaceMuted = surfaceMuted, track = track,
                ink = ink, inkMuted = inkMuted, inkFaint = inkFaint,
                line = line, lineSoft = lineSoft, divider = divider,
                primary = primary, onPrimary = onPrimary, primaryStrong = primaryStrong, primaryTint = primaryTint,
                selection = selection,
                success = success, successTint = successTint, attention = attention, attentionTint = attentionTint,
                danger = danger, dangerTint = dangerTint, dangerLine = dangerLine,
                neutral = neutral, neutralTint = neutralTint,
                night = night, onNight = onNight, onNightMuted = onNightMuted, nightAccent = nightAccent,
                scrim = scrim, shadow = shadow,
                tiles = LagoonTokens.tiles,
            )
        }
    }
}

/** Sizes that follow the platform: touch on phones, mouse on desktop. */
@Immutable
data class LagoonMetrics(
    val controlHeight: Dp,
    val buttonHeight: Dp,
    val rowHeight: Dp,
    val touchTarget: Dp,
    val gutter: Dp,
    val gap: Dp,
    val titleSize: TextUnit,
    val headlineSize: TextUnit,
    val bodySize: TextUnit,
    val smallSize: TextUnit,
    val labelSize: TextUnit,
) {
    companion object {
        val Mobile: LagoonMetrics get() = LagoonTokens.mobileMetrics
        val Desktop: LagoonMetrics get() = LagoonTokens.desktopMetrics
    }
}

@Immutable
data class LagoonShapes(
    val small: CornerBasedShape,
    val control: CornerBasedShape,
    val card: CornerBasedShape,
    val dialog: CornerBasedShape,
    val pill: CornerBasedShape,
) {
    companion object {
        val Default = LagoonShapes(
            small = RoundedCornerShape(LagoonTokens.Radius.small),
            control = RoundedCornerShape(LagoonTokens.Radius.control),
            card = RoundedCornerShape(LagoonTokens.Radius.card),
            dialog = RoundedCornerShape(LagoonTokens.Radius.dialog),
            pill = RoundedCornerShape(percent = 50),
        )
    }
}

/** Text styles, sized from [LagoonMetrics]: the same scale reads denser on desktop. */
@Immutable
data class LagoonTypography(
    val title: TextStyle,
    val headline: TextStyle,
    val heading: TextStyle,
    val body: TextStyle,
    val bodyStrong: TextStyle,
    val small: TextStyle,
    val label: TextStyle,
    val overline: TextStyle,
    val amount: TextStyle,
)

internal fun lagoonTypography(family: FontFamily, m: LagoonMetrics) = LagoonTypography(
    title = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = m.titleSize, letterSpacing = (-0.02).em),
    headline = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = m.headlineSize, letterSpacing = (-0.02).em),
    heading = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = m.bodySize * 1.08f),
    body = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = m.bodySize),
    bodyStrong = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = m.bodySize),
    small = TextStyle(fontFamily = family, fontWeight = FontWeight.Normal, fontSize = m.smallSize),
    label = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = m.labelSize),
    overline = TextStyle(fontFamily = family, fontWeight = FontWeight.SemiBold, fontSize = m.labelSize, letterSpacing = 0.06.em),
    amount = TextStyle(fontFamily = family, fontWeight = FontWeight.Bold, fontSize = m.titleSize * 1.08f, letterSpacing = (-0.02).em),
)

val LocalLagoonColors = staticCompositionLocalOf { LagoonColors.Light }
val LocalLagoonMetrics = staticCompositionLocalOf { LagoonMetrics.Mobile }
val LocalLagoonShapes = staticCompositionLocalOf { LagoonShapes.Default }
val LocalLagoonTypography = staticCompositionLocalOf<LagoonTypography> { error("No LagoonTheme around this composable") }

/** The platform's default metrics: [LagoonMetrics.Mobile] on Android and iOS, [LagoonMetrics.Desktop] on the JVM. */
expect val platformLagoonMetrics: LagoonMetrics

/** Shortcuts to the current theme values, like `MaterialTheme`. */
object Lagoon {
    val colors: LagoonColors @Composable @ReadOnlyComposable get() = LocalLagoonColors.current
    val metrics: LagoonMetrics @Composable @ReadOnlyComposable get() = LocalLagoonMetrics.current
    val shapes: LagoonShapes @Composable @ReadOnlyComposable get() = LocalLagoonShapes.current
    val type: LagoonTypography @Composable @ReadOnlyComposable get() = LocalLagoonTypography.current
}

@Composable
fun interFontFamily(): FontFamily = FontFamily(
    Font(Res.font.inter_regular, FontWeight.Normal),
    Font(Res.font.inter_medium, FontWeight.Medium),
    Font(Res.font.inter_semibold, FontWeight.SemiBold),
    Font(Res.font.inter_bold, FontWeight.Bold),
)

/**
 * The Lagoon theme: Lagoon colours, Inter and shapes everywhere; [metrics] decide density.
 * Material 3 components inside get a matching colour scheme, type scale and shapes.
 */
@Composable
fun LagoonTheme(
    metrics: LagoonMetrics = platformLagoonMetrics,
    colors: LagoonColors = LagoonColors.Light,
    content: @Composable () -> Unit,
) {
    val family = interFontFamily()
    val type = lagoonTypography(family, metrics)
    val shapes = LagoonShapes.Default
    val scheme = lightColorScheme(
        primary = colors.primary,
        onPrimary = colors.onPrimary,
        primaryContainer = colors.primaryTint,
        onPrimaryContainer = colors.primaryStrong,
        secondary = colors.success,
        secondaryContainer = colors.successTint,
        tertiary = colors.attention,
        tertiaryContainer = colors.attentionTint,
        background = colors.ground,
        onBackground = colors.ink,
        surface = colors.surface,
        onSurface = colors.ink,
        surfaceVariant = colors.surfaceMuted,
        onSurfaceVariant = colors.inkMuted,
        outline = colors.line,
        outlineVariant = colors.divider,
        error = colors.danger,
        errorContainer = colors.dangerTint,
        scrim = colors.scrim,
    )
    val material = Typography(
        headlineLarge = type.title, headlineMedium = type.headline,
        titleLarge = type.headline, titleMedium = type.heading, titleSmall = type.bodyStrong,
        bodyLarge = type.body, bodyMedium = type.body, bodySmall = type.small,
        labelLarge = type.bodyStrong, labelMedium = type.label, labelSmall = type.label,
    )
    CompositionLocalProvider(
        LocalLagoonColors provides colors,
        LocalLagoonMetrics provides metrics,
        LocalLagoonShapes provides shapes,
        LocalLagoonTypography provides type,
    ) {
        MaterialTheme(
            colorScheme = scheme,
            typography = material,
            shapes = Shapes(small = shapes.small, medium = shapes.control, large = shapes.card),
            content = content,
        )
    }
}
