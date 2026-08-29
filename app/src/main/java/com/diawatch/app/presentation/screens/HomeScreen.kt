package com.diawatch.app.presentation.screens

import android.app.RemoteInput
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.ListHeaderDefaults
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SplitSwitchButton
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.input.RemoteInputIntentHelper
import androidx.wear.input.wearableExtender
import com.diawatch.app.R
import com.diawatch.app.data.DataStoreManager
import com.diawatch.app.data.DataStoreManager.getValue
import com.diawatch.app.data.DataStoreManager.setValue
import com.diawatch.app.data.LoginService
import com.diawatch.app.presentation.architecture.ButtonSettingsType
import kotlinx.coroutines.launch

@Composable
fun TransformingLazyColumnItemScope.AlarmSettingsButton(
    transformationSpec: TransformationSpec,
    buttonSettingsType: ButtonSettingsType,
    onContainerClick: (String) -> Unit
) {

    DataStoreManager.init(LocalContext.current)

    val buttonSettings = buttonSettingsType.settings
    var checked by remember { mutableStateOf(false) }
    var limit by remember { mutableIntStateOf(0) }

    LaunchedEffect(LocalContext.current) {
        checked = getValue(buttonSettings.enabled)
        limit = getValue(buttonSettings.limit)
    }
    val scope = rememberCoroutineScope()
    SplitSwitchButton(
        checked = checked,
        onCheckedChange = {
            checked = it
            scope.launch { setValue(buttonSettings.enabled, it) }
        },
        onContainerClick = {
            onContainerClick(buttonSettings.titleID)
        },
        toggleContentDescription = "",
        label = {
            Text(
                text = stringResource(buttonSettings.titleResource),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1
            )
        },
        secondaryLabel = {
            Text(
                text = "$limit.0 mmol/l",
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
) {
    ListHeader(
        Modifier
            .fillMaxWidth()
            .minimumVerticalContentPadding(ListHeaderDefaults.minimumTopListContentPadding)
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
fun LoginEdgeButton(
    columnState: TransformingLazyColumnState,
    email: String,
    password: String
) {
    val context = LocalContext.current.applicationContext
    EdgeButton(
        onClick = {
            LoginService.login(context, email, password)
        },
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

const val RESULT_KEY = "RESULT_KEY"

@Composable
fun TransformingLazyColumnItemScope.LoginButton(
    transformationSpec: TransformationSpec,
    label: String,
    text: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean
) {


    var userInput by remember { mutableStateOf(text) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        it.data?.let { data ->
            // Get all the results
            val results: Bundle = RemoteInput.getResultsFromIntent(data)
            // Use the inputTextKey to select the input we are interested in
            val newInputText: CharSequence? = results.getCharSequence(RESULT_KEY)
            // Save the text to our variable as a string. Ensure to handle the null case
            userInput = newInputText?.toString() ?: ""
            onValueChange(userInput)
        }
    }


    FilledTonalButton(
        onClick = {
            val intent = RemoteInputIntentHelper.createActionRemoteInputIntent()
            val remoteInputs = listOf(
                RemoteInput.Builder(RESULT_KEY)
                    .setLabel(label)
                    .wearableExtender {
                        setEmojisAllowed(false)
                        setInputActionType(EditorInfo.IME_ACTION_DONE)
                    }
                    .build()
            )
            RemoteInputIntentHelper.putRemoteInputsExtra(intent, remoteInputs)
            launcher.launch(intent)
        },
        modifier = Modifier
            .fillMaxWidth()
            .transformedHeight(this, transformationSpec),
        transformation = SurfaceTransformation(transformationSpec),
        label = {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium
            )
        },
        secondaryLabel = {
            if (userInput.isNotEmpty()) {
                Text(
                    if (isPassword) "•".repeat(userInput.length) else userInput,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    )
}

@Composable
fun HomeScreen(
    openAlarmScreen: (String) -> Unit
) {

    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }


    ScreenScaffold(
        scrollState = columnState,
        edgeButton = {
            LoginEdgeButton(columnState = columnState, email = email, password = password)
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
                    transformationSpec = transformationSpec,
                    buttonSettingsType = ButtonSettingsType.HIGH,
                ) {
                    openAlarmScreen(it)
                }
            }
            item {
                AlarmSettingsButton(
                    transformationSpec = transformationSpec,
                    buttonSettingsType = ButtonSettingsType.LOW,
                ) {
                    openAlarmScreen(it)
                }
            }
            item {
                BaseListHeader(
                    label = stringResource(R.string.header_login),
                    transformationSpec = transformationSpec
                )
            }
            item {
                LoginButton(
                    transformationSpec = transformationSpec,
                    label = stringResource(R.string.button_email),
                    text = email,
                    onValueChange = { email = it },
                    isPassword = false
                )
            }
            item {
                LoginButton(
                    transformationSpec = transformationSpec,
                    label = stringResource(R.string.button_password),
                    text = password,
                    onValueChange = { password = it },
                    isPassword = true
                )
            }
        }
    }

}