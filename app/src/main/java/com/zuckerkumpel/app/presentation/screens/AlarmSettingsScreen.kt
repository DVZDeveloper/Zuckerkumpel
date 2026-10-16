package com.zuckerkumpel.app.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnDefaults
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnItemScope
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.foundation.rotary.RotaryScrollableDefaults
import androidx.wear.compose.material3.Dialog
import androidx.wear.compose.material3.EdgeButton
import androidx.wear.compose.material3.FilledTonalButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.ListHeader
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Picker
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.SurfaceTransformation
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.lazy.TransformationSpec
import androidx.wear.compose.material3.lazy.rememberTransformationSpec
import androidx.wear.compose.material3.lazy.transformedHeight
import androidx.wear.compose.material3.rememberPickerState
import androidx.wear.tiles.TileService
import com.zuckerkumpel.app.R
import com.zuckerkumpel.app.data.DataStoreManager.getValue
import com.zuckerkumpel.app.data.DataStoreManager.setValue
import com.zuckerkumpel.app.presentation.architecture.ButtonSettings
import com.zuckerkumpel.app.presentation.architecture.ButtonSettingsType
import com.zuckerkumpel.app.tile.MainTileService
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class AlarmSettingsButtonType {
    LIMIT,
    TIMEOUT;
}


@Composable
fun AlarmSettingsScreen(
    titleID: String

) {


    val settings = ButtonSettingsType.fromId(titleID).settings
    val columnState = rememberTransformingLazyColumnState()
    val transformationSpec = rememberTransformationSpec()

    ScreenScaffold(
        scrollState = columnState,
    ) { contentPadding ->
        TransformingLazyColumn(
            state = columnState,
            contentPadding = contentPadding,
            flingBehavior = TransformingLazyColumnDefaults.snapFlingBehavior(columnState),
            rotaryScrollableBehavior = RotaryScrollableDefaults.snapBehavior(columnState)
        ) {
            item {
                BaseListHeader(
                    transformationSpec = transformationSpec,
                    label = stringResource(settings.titleResource)
                )
            }
            item {
                NumberEntryButton(
                    transformationSpec = transformationSpec,
                    title = stringResource(R.string.limit_title),
                    titleID = titleID,
                    unit = stringResource(R.string.limit_unit),
                    type = AlarmSettingsButtonType.LIMIT,
                    settings = settings
                )
            }
            item {
                NumberEntryButton(
                    transformationSpec = transformationSpec,
                    title = stringResource(R.string.timeout_title),
                    titleID = titleID,
                    unit = stringResource(R.string.timeout_unit),
                    type = AlarmSettingsButtonType.TIMEOUT,
                    settings = settings
                )
            }
        }
    }

}

@Composable
fun PickerDialog(
    showDialog: Boolean,
    initialValue: Double,
    titleID: String,
    type: AlarmSettingsButtonType,
    title: String,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    val offset = if (type == AlarmSettingsButtonType.LIMIT) {
        when (titleID) {
            "high" -> 12.0
            "low" -> 3.0
            else -> 0.0
        }
    } else {
        15.0
    }

    val stepSize = when (type) {
        AlarmSettingsButtonType.LIMIT ->
            when (titleID) {
                "high" -> 0.2
                "low" -> 0.1
                else -> 1.0
            }

        AlarmSettingsButtonType.TIMEOUT -> 5.0
    }

    val pickerState = rememberPickerState(
        initialNumberOfOptions = 30,
        initiallySelectedIndex = ((initialValue - offset) / stepSize).roundToInt(),
        false
    )

    Dialog(
        visible = showDialog,
        onDismissRequest = onDismiss,
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ListHeader {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Picker(
                state = pickerState,
                contentDescription = null,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { index ->
                Text(
                    text = "%1$.1f".format(index * stepSize + offset),
                    style = MaterialTheme.typography.numeralLarge
                )
            }

            EdgeButton(
                onClick = { onConfirm((pickerState.selectedOptionIndex) * stepSize + offset) }
            ) {
                Icon(
                    painterResource(R.drawable.ic_check),
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun TransformingLazyColumnItemScope.NumberEntryButton(
    transformationSpec: TransformationSpec,
    titleID: String,
    title: String,
    unit: String,
    settings: ButtonSettings,
    type: AlarmSettingsButtonType,

    ) {
    val screenSize = LocalConfiguration.current.screenWidthDp

    val context = LocalContext.current.applicationContext
    val scope = rememberCoroutineScope()
    var showPicker by remember { mutableStateOf(false) }
    var value by remember { mutableDoubleStateOf(0.0) }


    LaunchedEffect(LocalContext.current) {
        value = getValue(
            when (type) {
                AlarmSettingsButtonType.TIMEOUT -> settings.timeout
                AlarmSettingsButtonType.LIMIT -> settings.limit
            }
        ).toDouble()
    }
    FilledTonalButton(
        onClick = {
            showPicker = true
        },
        modifier = Modifier
            .fillMaxWidth()
            .transformedHeight(this, transformationSpec),
        label = {
            Text(
                title,
                style = if (screenSize >= 190) MaterialTheme.typography.labelMedium
                else MaterialTheme.typography.labelSmall,
            )

        },
        secondaryLabel = {
            Text(
                unit.format(value),
            )
        },
        transformation = SurfaceTransformation(transformationSpec)
    )
    PickerDialog(
        showDialog = showPicker,
        initialValue = value,
        type = type,
        titleID = titleID,
        onConfirm = {
            value = it
            showPicker = false
            TileService.getUpdater(context)
                .requestUpdate(MainTileService::class.java)

            scope.launch {
                when (type) {
                    AlarmSettingsButtonType.TIMEOUT -> setValue(settings.timeout, it.toInt())
                    AlarmSettingsButtonType.LIMIT -> setValue(settings.limit, it)
                }
            }
        },
        onDismiss = { showPicker = false },
        title = title
    )
}







