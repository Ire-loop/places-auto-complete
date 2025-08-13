@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.places.autocomplete.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.places.autocomplete.R
import com.example.places.autocomplete.ui.screens.autocomplete.SelectLocationScreen
import com.example.places.autocomplete.ui.screens.details.ShipmentDetailsScreen
import com.example.places.autocomplete.ui.screens.map.RouteMapScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStack?.destination?.route ?: Routes.SELECT_LOCATION.name

    // Extract the current route from the destination and get its title
    val currentRoute = Routes.entries.find { it.name == currentDestination }
    val currentTitle = currentRoute?.title ?: "Select Location"

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = currentTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.W600
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            val previousRoute = when (currentRoute) {
                                Routes.SHIPMENT_DETAILS -> Routes.SELECT_LOCATION.name
                                Routes.ROUTE_MAP -> Routes.SHIPMENT_DETAILS.name
                                else -> null
                            }
                            navController.navigate(previousRoute ?: Routes.SELECT_LOCATION.name)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                modifier = Modifier.fillMaxWidth(),
                content = {
                    // You can add bottom navigation items here if needed
                    FilledTonalButton(
                        onClick = {
                            val nextRoute = when (currentRoute) {
                                Routes.SELECT_LOCATION -> Routes.SHIPMENT_DETAILS.name
                                Routes.SHIPMENT_DETAILS -> Routes.ROUTE_MAP.name
                                else -> null
                            }
                            navController.navigate(nextRoute ?: Routes.SELECT_LOCATION.name)
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary),
                        enabled = currentRoute != Routes.ROUTE_MAP // Disable button on the last screen
                    ) {
                        Text(
                            text = "Next",
                            modifier = Modifier.padding(dimensionResource(R.dimen.small))
                        )
                    }
                }
            )
        }
    ) {

        NavHost(
            navController = navController,
            startDestination = Routes.SELECT_LOCATION.name,
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {

            composable(Routes.SELECT_LOCATION.name) { SelectLocationScreen() }

            composable(Routes.SHIPMENT_DETAILS.name) { ShipmentDetailsScreen() }

            composable(Routes.ROUTE_MAP.name) { RouteMapScreen() }
        }
    }
}
