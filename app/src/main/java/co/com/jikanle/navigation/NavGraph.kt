package co.com.jikanle.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import co.com.jikanle.R
import co.com.jikanle.feature.profile.BetaScreen
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import co.com.jikanle.feature.auth.LoginScreen
import co.com.jikanle.feature.events.EventsScreen
import co.com.jikanle.feature.lesson.FUYU_LESSON_ID
import co.com.jikanle.feature.lesson.LessonReaderScreen
import co.com.jikanle.feature.songbridge.SongbridgeScreen

object JikanleRoutes {
    const val Events = "events"
    const val Auth = "auth"
    const val Profile = "profile"
    const val Songbridge = "songbridge"
    const val LessonPattern = "lesson/{lessonId}"

    fun lesson(id: String): String = "lesson/$id"
}

@Composable
fun JikanleNavGraph(session: BetaSessionViewModel = hiltViewModel()) {
    androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_START) {
        session.foreground()
    }
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val currentRoute = entry?.destination?.route
    fun openTab(route: String) {
        navController.navigate(route) {
            popUpTo(JikanleRoutes.Events) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(selected = currentRoute == JikanleRoutes.Events, onClick = { openTab(JikanleRoutes.Events) }, icon = { Icon(Icons.Filled.Groups, null) }, label = { Text(stringResource(R.string.events_community)) })
                NavigationBarItem(selected = currentRoute == JikanleRoutes.LessonPattern, onClick = { openTab(JikanleRoutes.lesson(FUYU_LESSON_ID)) }, icon = { Icon(Icons.AutoMirrored.Filled.MenuBook, null) }, label = { Text(stringResource(R.string.events_lesson)) })
                NavigationBarItem(selected = currentRoute == JikanleRoutes.Profile, onClick = { openTab(JikanleRoutes.Profile) }, icon = { Icon(Icons.Filled.Person, null) }, label = { Text(stringResource(R.string.beta_nav)) })
            }
        },
        topBar = {
            if (currentRoute == JikanleRoutes.Auth) {
                IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) }
            }
        },
    ) { padding ->
    NavHost(
        navController = navController,
        startDestination = JikanleRoutes.Events,
        modifier = Modifier.padding(padding),
    ) {
        composable(JikanleRoutes.Events) {
            EventsScreen(
                onOpenLesson = { openTab(JikanleRoutes.lesson(FUYU_LESSON_ID)) },
            )
        }
        composable(
            route = JikanleRoutes.LessonPattern,
            arguments = listOf(navArgument("lessonId") { type = NavType.StringType }),
            deepLinks = listOf(
                navDeepLink { uriPattern = "jikanle://lesson/{lessonId}" },
                navDeepLink { uriPattern = "https://jikanle.com.co/app/lesson/{lessonId}" },
            ),
        ) {
            LessonReaderScreen(
                onOpenEvents = { openTab(JikanleRoutes.Events) },
                onOpenFeedback = { openTab(JikanleRoutes.Profile) },
            )
        }
        composable(JikanleRoutes.Auth) {
            LoginScreen(onAuthenticated = { navController.popBackStack() })
        }
        composable(JikanleRoutes.Profile) {
            BetaScreen(onSignIn = { navController.navigate(JikanleRoutes.Auth) })
        }
        composable(JikanleRoutes.Songbridge) {
            SongbridgeScreen()
        }
    }
    }
}
