/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)

package com.android.developers.androidify.creation

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
* Copyright 2025 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

@Composable
fun AndroidBotColorPicker(
    selectedBotColor: BotColor,
    modifier: Modifier = Modifier,
    onBotColorSelected: (BotColor) -> Unit,
    listBotColor: List<BotColor>,
) {
    Column(
        modifier
            .fillMaxWidth()
            .wrapContentHeight(),
    ) {
        Text(stringResource(R.string.choose_my_bot_color), fontSize = 20.sp)
        Spacer(Modifier.height(24.dp))
        val listBotColors = listBotColor

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.align(Alignment.CenterHorizontally),
        ) {
            listBotColors.forEach {
                AndroidBotIndividualColor(
                    it,
                    isSelected = selectedBotColor == it,
                    onSelected = {
                        onBotColorSelected(it)
                    },
                )
            }
        }
    }
}

@Composable
fun AndroidBotIndividualColor(
    botColor: BotColor,
    isSelected: Boolean,
    onSelected: (BotColor) -> Unit,
) {
    val clip = animateIntAsState(
        if (isSelected) 0 else 50,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
    )
    Box(
        modifier = Modifier
            .border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(clip.value))
            .size(36.dp)
            .clip(RoundedCornerShape(clip.value))
            .clickable {
                onSelected(botColor)
            },
    ) {
        DisplayBotColor(botColor)

        if (isSelected) {
            Icon(
                ImageVector.vectorResource(R.drawable.rounded_check_24),
                contentDescription = stringResource(R.string.cd_bot_color_selected),
                modifier = Modifier.align(Alignment.Center),
            )
        }
    }
}

@Composable
fun DisplayBotColor(
    botColor: BotColor,
    modifier: Modifier = Modifier,
) {
    if (botColor.imageRes != null) {
        Image(
            painter = painterResource(botColor.imageRes),
            contentDescription = botColor.name,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .requiredSize(48.dp),
        )
    } else if (botColor.color != null) {
        Box(
            modifier = modifier
                .semantics {
                    this.contentDescription = botColor.name
                    this.role = Role.Button
                }
                .background(botColor.color)
                .requiredSize(48.dp),
        )
    }
}
