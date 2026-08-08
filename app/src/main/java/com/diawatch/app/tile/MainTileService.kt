package com.diawatch.app.tile

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.DimensionBuilders.weight
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.ResourceBuilders.Resources
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.protolayout.layout.androidImageResource
import androidx.wear.protolayout.layout.imageResource
import androidx.wear.protolayout.material3.ButtonDefaults.filledTonalButtonColors
import androidx.wear.protolayout.material3.Typography.LABEL_MEDIUM
import androidx.wear.protolayout.material3.button
import androidx.wear.protolayout.material3.card
import androidx.wear.protolayout.material3.icon
import androidx.wear.protolayout.material3.iconButton
import androidx.wear.protolayout.material3.materialScope
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textEdgeButton
import androidx.wear.protolayout.modifiers.LayoutModifier
import androidx.wear.protolayout.modifiers.background
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.RequestBuilders.ResourcesRequest
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.tooling.preview.Preview
import androidx.wear.tiles.tooling.preview.TilePreviewData
import androidx.wear.tooling.preview.devices.WearDevices
import com.diawatch.app.R
import com.diawatch.app.data.DataStoreKey
import com.diawatch.app.data.DataStoreManager
import com.diawatch.app.presentation.MainActivity
import com.google.android.horologist.annotations.ExperimentalHorologistApi
import com.google.android.horologist.tiles.SuspendingTileService


private const val RESOURCES_VERSION = "0"

@OptIn(ExperimentalHorologistApi::class)
class MainTileService : SuspendingTileService() {

    override suspend fun tileRequest(requestParams: RequestBuilders.TileRequest): TileBuilders.Tile {
        val dataStoreManager = DataStoreManager.getInstance(applicationContext)
        val glucoseValue: Double = dataStoreManager.getValue(DataStoreKey.LAST_VALUE)
        val trendArrow: Int = dataStoreManager.getValue(DataStoreKey.TREND_ARROW)
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
    glucoseValue: Double,
    trendArrow: Int
): TileBuilders.Tile {
    return TileBuilders.Tile.Builder()
        .setResourcesVersion(RESOURCES_VERSION)
        .setTileTimeline(
            TimelineBuilders.Timeline.fromLayoutElement(
                materialScope(context, requestParams.deviceConfiguration) {
                    primaryLayout(
                        titleSlot = {
                            text(
                                text = context.getString(R.string.tile_title).layoutString,
                                typography = LABEL_MEDIUM,
                            )
                        },
                        mainSlot = {
                            LayoutElementBuilders.Row.Builder()
                                .setWidth(expand())
                                .setHeight(expand())
                                .addContent(
                                    card(
                                        onClick = clickable(),
                                        width = weight(2f),
                                        height = expand(),
                                        modifier =
                                            LayoutModifier
                                                .background(
                                                    colorScheme.surfaceContainer
                                                ),
                                    ) {
                                        text("a".layoutString)
                                    }
                                )
                                .addContent(
                                    LayoutElementBuilders.Spacer.Builder().setWidth(dp(3f)).build()
                                )
                                .addContent(
                                    LayoutElementBuilders.Column.Builder()
                                        .setWidth(expand())
                                        .setHeight(expand())
                                        .addContent(
                                            iconButton(
                                                onClick = clickable(),
                                                width = weight(1f),
                                                height = expand(),
                                                iconContent = {
                                                    icon(

                                                        ))
                                                    )
                                                }
                                            )
                                        )
                                        .addContent(
                                            LayoutElementBuilders.Spacer.Builder().setHeight(dp(3f)).build()
                                        )
                                        .addContent(
                                            button(
                                                onClick = clickable(),
                                                width = weight(1f),
                                                height = expand(),
                                                labelContent = {text("a".layoutString)}
                                            )
                                        )
                                        .build()
                                )
                                .build()
                        },
                        bottomSlot = {
                            textEdgeButton(
                                labelContent = { text(context.getString(R.string.tile_button).layoutString) },
                                onClick = clickable(
                                    action = ActionBuilders.LaunchAction.Builder()
                                        .setAndroidActivity(
                                            ActionBuilders.AndroidActivity.Builder()
                                                .setPackageName(context.packageName)
                                                .setClassName(MainActivity::class.java.name)
                                                .build()
                                        )
                                        .build()
                                ),
                                colors = filledTonalButtonColors()
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