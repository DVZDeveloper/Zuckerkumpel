package com.diawatch.app.tile

import android.content.Context
import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.material3.Typography.BODY_LARGE
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tooling.preview.devices.WearDevices
import com.diawatch.app.data.DataStoreKey
import com.diawatch.app.data.DataStoreManager
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService


private const val RESOURCES_VERSION = "0"

@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService() {

    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
        val dataStoreManager = DataStoreManager.getInstance(applicationContext)
        val glucoseValue : Double = dataStoreManager.getValue(DataStoreKey.LAST_VALUE)
        val trendArrow : Int = dataStoreManager.getValue(DataStoreKey.TREND_ARROW)
        return tile(requestParams, this, glucoseValue, trendArrow)
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
    trendArrow: Int
): TileBuilders.Tile {
    return TileBuilders.Tile.Builder()
        .setResourcesVersion(RESOURCES_VERSION)
        .setTileTimeline(
            TimelineBuilders.Timeline.fromLayoutElement(
                materialScope(context, requestParams.deviceConfiguration) {
                    primaryLayout(
                        mainSlot = {
                            text(
                                "$glucoseValue, $trendArrow".layoutString,
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
    tile(it, context, 7.4, 3)
}