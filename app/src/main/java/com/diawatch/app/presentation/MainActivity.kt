package com.diawatch.app.presentation

import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SplitSwitchButton
import androidx.wear.compose.material3.SplitSwitchButtonColors
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.ui.tooling.preview.WearPreviewDevices
import androidx.wear.compose.ui.tooling.preview.WearPreviewFontScales
import com.diawatch.app.R
import com.diawatch.app.data.AlarmScheduler
import com.diawatch.app.data.Debugger
import com.diawatch.app.data.ServiceLauncher
import com.diawatch.app.presentation.theme.DynamicTheme

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
                        limit = 15.0,
                        transformationSpec = transformationSpec
                    )
                }
                item {
                    AlarmSettingsButton(
                        title =stringResource(R.string.setting_title_low),
                        limit = 3.9,
                        transformationSpec = transformationSpec
                    )
                }
                item {
                    BaseListHeader(
                        label = stringResource(R.string.header_login),
                        transformationSpec = transformationSpec
                    )
                }
            }
        }
    }
}


@Composable
fun TransformingLazyColumnItemScope.AlarmSettingsButton(
    title: String,
    limit: Double,
    transformationSpec: TransformationSpec
) {
    var checked by remember { mutableStateOf(true) }
    SplitSwitchButton(
        checked = checked,
        onCheckedChange = { checked = it },
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
        Text(text = label)
    }
}



@WearPreviewDevices
@WearPreviewFontScales
@Composable
fun DefaultPreview() {
    WearApp()
}

