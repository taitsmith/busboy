package com.taitsmith.busboy.ui.theme

import android.content.Context
import androidx.annotation.AttrRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.android.material.color.MaterialColors
import androidx.appcompat.R as AppCompatR
import com.google.android.material.R as MaterialR

/**
 * Fallback used when a color attribute is missing from the host theme. Deliberately a
 * jarring magenta: every attribute below is defined by both AppTheme.* styles, so seeing
 * this on screen means the Compose content is hosted by something that is not an agency
 * theme, which should be obvious rather than silently subtle.
 */
private const val MISSING_ATTR_COLOR = android.graphics.Color.MAGENTA

private fun Context.themeColor(@AttrRes attr: Int): Color =
    Color(MaterialColors.getColor(this, attr, MISSING_ATTR_COLOR))

/**
 * Makes the agency theme available to Compose.
 *
 * The scheme is read back off the hosting Activity's theme rather than rebuilt from
 * colors.xml. That keeps themes.xml the single source of truth — the mapping of color
 * token to Material role is written once, in XML — so this screen cannot drift from the
 * View-based screens, and it needs no knowledge of which agency is selected: whatever
 * MainActivity applied in setTheme() is what it reads.
 *
 * Light-only, matching Base.AppTheme. If a dark theme is ever added, this needs to switch
 * to darkColorScheme() alongside it.
 */
@Composable
fun BusboyTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = context.themeColor(AppCompatR.attr.colorPrimary),
            onPrimary = context.themeColor(MaterialR.attr.colorOnPrimary),
            primaryContainer = context.themeColor(MaterialR.attr.colorPrimaryContainer),
            onPrimaryContainer = context.themeColor(MaterialR.attr.colorOnPrimaryContainer),
            secondary = context.themeColor(MaterialR.attr.colorSecondary),
            onSecondary = context.themeColor(MaterialR.attr.colorOnSecondary),
            secondaryContainer = context.themeColor(MaterialR.attr.colorSecondaryContainer),
            onSecondaryContainer = context.themeColor(MaterialR.attr.colorOnSecondaryContainer),
            tertiary = context.themeColor(MaterialR.attr.colorTertiary),
            onTertiary = context.themeColor(MaterialR.attr.colorOnTertiary),
            tertiaryContainer = context.themeColor(MaterialR.attr.colorTertiaryContainer),
            onTertiaryContainer = context.themeColor(MaterialR.attr.colorOnTertiaryContainer),
            error = context.themeColor(AppCompatR.attr.colorError),
            onError = context.themeColor(MaterialR.attr.colorOnError),
            errorContainer = context.themeColor(MaterialR.attr.colorErrorContainer),
            onErrorContainer = context.themeColor(MaterialR.attr.colorOnErrorContainer),
            background = context.themeColor(android.R.attr.colorBackground),
            onBackground = context.themeColor(MaterialR.attr.colorOnBackground),
            surface = context.themeColor(MaterialR.attr.colorSurface),
            onSurface = context.themeColor(MaterialR.attr.colorOnSurface),
            surfaceVariant = context.themeColor(MaterialR.attr.colorSurfaceVariant),
            onSurfaceVariant = context.themeColor(MaterialR.attr.colorOnSurfaceVariant),
            outline = context.themeColor(MaterialR.attr.colorOutline),
            inverseOnSurface = context.themeColor(MaterialR.attr.colorOnSurfaceInverse),
        ),
        content = content
    )
}
