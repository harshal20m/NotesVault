package com.vaultapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.vaultapp.ui.components.FloatingBottomNav
import com.vaultapp.ui.screens.*
import com.vaultapp.ui.theme.vaultColors

sealed class Screen(val route: String) {
    object Lock         : Screen("lock")
    object Setup        : Screen("setup")
    object Recover      : Screen("recover")
    object Home         : Screen("home")
    object Vault        : Screen("vault")
    object Analytics    : Screen("analytics")
    object NoteEdit     : Screen("note_edit?id={id}") { fun go(id: Long = -1L) = "note_edit?id=$id" }
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
    val vc = MaterialTheme.vaultColors
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show nav bar only on main screens
    val showNavBar = currentRoute in listOf(
        Screen.Home.route,
        Screen.Vault.route,
        Screen.Analytics.route,
        // Also show on settings, backup, trash, tags since they are main-level sections
        Screen.Settings.route,
        Screen.Backup.route,
        Screen.Trash.route,
        Screen.Tags.route
    )

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.fillMaxSize()
        ) {

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
            composable(Screen.Analytics.route) {
                AnalyticsScreen(
                    onBack = { navController.popBackStack() }
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

        if (showNavBar) {
            FloatingBottomNav(
                selectedIndex = when (currentRoute) {
                    Screen.Home.route -> 0
                    Screen.Vault.route -> 1
                    Screen.Analytics.route -> 2
                    Screen.Settings.route, Screen.Backup.route, Screen.Trash.route, Screen.Tags.route -> 3
                    else -> 0
                },
                onHome = {
                    if (currentRoute != Screen.Home.route) {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onVault = {
                    if (currentRoute != Screen.Vault.route) {
                        navController.navigate(Screen.Vault.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onAnalytics = {
                    if (currentRoute != Screen.Analytics.route) {
                        navController.navigate(Screen.Analytics.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                onSettings = {
                    if (currentRoute != Screen.Settings.route) {
                        navController.navigate(Screen.Settings.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                vc = vc,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 12.dp)
            )
        }
    }
}
