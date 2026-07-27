package co.com.jikanle.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import co.com.jikanle.feature.auth.LoginScreen
import co.com.jikanle.feature.lesson.FUYU_LESSON_ID
import co.com.jikanle.feature.lesson.LessonReaderScreen
import co.com.jikanle.feature.profile.ProfileScreen
import co.com.jikanle.feature.songbridge.SongbridgeScreen

object JikanleRoutes {
    const val Auth = "auth"
    const val Profile = "profile"
    const val Songbridge = "songbridge"
    const val LessonPattern = "lesson/{lessonId}"

    fun lesson(id: String): String = "lesson/$id"
}

@Composable
fun JikanleNavGraph() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = JikanleRoutes.lesson(FUYU_LESSON_ID),
    ) {
        composable(
            route = JikanleRoutes.LessonPattern,
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "jikanle://lesson/{lessonId}" },
                navDeepLink { uriPattern = "https://jikanle.com.co/app/lesson/{lessonId}" },
            ),
        ) {
            LessonReaderScreen(
                onOpenAuth = { navController.navigate(JikanleRoutes.Auth) },
                onOpenProfile = { navController.navigate(JikanleRoutes.Profile) },
            )
        }
        composable(JikanleRoutes.Auth) {
            LoginScreen()
        }
        composable(JikanleRoutes.Profile) {
            ProfileScreen()
        }
        composable(JikanleRoutes.Songbridge) {
            SongbridgeScreen()
        }
    }
}
