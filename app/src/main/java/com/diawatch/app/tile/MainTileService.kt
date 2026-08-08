package com.diawatch.app.tile


import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.DimensionBuilders.weight
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.TimelineBuilders.Timeline.fromLayoutElement
import androidx.wear.protolayout.layout.androidImageResource
import androidx.wear.protolayout.layout.imageResource
import androidx.wear.protolayout.material3.ButtonDefaults.filledTonalButtonColors
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.Typography.LABEL_MEDIUM
import androidx.wear.protolayout.material3.button
import androidx.wear.protolayout.material3.card
import androidx.wear.protolayout.material3.icon
import androidx.wear.protolayout.material3.iconButton
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.material3.textEdgeButton
import androidx.wear.protolayout.modifiers.LayoutModifier
import androidx.wear.protolayout.modifiers.background
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.Material3TileService
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.tiles.TileBuilders.Tile
import com.diawatch.app.R
import com.diawatch.app.data.DataStoreKey
import com.diawatch.app.data.DataStoreManager
import com.diawatch.app.presentation.MainActivity


private const val RESOURCES_VERSION = "0"

class MainTileService : Material3TileService(
) {


    override suspend fun MaterialScope.tileResponse(
        requestParams: TileRequest
    ): Tile {
        val dataStoreManager = DataStoreManager.getInstance(applicationContext)
        dataStoreManager.getValue(DataStoreKey.LAST_VALUE)
        dataStoreManager.getValue(DataStoreKey.TREND_ARROW)
        return Tile.Builder()
            .setTileTimeline(
                fromLayoutElement(
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
                                                        imageResource(androidImageResource(R.drawable.ic_snooze))
                                                    )
                                                }
                                            )
                                        )
                                        .addContent(
                                            LayoutElementBuilders.Spacer.Builder().setHeight(dp(3f))
                                                .build()
                                        )
                                        .addContent(
                                            iconButton(
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
                                                width = weight(1f),
                                                height = expand(),
                                                iconContent = {
                                                    icon(
                                                        imageResource(androidImageResource(R.drawable.ic_settings))
                                                    )
                                                }
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
                )
            )
            .build()

    }
}


/*
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


 */