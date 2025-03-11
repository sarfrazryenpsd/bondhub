package com.ryen.bondhub.presentation.screens.navBar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ryen.bondhub.presentation.theme.NavBarSurface

@Composable
fun CustomNavBar(
    navController: NavHostController,
    currentRoute: String?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp)
        ,
        colors = CardDefaults.cardColors(
            containerColor = NavBarSurface
        ),
        shape = RoundedCornerShape(100),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItems.getAllItems().forEach { item ->
                val isSelected = currentRoute == item.route
                BottomNavItem(
                    item = item,
                    isSelected = isSelected,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                // Pop up to the start destination to avoid building up a large stack
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination
                                launchSingleTop = true
                                // Restore state when navigating back to a previously selected item
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    }
}

@Preview
@Composable
private fun CustomNavBarPrev() {
    CustomNavBar(
        navController = rememberNavController(),
        currentRoute = BottomNavItems.FriendRequests.route
    )
}