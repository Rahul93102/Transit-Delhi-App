package com.example.opendelhitransit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.EvStation
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.opendelhitransit.features.fuel.FuelScreen
import com.example.opendelhitransit.features.home.HomeScreen
import com.example.opendelhitransit.features.metro.MetroScreen
import com.example.opendelhitransit.features.transit.RealTimeTransitScreen
import com.example.opendelhitransit.features.transit.LiveBusMapScreen
import com.example.opendelhitransit.ui.theme.OpenDelhiTransitTheme
import com.example.opendelhitransit.viewmodel.TransitViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            OpenDelhiTransitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppContent()
                }
            }
        }
    }
}

@Composable
fun MainAppContent() {
    val navController = rememberNavController()
    var selectedItemIndex by remember { mutableStateOf(0) }
    
    val navigationItems = listOf(
        NavigationItem(
            title = "Home",
            selectedIcon = Icons.Filled.Home,
            route = "home"
        ),
//        NavigationItem(
//            title = "Step Tracker",
//            selectedIcon = Icons.Filled.DirectionsWalk,
//            route = "step_tracker"
//        ),
//        NavigationItem(
//            title = "Transit",
//            selectedIcon = Icons.Filled.DirectionsBus,
//            route = "transit_app"
//        ),
        NavigationItem(
            title = "Metro",
            selectedIcon = Icons.Filled.Train,
            route = "metro"
        ),
        NavigationItem(
            title = "Live Transit",
            selectedIcon = Icons.Filled.DirectionsBus,
            route = "real_time_transit"
        ),
        NavigationItem(
            title = "Fuel",
            selectedIcon = Icons.Filled.EvStation,
            route = "fuel"
        )
    )
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                navigationItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItemIndex == index,
                        onClick = {
                            selectedItemIndex = index
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.selectedIcon,
                                contentDescription = item.title
    )
                        },
                        label = { Text(text = item.title) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(navController = navController)
            }
//            composable("step_tracker") {
//                StepTrackerScreen(navController = navController)
//            }
//            composable("transit_app") {
//                TransitScreen(navController = navController)
//            }
            composable("metro") {
                MetroScreen()
            }
            composable("real_time_transit") {
                RealTimeTransitScreen(navController = navController)
            }
            composable("live_bus_map") {
                // Show the map screen and provide the repository via the ViewModel
                val transitViewModel = hiltViewModel<TransitViewModel>()
                LiveBusMapScreen(
                    transitRepository = transitViewModel.repository
                )
            }
            composable("fuel") {
                FuelScreen()
            }
        }
    }
}

data class NavigationItem(
    val title: String,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    val route: String
)