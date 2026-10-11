package com.diawatch.app.presentation.screens

import android.app.Dialog
import android.app.RemoteInput
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnState
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.Dialog
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.Icon
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
import com.diawatch.app.data.LoginState
import com.diawatch.app.data.LoginStateHolder
import com.diawatch.app.presentation.architecture.ButtonSettingsType
import kotlinx.coroutines.delay
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
    var limit by remember { mutableDoubleStateOf(0.0) }

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
    password: String,
    onClick: (Unit) -> Unit
) {
    val context = LocalContext.current.applicationContext
    EdgeButton(
        onClick = {
            onClick
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
            val results: Bundle = RemoteInput.getResultsFromIntent(data)
            val newInputText: CharSequence? = results.getCharSequence(RESULT_KEY)
            userInput = newInputText?.toString()?.filterNot { it.isWhitespace() } ?: ""
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
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
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

    val loginState by LoginStateHolder.state.collectAsState()


    ScreenScaffold(
        scrollState = columnState,
        edgeButton = {
            LoginEdgeButton(
                columnState = columnState,
                email = email,
                password = password,
            ) {
                password = ""
                email = ""
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
        LoginStatusDialog(loginState)
    }

}

@Composable
fun LoginStatusDialog(
    state: LoginState
) {
    var visible by remember { mutableStateOf(false) }
    var shown by remember { mutableStateOf<LoginState>(LoginState.Loading) }

    LaunchedEffect(state) {
        when (state) {
            LoginState.Idle -> visible = false
            LoginState.Loading -> { shown = state; visible = true }
            LoginState.Success -> {
                shown = state; visible = true
                delay(1500)
                visible = false
            }
            is LoginState.Error -> {
                shown = state; visible = true
                delay(2000)
                visible = false
            }
        }
    }

    Dialog(
        visible = visible,
        onDismissRequest = { visible = false }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val s = shown) {
                LoginState.Loading -> {

                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Loading…")
                }
                LoginState.Success -> {
                    Icon(
                        painterResource(R.drawable.ic_check),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Success")

                }
                is LoginState.Error -> {
                    Icon(
                        painterResource(R.drawable.ic_error),
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(s.message ?: "Failed", textAlign = TextAlign.Center)
                }
                LoginState.Idle -> Unit
            }
        }
    }
}