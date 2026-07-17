package com.diawatch.app.tile

import android.content.Context
import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material3.Typography.BODY_LARGE
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tooling.preview.devices.WearDevices
import com.diawatch.app.data.SensorKeys.LAST_VALUE
import com.diawatch.app.data.SensorKeys.OLD_TIMESTAMP
import com.diawatch.app.data.SensorKeys.TIMESTAMP
import com.diawatch.app.data.sensorDataStore

import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.sql.Timestamp

private const val RESOURCES_VERSION = "0"

@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService() {

    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
        val glucoseValue : Double = applicationContext.sensorDataStore.data.map {preferences -> preferences[LAST_VALUE] ?: 0.0}.first()
        val timestamp : Double = applicationContext.sensorDataStore.data.map {preferences -> preferences[TIMESTAMP] ?: 0.0}.first()
        return tile(requestParams, this, glucoseValue, timestamp)
    }

    override suspend fun resourcesRequest(requestParams: ResourcesRequest): Resources {
        return resources(requestParams)
    }
}

private fun resources(requestParams: ResourcesRequest): Resources {
    return Resources.Builder()
        .setVersion(RESOURCES_VERSION)
        .build()
}

private fun tile(
    requestParams: RequestBuilders.TileRequest,
    context: Context,
    glucoseValue : Double,
    timestamp: Double
): TileBuilders.Tile {
    return TileBuilders.Tile.Builder()
        .setResourcesVersion(RESOURCES_VERSION)
        .setTileTimeline(
            TimelineBuilders.Timeline.fromLayoutElement(
                materialScope(context, requestParams.deviceConfiguration) {
                    primaryLayout(
                        mainSlot = {
                            text(
                                "${glucoseValue.toString()}, $timestamp".layoutString,
                                typography = BODY_LARGE
                            )
                        }
                    )
                }
            )
        )
        .build()
}

@Preview(device = WearDevices.SMALL_ROUND)
@Preview(device = WearDevices.LARGE_ROUND)
fun tilePreview(context: Context) = TilePreviewData(::resources) {
    tile(it, context, 7.4, 68.0)
}