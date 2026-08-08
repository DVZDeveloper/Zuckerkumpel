package com.diawatch.app.tile


import android.content.Intent
import android.graphics.Color.argb
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.protolayout.ActionBuilders
import androidx.wear.protolayout.DimensionBuilders.dp
import androidx.wear.protolayout.DimensionBuilders.expand
import androidx.wear.protolayout.DimensionBuilders.weight
import androidx.wear.protolayout.DimensionBuilders.wrap
import androidx.wear.protolayout.LayoutElementBuilders
import androidx.wear.protolayout.LayoutElementBuilders.FontSetting
import androidx.wear.protolayout.TimelineBuilders.Timeline.fromLayoutElement
import androidx.wear.protolayout.layout.androidImageResource
import androidx.wear.protolayout.layout.imageResource
import androidx.wear.protolayout.material3.MaterialScope
import androidx.wear.protolayout.material3.Typography.LABEL_MEDIUM
import androidx.wear.protolayout.material3.Typography.NUMERAL_LARGE
import androidx.wear.protolayout.material3.Typography.NUMERAL_MEDIUM
import androidx.wear.protolayout.material3.Typography.NUMERAL_SMALL
import androidx.wear.protolayout.material3.card
import androidx.wear.protolayout.material3.icon
import androidx.wear.protolayout.material3.iconButton
import androidx.wear.protolayout.material3.primaryLayout
import androidx.wear.protolayout.material3.text
import androidx.wear.protolayout.modifiers.LayoutModifier
import androidx.wear.protolayout.modifiers.background
import androidx.wear.protolayout.modifiers.clickable
import androidx.wear.protolayout.modifiers.loadAction
import androidx.wear.protolayout.types.LayoutColor
import androidx.wear.protolayout.types.layoutString
import androidx.wear.tiles.Material3TileService
import androidx.wear.tiles.RequestBuilders.TileRequest
import androidx.wear.tiles.TileBuilders.Tile
import com.diawatch.app.R
import com.diawatch.app.data.DataStoreKey
import com.diawatch.app.data.DataStoreManager
import com.diawatch.app.data.SnoozeAlarmReceiver
import com.diawatch.app.presentation.MainActivity

private val SPACER_SIZE = dp(4f)
class MainTileService : Material3TileService(
) {


    override suspend fun MaterialScope.tileResponse(
        requestParams: TileRequest
    ): Tile {




        val dataStoreManager = DataStoreManager.getInstance(applicationContext)
        val glucoseValue = dataStoreManager.getValue(DataStoreKey.LAST_VALUE)
        val trendArrow = dataStoreManager.getValue(DataStoreKey.TREND_ARROW)
        val lowLimit = dataStoreManager.getValue(DataStoreKey.LOW_BLOOD_SUGAR_LIMIT)
        val highLimit = dataStoreManager.getValue(DataStoreKey.HIGH_BLOOD_SUGAR_LIMIT)

        val screenSize = requestParams.deviceConfiguration.screenWidthDp

        if(requestParams.currentState.lastClickableId == "snooze_alarm"){
            context.sendBroadcast(
                Intent(
                    context,
                    SnoozeAlarmReceiver::class.java
                )
            )

        }

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
                                        width = weight(2f),
                                        height = expand(),
                                        modifier =
                                            LayoutModifier
                                                .background(
                                                    colorScheme.surfaceContainer
                                                ),
                                    ) {
                                        LayoutElementBuilders.Row.Builder()
                                            .setWidth(wrap())
                                            .setHeight(wrap())
                                            .addContent(
                                                text(
                                                    "%1$.1f".format(glucoseValue).layoutString, //auf eine nachkommastelle limitieren
                                                    typography = if (screenSize >= 225) NUMERAL_MEDIUM else NUMERAL_SMALL,
                                                    settings = listOf(
                                                        FontSetting.tabularNum()
                                                    ),
                                                    color = when {
                                                        //rot für tief, orange für hoch, grün für gut (noch nicht dynamisch darum manchmal nur semischön aber whatever)
                                                        glucoseValue <= lowLimit -> LayoutColor(0xFFFFC7C2.toInt())
                                                        glucoseValue >= highLimit -> LayoutColor(0xFFFFCB98.toInt())
                                                        else -> LayoutColor(0xFF90FA8E.toInt())
                                                    }
                                                )
                                            )
                                            .addContent(
                                                icon(
                                                    imageResource(androidImageResource(
                                                        when (trendArrow){
                                                        1 -> R.drawable.ic_trend_up_fast
                                                        2 -> R.drawable.ic_trend_up
                                                        3 -> R.drawable.ic_trend_flat
                                                        4 -> R.drawable.ic_trend_down
                                                        5 -> R.drawable.ic_trend_down_fast

                                                        else -> R.drawable.ic_error
                                                        }
                                                    )),
                                                    tintColor = colorScheme.onSurfaceVariant
                                                    )
                                            )
                                            .build()

                                    }
                                )
                                .addContent(
                                    LayoutElementBuilders.Spacer.Builder().setWidth(SPACER_SIZE).build()
                                )
                                .addContent(
                                    LayoutElementBuilders.Column.Builder()
                                        .setWidth(expand())
                                        .setHeight(expand())
                                        .addContent(
                                            iconButton(
                                                onClick = clickable(
                                                    id = "snooze_alarm",
                                                    action = loadAction()
                                                ),
                                                width = weight(1f),
                                                height = expand(),
                                                iconContent = {
                                                    icon(
                                                        imageResource(androidImageResource(R.drawable.ic_snooze)),

                                                    )
                                                },
                                            )
                                        )
                                        .addContent(
                                            LayoutElementBuilders.Spacer.Builder().setHeight(SPACER_SIZE)
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
                        /*
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
                                colors = ButtonColors(
                                    containerColor = colorScheme.surfaceContainerHigh,
                                    labelColor = colorScheme.onSurface
                                )
                            )
                        }

                         */
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