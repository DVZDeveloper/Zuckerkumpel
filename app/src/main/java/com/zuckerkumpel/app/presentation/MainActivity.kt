package com.zuckerkumpel.app.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.navigation3.rememberSwipeDismissableSceneStrategy
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.zuckerkumpel.app.data.AlarmScheduler
import com.zuckerkumpel.app.data.CheckBloodSugarReceiver
import com.zuckerkumpel.app.data.DataStoreManager
import com.zuckerkumpel.app.data.Debugger
import com.zuckerkumpel.app.presentation.navigation.Route
import com.zuckerkumpel.app.presentation.screens.AlarmSettingsScreen
import com.zuckerkumpel.app.presentation.screens.HomeScreen
import com.zuckerkumpel.app.presentation.theme.DynamicTheme

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DynamicTheme {
                WearApp()
            }
        }

        DataStoreManager.init(applicationContext)
        val nm = getSystemService(NotificationManager::class.java)

        nm.createNotificationChannel(
            NotificationChannel(
                "alarm_channel",
                "Alarm Channel",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High Importance Channel für Alarme"
            }
        )




        Debugger.print(applicationContext, "MAIN_ACTIVITY", "restarted chain due to app boot")

        AlarmScheduler.schedule(this)

        sendBroadcast(Intent(applicationContext, CheckBloodSugarReceiver::class.java))


    }
}


@Composable
fun WearApp() {
    val backStack = rememberNavBackStack(Route.Home as NavKey)
    val strategy = rememberSwipeDismissableSceneStrategy<NavKey>()
    AppScaffold {
        NavDisplay(
            backStack = backStack,
            sceneStrategies = listOf(strategy),
            entryProvider = entryProvider {
                entry<Route.Home> {
                    HomeScreen {
                        backStack.add(Route.AlarmSettings(it))
                    }
                }
                entry<Route.AlarmSettings> { screen ->
                    AlarmSettingsScreen(screen.titleID)
                }
            }
        )
    }
}


@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun AppPreview() {
    WearApp()
}
