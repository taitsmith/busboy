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
 * Resolves a color from the host theme, throwing if the attribute is absent.
 *
 * Throwing rather than substituting a sentinel is deliberate: every attribute read below is
 * defined by both AppTheme.* styles *and* by Theme.Material3 itself, so a miss means the
 * content is hosted somewhere it was never meant to be. A silent fallback would render a
 * plausible-looking wrong color that nobody traces back to here.
 */
private fun Context.themeColor(@AttrRes attr: Int): Color =
    Color(MaterialColors.getColor(this, attr, "BusboyTheme"))

/**
 * Makes the agency theme available to Compose.
 *
 * The scheme is read back off the hosting Activity's theme rather than rebuilt from
 * colors.xml, so this screen cannot drift from the View-based screens and needs no knowledge
 * of which agency is selected: whatever MainActivity applied in setTheme() is what it reads.
 *
 * Note the scope of that guarantee. Every ColorScheme role that has a corresponding theme
 * attribute is mapped here. The roles Material has no attribute for — `surfaceTint`, `scrim`,
 * and the twelve `*Fixed` roles — are set explicitly or left at lightColorScheme()'s M3
 * baseline; the `*Fixed` set in particular is NOT agency-aware, so map it before using it.
 *
 * Also note that only *colors* come from XML. Typography and shapes still come from Compose
 * defaults, so MaterialTheme.shapes/typography are not agency- or app-themed.
 *
 * Light-only, matching Base.AppTheme. If a dark theme is ever added, this needs to switch to
 * darkColorScheme() alongside it.
 */
@Composable
fun BusboyTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val primary = context.themeColor(AppCompatR.attr.colorPrimary)
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = primary,
            onPrimary = context.themeColor(MaterialR.attr.colorOnPrimary),
            primaryContainer = context.themeColor(MaterialR.attr.colorPrimaryContainer),
            onPrimaryContainer = context.themeColor(MaterialR.attr.colorOnPrimaryContainer),
            inversePrimary = context.themeColor(MaterialR.attr.colorPrimaryInverse),
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
            surfaceBright = context.themeColor(MaterialR.attr.colorSurfaceBright),
            surfaceDim = context.themeColor(MaterialR.attr.colorSurfaceDim),
            surfaceContainer = context.themeColor(MaterialR.attr.colorSurfaceContainer),
            surfaceContainerLow = context.themeColor(MaterialR.attr.colorSurfaceContainerLow),
            surfaceContainerLowest = context.themeColor(MaterialR.attr.colorSurfaceContainerLowest),
            surfaceContainerHigh = context.themeColor(MaterialR.attr.colorSurfaceContainerHigh),
            surfaceContainerHighest = context.themeColor(MaterialR.attr.colorSurfaceContainerHighest),
            inverseSurface = context.themeColor(MaterialR.attr.colorSurfaceInverse),
            inverseOnSurface = context.themeColor(MaterialR.attr.colorOnSurfaceInverse),
            outline = context.themeColor(MaterialR.attr.colorOutline),
            outlineVariant = context.themeColor(MaterialR.attr.colorOutlineVariant),
            //no theme attributes exist for these two. surfaceTint is primary in M3 by
            //definition, and scrim is always black.
            surfaceTint = primary,
            scrim = Color.Black,
        ),
        content = content
    )
}
