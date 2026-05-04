package com.vaultapp.ui.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.vaultapp.data.model.AppTheme
data class VaultColors(
    val background: Color, val surface: Color, val surfaceVariant: Color,
    val primary: Color, val primaryContainer: Color, val onPrimary: Color,
    val onBackground: Color, val onSurface: Color, val onSurfaceVariant: Color, val outline: Color,
    val noteCard1: Color, val noteCard2: Color, val noteCard3: Color, val noteCard4: Color,
    val noteCard5: Color, val noteCard6: Color, val noteCard7: Color, val noteCard8: Color
)
val LocalVaultColors = staticCompositionLocalOf { midnight() }
val MaterialTheme.vaultColors: VaultColors
    @Composable get() = LocalVaultColors.current
@Composable
fun VaultTheme(appTheme: AppTheme = AppTheme.MIDNIGHT, content: @Composable () -> Unit) {
    val isDarkMode = isSystemInDarkTheme()
    val vc = appTheme.toVaultColors(isDarkMode)
    val cs = if (isDarkMode) {
        darkColorScheme(
            primary = vc.primary,
            primaryContainer = vc.primaryContainer,
            background = vc.background,
            surface = vc.surface,
            surfaceVariant = vc.surfaceVariant,
            onPrimary = vc.onPrimary,
            onBackground = vc.onBackground,
            onSurface = vc.onSurface,
            onSurfaceVariant = vc.onSurfaceVariant,
            outline = vc.outline
        )
    } else {
        lightColorScheme(
            primary = vc.primary,
            primaryContainer = vc.primaryContainer,
            background = vc.background,
            surface = vc.surface,
            surfaceVariant = vc.surfaceVariant,
            onPrimary = vc.onPrimary,
            onBackground = vc.onBackground,
            onSurface = vc.onSurface,
            onSurfaceVariant = vc.onSurfaceVariant,
            outline = vc.outline
        )
    }
    CompositionLocalProvider(LocalVaultColors provides vc) {
        MaterialTheme(
            colorScheme = cs,
            typography = VaultTypography,
            content = content
        )
    }
}
fun AppTheme.toVaultColors(isDarkMode: Boolean = false) = if (isDarkMode) toDarkVaultColors() else when (this) {
    AppTheme.MIDNIGHT -> midnight()
    AppTheme.CLOUD    -> cloud()
    AppTheme.FOREST   -> forest()
    AppTheme.ROSE     -> rose()
    AppTheme.OCEAN    -> ocean()
    AppTheme.AMBER    -> amber()
    AppTheme.VIOLET   -> violet()
    AppTheme.ABYSS    -> abyss()
    AppTheme.MONO     -> mono()
    AppTheme.SUNSET   -> sunset()
    AppTheme.CHERRY   -> cherry()
    AppTheme.ARCTIC   -> arctic()
}
private fun AppTheme.toDarkVaultColors() = VaultColors(
    background = c(backgroundHex),
    surface = c(surfaceHex),
    surfaceVariant = lighten(c(surfaceHex), 0.12f),
    primary = c(primaryHex),
    primaryContainer = lighten(c(primaryHex), 0.18f),
    onPrimary = Color.White,
    onBackground = c("#F5F5F7"),
    onSurface = c("#ECECF1"),
    onSurfaceVariant = c("#B2B2C0"),
    outline = c("#3A3A4A"),
    noteCard1 = lighten(c(surfaceHex), 0.05f),
    noteCard2 = lighten(c(surfaceHex), 0.08f),
    noteCard3 = lighten(c(surfaceHex), 0.11f),
    noteCard4 = lighten(c(surfaceHex), 0.14f),
    noteCard5 = lighten(c(surfaceHex), 0.17f),
    noteCard6 = lighten(c(surfaceHex), 0.20f),
    noteCard7 = lighten(c(surfaceHex), 0.23f),
    noteCard8 = lighten(c(surfaceHex), 0.26f)
)
private fun lighten(color: Color, amount: Float): Color = Color(
    red = color.red + (1f - color.red) * amount,
    green = color.green + (1f - color.green) * amount,
    blue = color.blue + (1f - color.blue) * amount,
    alpha = color.alpha
)
private fun c(hex: String) = Color(android.graphics.Color.parseColor(hex))
fun midnight() = VaultColors(
    c("#F7F5FF"), c("#FFFFFF"), c("#EFEAFE"),
    c("#7C6AF5"), c("#E6E0FF"), Color.White,
    c("#1A1A24"), c("#2A2A38"), c("#6A6A80"), c("#CFCFE3"),
    c("#EEE9FF"), c("#E6F7F1"), c("#FDEAF1"), c("#FFF4E5"),
    c("#E8F2FF"), c("#EDF8E7"), c("#FDECEC"), c("#F1EEFF")
)
fun cloud() = VaultColors(
    c("#FAFAFF"), c("#FFFFFF"), c("#F3F4FA"),
    c("#6B5CE7"), c("#EAE7FF"), Color.White,
    c("#1C1C28"), c("#2C2C3A"), c("#70708A"), c("#D6D8E5"),
    c("#EFEAFF"), c("#E8F7F0"), c("#FFECEF"), c("#FFF6E8"),
    c("#EAF4FF"), c("#EDF8EE"), c("#FFF0F0"), c("#F5EEFF")
)
fun forest() = VaultColors(
    c("#F4FBF7"), c("#FFFFFF"), c("#E8F5EC"),
    c("#1D9E75"), c("#D8F3E8"), Color.White,
    c("#16241D"), c("#24342B"), c("#5F7A6C"), c("#C7DDD2"),
    c("#DDF4EA"), c("#EAF8E8"), c("#F3F2E4"), c("#EEF6E5"),
    c("#E2F4EC"), c("#E8F7E1"), c("#EDF8EE"), c("#E6F3EA")
)
fun rose() = VaultColors(
    c("#FFF7FA"), c("#FFFFFF"), c("#FDECF2"),
    c("#D4537E"), c("#FFDCE7"), Color.White,
    c("#2B1820"), c("#3A2530"), c("#8A6675"), c("#E7C8D3"),
    c("#FFE5EC"), c("#FFF0F4"), c("#FFDCE7"), c("#FFF1F3"),
    c("#FDEAF1"), c("#FFF4F7"), c("#FFE8EE"), c("#FCEEF5")
)
fun ocean() = VaultColors(
    c("#F4F9FF"), c("#FFFFFF"), c("#E8F2FC"),
    c("#378ADD"), c("#D9EBFF"), Color.White,
    c("#162434"), c("#223548"), c("#647A90"), c("#C8D8E8"),
    c("#E3F0FF"), c("#EDF7FF"), c("#EEF4FA"), c("#F5F8FC"),
    c("#DDEEFF"), c("#EEF7FB"), c("#EDF4FA"), c("#E8F1FF")
)
fun amber() = VaultColors(
    c("#FFF9F2"), c("#FFFFFF"), c("#FEF1DE"),
    c("#BA7517"), c("#FBE3BC"), Color.White,
    c("#2C2112"), c("#3C2E1C"), c("#8B7352"), c("#E6D3B5"),
    c("#FFF0D9"), c("#FFF7EB"), c("#FFF1E2"), c("#FFF4D8"),
    c("#FFF8EE"), c("#FFF6E5"), c("#FFF0E8"), c("#FFF7EC")
)
fun violet() = VaultColors(
    c("#F8F5FF"), c("#FFFFFF"), c("#EFE8FC"),
    c("#9B59B6"), c("#E9D8F5"), Color.White,
    c("#241A2C"), c("#33243D"), c("#7B6686"), c("#D8C8E2"),
    c("#F0E5FA"), c("#F6F0FC"), c("#F3E8FA"), c("#F8F1FC"),
    c("#EFEAFF"), c("#F5EEFA"), c("#F7F0FB"), c("#F2EAFE")
)
fun abyss() = VaultColors(
    c("#F2FAFD"), c("#FFFFFF"), c("#E3F2F8"),
    c("#1D6FA4"), c("#D5EAF5"), Color.White,
    c("#132430"), c("#203542"), c("#607887"), c("#C2D7E2"),
    c("#DDEFFA"), c("#EAF6FB"), c("#EEF6F8"), c("#F2F8FA"),
    c("#E3F4FB"), c("#EDF7F8"), c("#EFF6F9"), c("#E8F1F6")
)
fun mono() = VaultColors(
    c("#FAFAFA"), c("#FFFFFF"), c("#F1F1F1"),
    c("#888780"), c("#E5E5E5"), Color.White,
    c("#1E1E1E"), c("#2C2C2C"), c("#6F6F6F"), c("#D0D0D0"),
    c("#F2F2F2"), c("#F7F7F7"), c("#EFEFEF"), c("#F5F5F5"),
    c("#ECECEC"), c("#F3F3F3"), c("#EEEEEE"), c("#F8F8F8")
)
fun sunset() = VaultColors(
    c("#FFF7F2"), c("#FFFFFF"), c("#FDECE3"),
    c("#E87040"), c("#FFDCCF"), Color.White,
    c("#2E1E18"), c("#3D2A22"), c("#8C6B60"), c("#E7CBBE"),
    c("#FFE8DD"), c("#FFF1EA"), c("#FFE4DA"), c("#FFF0E5"),
    c("#FFF5EF"), c("#FFF2E8"), c("#FFEDE7"), c("#FFF3EC")
)
fun cherry() = VaultColors(
    c("#FFF5F7"), c("#FFFFFF"), c("#FDE8ED"),
    c("#E05070"), c("#FFD9E2"), Color.White,
    c("#2D1820"), c("#3B2430"), c("#8C6470"), c("#E6C7D0"),
    c("#FFE4EA"), c("#FFF0F3"), c("#FFDCE4"), c("#FFF1F4"),
    c("#FDECEF"), c("#FFF4F6"), c("#FFE8ED"), c("#FCEEF2")
)
fun arctic() = VaultColors(
    c("#F3FBFD"), c("#FFFFFF"), c("#E5F4F8"),
    c("#4DC8E0"), c("#D6F3F8"), c("#08313A"),
    c("#16242A"), c("#22353D"), c("#66808A"), c("#C6DDE3"),
    c("#DFF7FB"), c("#ECF9FB"), c("#EEF7FA"), c("#F3FAFB"),
    c("#E4F7FB"), c("#EEF9FA"), c("#F0F8FA"), c("#E8F4F8")
)
