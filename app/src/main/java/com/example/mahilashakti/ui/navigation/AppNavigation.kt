package com.example.mahilashakti.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.mahilashakti.ui.members.MemberListScreen
import com.example.mahilashakti.ui.memberdetail.MemberDetailScreen
import com.example.mahilashakti.ui.admin.AdminLoginScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "admin_login") {
        composable("admin_login") {
            AdminLoginScreen(
                onLoginSuccess = {
                    navController.navigate("member_list") {
                        popUpTo("admin_login") { inclusive = true }
                    }
                }
            )
        }
        composable("member_list") {
            MemberListScreen(
                onMemberClick = { memberId ->
                    navController.navigate("member_detail/$memberId")
                }
            )
        }
        composable(
            route = "member_detail/{memberId}",
            arguments = listOf(navArgument("memberId") { type = NavType.LongType })
        ) { backStackEntry ->
            val memberId = backStackEntry.arguments?.getLong("memberId") ?: return@composable
            MemberDetailScreen(
                memberId = memberId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
