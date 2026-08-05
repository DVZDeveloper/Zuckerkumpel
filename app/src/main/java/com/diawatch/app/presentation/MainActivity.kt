package com.diawatch.app.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.datastore.preferences.core.Preferences
import androidx.navigation.NavController
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SplitSwitchButton
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import androidx.work.impl.constraints.controllers.ConstraintController
import com.diawatch.app.R
import com.diawatch.app.data.AlarmScheduler
import com.diawatch.app.data.DataStoreKeys
import com.diawatch.app.data.DataStoreManager
import com.diawatch.app.data.DataStorePreference
import com.diawatch.app.data.Debugger
import com.diawatch.app.data.ServiceLauncher
import com.diawatch.app.presentation.theme.DynamicTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        ServiceLauncher.launch(this)
        AlarmScheduler.schedule(this)
        //Alte Funktionen zum regelmässigen ausführen des Backends
        //WorkScheduler.schedule(this)


        setContent {
            DynamicTheme {
                WearApp()
            }
        }
    }
}

@Composable
fun WearApp() {
    val navController = rememberSwipeDismissableNavController()

    SwipeDismissableNavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(navController)
        }

        composable("alarm_settings") {
            AlarmSettingsScreen(navController)
        }
    }
}


@Composable
fun TransformingLazyColumnItemScope.AlarmSettingsButton(
    title: String,
    transformationSpec: TransformationSpec,
    switchPreference: DataStorePreference<Boolean>,
    limitPreference: DataStorePreference<Double>,
) {
    val dataStoreManager = DataStoreManager.getInstance(LocalContext.current)
    var checked by remember { mutableStateOf(true) }
    var limit = 0.0
    LaunchedEffect(Unit) {
        checked = dataStoreManager.getValue(switchPreference)
        limit = dataStoreManager.getValue(limitPreference)
    }
    var scope = rememberCoroutineScope()
    SplitSwitchButton(
        checked = checked,
        onCheckedChange = {
            checked = it
            scope.launch { dataStoreManager.setValue(switchPreference, it) }
        },
        onContainerClick = {
            TODO("Not implemented")
        },
        toggleContentDescription = "",
        label = {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1
            )
        },
        secondaryLabel = {
            Text(
                text = "$limit mmol/l",
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .transformedHeight(this, transformationSpec),
        transformation = SurfaceTransformation(transformationSpec)
    )
}

@Composable
fun TransformingLazyColumnItemScope.BaseListHeader(
    transformationSpec: TransformationSpec,
    label: String,
){
    ListHeader(
        Modifier
            .fillMaxWidth()
            .transformedHeight(this, transformationSpec),
        transformation = SurfaceTransformation(transformationSpec)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
fun TransformingLazyColumnItemScope.LoginButton(
    transformationSpec: TransformationSpec,
    label: String,
) {
    FilledTonalButton(
        onClick = {TODO("Not implemented")},
        modifier = Modifier
            .fillMaxWidth()
            .transformedHeight(this, transformationSpec),
        transformation = SurfaceTransformation(transformationSpec)
    )  { Text(label) }
}

@Composable
fun HomeScreen(
    navController: NavController
){
    AppScaffold {
        val columnState = rememberTransformingLazyColumnState()
        val transformationSpec = rememberTransformationSpec()
        ScreenScaffold(
            scrollState = columnState,
            edgeButton = {
                EdgeButton(
                    onClick = {TODO("not implemented")},
                    modifier = Modifier.scrollable(
                        columnState,
                        orientation = Orientation.Vertical,
                        reverseDirection = true,
                        overscrollEffect = rememberOverscrollEffect(),
                    )
                ) {
                    Text(stringResource(R.string.button_login))
                }
            }
        ) { contentPadding ->
            TransformingLazyColumn(
                state = columnState,
                contentPadding = contentPadding,
                flingBehavior = TransformingLazyColumnDefaults.snapFlingBehavior(columnState),
                rotaryScrollableBehavior = RotaryScrollableDefaults.snapBehavior(columnState)
            ) {
                item {
                    BaseListHeader(
                        label = stringResource(R.string.header_alarm),
                        transformationSpec = transformationSpec
                    )
                }
                item {
                    AlarmSettingsButton(
                        title = stringResource(R.string.setting_title_high),
                        transformationSpec = transformationSpec,
                        switchPreference = DataStoreKeys.HIGH_BLOOD_SUGAR_ALARM_ENABLED,
                        limitPreference = DataStoreKeys.HIGH_BLOOD_SUGAR_LIMIT,
                    )
                }
                item {
                    AlarmSettingsButton(
                        title =stringResource(R.string.setting_title_low),
                        transformationSpec = transformationSpec,
                        switchPreference = DataStoreKeys.LOW_BLOOD_SUGAR_ALARM_ENABLED,
                        limitPreference = DataStoreKeys.LOW_BLOOD_SUGAR_LIMIT
                    )
                }
                item {
                    BaseListHeader(
                        label = stringResource(R.string.header_login),
                        transformationSpec = transformationSpec
                    )
                }
                item{
                    LoginButton(
                        transformationSpec = transformationSpec,
                        label = stringResource(R.string.button_email)
                    )
                }
                item{
                    LoginButton(
                        transformationSpec = transformationSpec,
                        label = stringResource(R.string.button_password)
                    )
                }
            }
        }
    }
}

@Composable
fun AlarmSettingsScreen(
    NavController: NavController
){

}


@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun AppPreview() {
    WearApp()
}
