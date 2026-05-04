package com.vaultapp.ui.theme

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

// Extension on M3 MaterialTheme (no name clash since this is an extension property)
val MaterialTheme.vaultColors: VaultColors @Composable get() = LocalVaultColors.current

@Composable
fun VaultTheme(appTheme: AppTheme = AppTheme.MIDNIGHT, content: @Composable () -> Unit) {
    val vc = appTheme.toVaultColors()
    val isLight = appTheme == AppTheme.CLOUD
    val cs = if (isLight) lightColorScheme(primary = vc.primary, background = vc.background, surface = vc.surface,
        onPrimary = vc.onPrimary, onBackground = vc.onBackground, onSurface = vc.onSurface,
        surfaceVariant = vc.surfaceVariant, outline = vc.outline)
    else darkColorScheme(primary = vc.primary, background = vc.background, surface = vc.surface,
        onPrimary = vc.onPrimary, onBackground = vc.onBackground, onSurface = vc.onSurface,
        surfaceVariant = vc.surfaceVariant, outline = vc.outline)
    CompositionLocalProvider(LocalVaultColors provides vc) {
        MaterialTheme(colorScheme = cs, typography = VaultTypography, content = content)
    }
}

fun AppTheme.toVaultColors() = when (this) {
    AppTheme.MIDNIGHT -> midnight(); AppTheme.CLOUD    -> cloud()
    AppTheme.FOREST   -> forest();   AppTheme.ROSE     -> rose()
    AppTheme.OCEAN    -> ocean();    AppTheme.AMBER    -> amber()
    AppTheme.VIOLET   -> violet();   AppTheme.ABYSS    -> abyss()
    AppTheme.MONO     -> mono();     AppTheme.SUNSET   -> sunset()
    AppTheme.CHERRY   -> cherry();   AppTheme.ARCTIC   -> arctic()
}

private fun c(hex: String) = Color(android.graphics.Color.parseColor(hex))

fun midnight() = VaultColors(c("#0F0F14"),c("#1E1E2E"),c("#2A2A3E"),c("#7C6AF5"),c("#2A1A50"),Color.White,c("#E8E8F0"),c("#C8C8E0"),c("#8888AA"),c("#3A3A50"),c("#2D2460"),c("#0D3D30"),c("#3D1A2E"),c("#3A2300"),c("#0D2A45"),c("#1A3010"),c("#3A1010"),c("#1A1A40"))
fun cloud()    = VaultColors(c("#F8F8FF"),c("#FFFFFF"),c("#F0F0F8"),c("#6B5CE7"),c("#EDEAFF"),c("#0F0F14"),c("#1A1A2E"),c("#2A2A3E"),c("#6B6B8A"),c("#D0D0E0"),c("#EDE9FF"),c("#E0F4EE"),c("#FFE8F0"),c("#FFF3E0"),c("#E3F2FD"),c("#E8F5E9"),c("#FFEBEE"),c("#F3E8FF"))
fun forest()   = VaultColors(c("#0A1F18"),c("#0D3D30"),c("#1A4A3A"),c("#1D9E75"),c("#0A2A20"),Color.White,c("#B0E8D0"),c("#90D0B8"),c("#6AAA90"),c("#2A5040"),c("#0D3D30"),c("#1A4020"),c("#2A3010"),c("#1A2A10"),c("#0A2A20"),c("#203A18"),c("#0F3020"),c("#183020"))
fun rose()     = VaultColors(c("#1A0610"),c("#3D1A2E"),c("#4A2038"),c("#D4537E"),c("#3D1A2E"),Color.White,c("#FFD0E0"),c("#EEB8CC"),c("#CC90A8"),c("#5A2A3E"),c("#3D1A2E"),c("#2A1020"),c("#3A0818"),c("#2A0A14"),c("#1A0810"),c("#300A1A"),c("#200810"),c("#401428"))
fun ocean()    = VaultColors(c("#060D1A"),c("#0D2A45"),c("#143055"),c("#378ADD"),c("#0A2040"),Color.White,c("#B8D8F8"),c("#90C0F0"),c("#6898C8"),c("#1A3A58"),c("#0D2A45"),c("#0A2030"),c("#102838"),c("#0A1828"),c("#081520"),c("#0F2230"),c("#0A1A28"),c("#102840"))
fun amber()    = VaultColors(c("#1A1000"),c("#3A2300"),c("#4A2E00"),c("#BA7517"),c("#3A2300"),Color.White,c("#FFE0A0"),c("#EEC880"),c("#CCA060"),c("#5A3800"),c("#3A2300"),c("#2A1800"),c("#3A1800"),c("#281000"),c("#1A0800"),c("#301A00"),c("#200E00"),c("#402800"))
fun violet()   = VaultColors(c("#100A20"),c("#2A1A50"),c("#361F65"),c("#9B59B6"),c("#2A1A50"),Color.White,c("#D8C0FF"),c("#C0A0F0"),c("#9870D0"),c("#3E2260"),c("#2A1A50"),c("#1E1040"),c("#301848"),c("#200E38"),c("#180A28"),c("#281440"),c("#1A0838"),c("#381C58"))
fun abyss()    = VaultColors(c("#030810"),c("#001A2A"),c("#002038"),c("#1D6FA4"),c("#001A30"),Color.White,c("#90C8E8"),c("#70A8CC"),c("#5088A8"),c("#002840"),c("#001A2A"),c("#001018"),c("#001820"),c("#000E18"),c("#000810"),c("#001418"),c("#000A14"),c("#001C28"))
fun mono()     = VaultColors(c("#0A0A0A"),c("#1A1A1A"),c("#242424"),c("#888780"),c("#2A2A2A"),Color.White,c("#E0E0E0"),c("#C0C0C0"),c("#909090"),c("#3A3A3A"),c("#1E1E1E"),c("#242424"),c("#1A1A1A"),c("#202020"),c("#181818"),c("#222222"),c("#161616"),c("#262626"))
fun sunset()   = VaultColors(c("#1A0800"),c("#3A1800"),c("#4A2010"),c("#E87040"),c("#3A1800"),Color.White,c("#FFD8B8"),c("#EEC098"),c("#CC9878"),c("#5A2A10"),c("#3A1800"),c("#2A1000"),c("#3A0C00"),c("#280800"),c("#1A0600"),c("#300E00"),c("#200A00"),c("#402010"))
fun cherry()   = VaultColors(c("#1A0008"),c("#3A0018"),c("#4A0820"),c("#E05070"),c("#3A0018"),Color.White,c("#FFB8C8"),c("#EE98A8"),c("#CC7888"),c("#5A1028"),c("#3A0018"),c("#2A0010"),c("#3A0008"),c("#280008"),c("#1A0006"),c("#300010"),c("#200008"),c("#400018"))
fun arctic()   = VaultColors(c("#0A1218"),c("#152030"),c("#1E2E40"),c("#4DC8E0"),c("#0A1C28"),c("#001820"),c("#B8E8F8"),c("#90D0E8"),c("#68B8D0"),c("#1E3048"),c("#152030"),c("#0A1A28"),c("#101E30"),c("#081420"),c("#060E18"),c("#0E1C28"),c("#080C18"),c("#162438"))
