package com.diawatch.app.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.diawatch.app.data.AlarmScheduler
import com.diawatch.app.data.Debugger
import com.diawatch.app.data.CheckBloodSugarReceiver
import com.diawatch.app.presentation.navigation.Route
import com.diawatch.app.presentation.screens.HomeScreen
import com.diawatch.app.presentation.theme.DynamicTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class MainActivity : ComponentActivity() {

    private  val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DynamicTheme {
                WearApp()
            }
        }

       Handler(Looper.getMainLooper()).post {

            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(
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

            sendBroadcast(Intent(applicationContext,CheckBloodSugarReceiver::class.java))


        }

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
                    HomeScreen()
                }
                entry<Route.AlarmSettings> {
                    AlarmSettingsScreen()
                }
            }
        )
    }
}




@Composable
fun AlarmSettingsScreen(
){

}


@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun AppPreview() {
    WearApp()
}
