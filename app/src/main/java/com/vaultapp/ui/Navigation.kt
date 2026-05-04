package com.vaultapp.ui

import androidx.compose.runtime.Composable
import androidx.navigation.*
import androidx.navigation.compose.*
import com.vaultapp.ui.screens.*

sealed class Screen(val route: String) {
    object Lock         : Screen("lock")
    object Setup        : Screen("setup")
    object Recover      : Screen("recover")
    object Home         : Screen("home")
    object NoteEdit     : Screen("note_edit?id={id}") { fun go(id: Long = -1L) = "note_edit?id=$id" }
    object Vault        : Screen("vault")
    object PasswordEdit : Screen("pw_edit?id={id}")   { fun go(id: Long = -1L) = "pw_edit?id=$id" }
    object Settings     : Screen("settings")
    object Themes       : Screen("themes")
    object Backup       : Screen("backup")
    object Trash        : Screen("trash")
    object Tags         : Screen("tags")
    object Media        : Screen("media?noteId={noteId}") { fun go(nid: Long) = "media?noteId=$nid" }
}

@Composable
fun VaultNavGraph(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Screen.Lock.route) {
            LockScreen(
                onUnlocked = { navController.navigate(Screen.Home.route) { popUpTo(0) { inclusive = true } } },
                onSetup    = { navController.navigate(Screen.Setup.route) { popUpTo(0) { inclusive = true } } },
                onRecover  = { navController.navigate(Screen.Recover.route) }
            )
        }
        composable(Screen.Setup.route) {
            SetupScreen(onSetupComplete = { navController.navigate(Screen.Home.route) { popUpTo(0) { inclusive = true } } })
        }
        composable(Screen.Recover.route) {
            RecoverScreen(
                onRecovered = { navController.navigate(Screen.Home.route) { popUpTo(0) { inclusive = true } } },
                onBack      = { navController.popBackStack() }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                onNoteClick          = { navController.navigate(Screen.NoteEdit.go(it)) },
                onAddNote            = { navController.navigate(Screen.NoteEdit.go()) },
                onNavigateToVault    = { navController.navigate(Screen.Vault.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(
            route     = Screen.NoteEdit.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L })
        ) {
            NoteEditScreen(
                noteId      = it.arguments?.getLong("id") ?: -1L,
                onBack      = { navController.popBackStack() },
                onMediaOpen = { nid -> navController.navigate(Screen.Media.go(nid)) }
            )
        }
        composable(Screen.Vault.route) {
            VaultScreen(
                onPasswordClick = { navController.navigate(Screen.PasswordEdit.go(it)) },
                onAddPassword   = { navController.navigate(Screen.PasswordEdit.go()) },
                onBack          = { navController.popBackStack() }
            )
        }
        composable(
            route     = Screen.PasswordEdit.route,
            arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L })
        ) {
            PasswordEditScreen(
                passwordId = it.arguments?.getLong("id") ?: -1L,
                onBack     = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onThemes = { navController.navigate(Screen.Themes.route) },
                onBackup = { navController.navigate(Screen.Backup.route) },
                onTrash  = { navController.navigate(Screen.Trash.route) },
                onTags   = { navController.navigate(Screen.Tags.route) },
                onBack   = { navController.popBackStack() }
            )
        }
        composable(Screen.Themes.route) { ThemesScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.Backup.route) { BackupScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.Trash.route) {
            TrashScreen(
                onNoteClick = { navController.navigate(Screen.NoteEdit.go(it)) },
                onBack      = { navController.popBackStack() }
            )
        }
        composable(Screen.Tags.route) {
            TagsScreen(
                onNoteClick = { navController.navigate(Screen.NoteEdit.go(it)) },
                onBack      = { navController.popBackStack() }
            )
        }
        composable(
            route     = Screen.Media.route,
            arguments = listOf(navArgument("noteId") { type = NavType.LongType })
        ) {
            MediaScreen(
                noteId = it.arguments?.getLong("noteId") ?: -1L,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
