package com.app.notifications.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.app.notifications.presentation.notification.screen.NotificationDetailScreen
import com.app.notifications.presentation.notification.screen.NotificationScheduleScreen

object NotificationRoutes {
    const val SCHEDULE = "schedule"
    const val DETAIL = "notification_detail/{notificationId}"

    fun detail(notificationId: Int) = "notification_detail/$notificationId"
}

@Composable
fun NotificationNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = NotificationRoutes.SCHEDULE,
    deepLinkNotificationId: Int? = null
) {
    LaunchedEffect(deepLinkNotificationId) {
        deepLinkNotificationId?.let { id ->
            navController.navigate(NotificationRoutes.detail(id)) {
                launchSingleTop = true
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NotificationRoutes.SCHEDULE) {
            NotificationScheduleScreen(
                onNavigateToDetail = { id ->
                    navController.navigate(NotificationRoutes.detail(id))
                }
            )
        }
        composable(
            route = NotificationRoutes.DETAIL,
            arguments = listOf(
                navArgument("notificationId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("notificationId") ?: return@composable
            NotificationDetailScreen(
                notificationId = id,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
