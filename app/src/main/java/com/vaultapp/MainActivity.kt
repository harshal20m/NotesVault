package com.vaultapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.rememberNavController
import com.vaultapp.data.local.PreferencesManager
import com.vaultapp.data.model.AppTheme
import com.vaultapp.service.NotificationHelper
import com.vaultapp.ui.Screen
import com.vaultapp.ui.VaultNavGraph
import com.vaultapp.ui.theme.VaultTheme
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createChannels(this)
        setContent {
            val vm: MainViewModel = hiltViewModel()
            val appTheme  by vm.appTheme.collectAsStateWithLifecycle()
            val isSetup   by vm.isSetupComplete.collectAsStateWithLifecycle()
            val isLoaded  by vm.isLoaded.collectAsStateWithLifecycle()

            VaultTheme(appTheme = appTheme) {
                // FIX: only render nav graph after prefs are loaded
                // prevents Setup screen flashing on every launch
                if (isLoaded) {
                    val navController = rememberNavController()
                    val start = if (isSetup) Screen.Lock.route else Screen.Setup.route
                    VaultNavGraph(navController = navController, startDestination = start)
                }
            }
        }
    }
}

@HiltViewModel
class MainViewModel @Inject constructor(private val prefs: PreferencesManager) : ViewModel() {
    val appTheme        = prefs.appTheme       .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), AppTheme.MIDNIGHT)
    val isSetupComplete = prefs.isSetupComplete.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    // FIX: separate state that starts false and flips true once prefs emit their first value
    private val _isLoaded = MutableStateFlow(false)
    val isLoaded = _isLoaded.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        viewModelScope.launch {
            // Wait for first real value from isSetupComplete, then mark loaded
            prefs.isSetupComplete.collect {
                _isLoaded.value = true
                return@collect  // only need first emission
            }
        }
    }
}
