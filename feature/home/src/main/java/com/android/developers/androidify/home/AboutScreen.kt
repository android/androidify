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
@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3ExpressiveApi::class)

package com.android.developers.androidify.home

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.xr.compose.platform.LocalSpatialCapabilities
import com.android.developers.androidify.home.xr.AboutScreenSpatial
import com.android.developers.androidify.theme.AndroidifyTheme
import com.android.developers.androidify.theme.LocalSharedTransitionScope
import com.android.developers.androidify.theme.SharedElementContextPreview
import com.android.developers.androidify.theme.SharedElementKey
import com.android.developers.androidify.theme.components.SecondaryOutlinedButton
import com.android.developers.androidify.theme.sharedBoundsReveal
import com.android.developers.androidify.util.LargeScreensPreview
import com.android.developers.androidify.util.PhonePreview
import com.android.developers.androidify.util.isWidthAtLeastMedium

@Composable
fun AboutScreen(
    viewModel: AboutViewModel = hiltViewModel(),
    onBackPressed: () -> Unit,
    onTermsClicked: () -> Unit,
    onPrivacyClicked: () -> Unit,
    onLicensesClicked: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    AboutScreenContents(
        onBackPressed = onBackPressed,
        onTermsClicked = onTermsClicked,
        onPrivacyClicked = onPrivacyClicked,
        onLicensesClicked = onLicensesClicked,
        xrEnabled = state.isXrEnabled,
    )
}

@Composable
fun AboutScreenContents(
    onBackPressed: () -> Unit,
    onTermsClicked: () -> Unit,
    onPrivacyClicked: () -> Unit,
    onLicensesClicked: () -> Unit,
    xrEnabled: Boolean = false,
    isMediumWindowSize: Boolean = isWidthAtLeastMedium(),
) {
    val bottomButtons = @Composable {
        FooterButtons(
            modifier = Modifier,
            onTermsClicked = onTermsClicked,
            onPrivacyClicked = onPrivacyClicked,
            onLicensesClicked = onLicensesClicked,
        )
    }
    when {
        xrEnabled && LocalSpatialCapabilities.current.isSpatialUiEnabled ->
            AboutScreenSpatial(onBackPressed = onBackPressed, bottomButtons = bottomButtons)

        isMediumWindowSize ->
            AboutScreenScaffold(topBar = { BackButton(onBackPressed) }) { contentPadding ->
                AboutScreenMedium(
                    padding = contentPadding,
                    bottomButtons = bottomButtons,
                )
            }

        else ->
            AboutScreenScaffold(topBar = { BackButton(onBackPressed) }) { contentPadding ->
                AboutScreenCompact(
                    padding = contentPadding,
                    bottomButtons = bottomButtons,
                )
            }
    }
}

@Composable
fun AboutScreenScaffold(
    topBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    val sharedElementScope = LocalSharedTransitionScope.current
    val navScope = LocalNavAnimatedContentScope.current
    with(sharedElementScope) {
        Scaffold(
            topBar = topBar,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier
                .sharedBoundsReveal(
                    rememberSharedContentState(SharedElementKey.AboutKey),
                    animatedVisibilityScope = navScope,
                ),
            content = content,
        )
    }
}

@Composable
private fun AboutScreenCompact(
    padding: PaddingValues,
    bottomButtons: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .padding(start = 24.dp, end = 24.dp, top = 24.dp),
    ) {
        Text(
            text = stringResource(R.string.how_it_works),
            style = MaterialTheme.typography.displayLarge,
        )
        AboutMessage()
        Spacer(modifier = Modifier.size(36.dp))
        BulletPoint(
            "1",
            stringResource(R.string.about_step1_title),
            stringResource(R.string.about_step1_label),
        )
        Spacer(modifier = Modifier.size(24.dp))
        BulletPoint(
            "2",
            stringResource(R.string.about_step2_title),
            stringResource(R.string.about_step2_label),
        )
        Spacer(modifier = Modifier.size(24.dp))
        BulletPoint(
            "3",
            stringResource(R.string.about_step3_title),
            stringResource(R.string.about_step3_label),
        )
        Spacer(modifier = Modifier.size(24.dp))
        bottomButtons()
        Spacer(modifier = Modifier.size(8.dp))
    }
}

@Composable
fun AboutScreenMedium(
    padding: PaddingValues,
    bottomButtons: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .safeDrawingPadding()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .width(700.dp),
        ) {
            Text(
                text = stringResource(R.string.how_it_works),
                style = MaterialTheme.typography.displayLarge,
            )
            Spacer(Modifier.size(48.dp))
            AboutMessage(textStyle = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.size(48.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                BulletPoint(
                    "1",
                    stringResource(R.string.about_step1_title),
                    stringResource(R.string.about_step1_label),
                    modifier = Modifier.weight(1 / 3f),
                    textStyle = MaterialTheme.typography.bodySmall,
                )
                BulletPoint(
                    "2",
                    stringResource(R.string.about_step2_title),
                    stringResource(R.string.about_step2_label),
                    modifier = Modifier.weight(1 / 3f),
                    textStyle = MaterialTheme.typography.bodySmall,
                )
                BulletPoint(
                    "3",
                    stringResource(R.string.about_step3_title),
                    stringResource(R.string.about_step3_label),
                    modifier = Modifier.weight(1 / 3f),
                    textStyle = MaterialTheme.typography.bodySmall,
                )
            }
            if (bottomButtons != null) {
                Spacer(Modifier.size(48.dp))
                bottomButtons()
                Spacer(Modifier.size(8.dp))
            }
        }
    }
}

@Composable
fun BackButton(onBackPressed: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(
        modifier = modifier
            .safeDrawingPadding()
            .padding(16.dp),
        shape = CircleShape,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        onClick = {
            onBackPressed()
        },
    ) {
        Icon(
            rememberVectorPainter(ImageVector.vectorResource(com.android.developers.androidify.theme.R.drawable.rounded_close_24)),
            contentDescription = stringResource(R.string.about_back_content_description),
        )
    }
}

@Composable
fun FooterButtons(
    modifier: Modifier = Modifier,
    onTermsClicked: () -> Unit,
    onPrivacyClicked: () -> Unit,
    onLicensesClicked: () -> Unit,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SecondaryOutlinedButton(
            onClick = {
                onTermsClicked()
            },
            buttonText = stringResource(R.string.terms),
        )
        SecondaryOutlinedButton(
            onClick = {
                onPrivacyClicked()
            },
            buttonText = stringResource(R.string.privacy),
        )
        SecondaryOutlinedButton(
            onClick = {
                onLicensesClicked()
            },
            buttonText = stringResource(R.string.oss_license),
        )
    }
}

@Composable
fun AboutMessage(textStyle: TextStyle = MaterialTheme.typography.bodyLarge) {
    Text(
        stringResource(R.string.about_message),
        style = textStyle.copy(fontWeight = FontWeight.Bold),
    )
}

@Composable
fun BulletPoint(
    bulletText: String,
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
) {
    Column(modifier.height(IntrinsicSize.Min)) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                bulletText,
                style = textStyle,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        Column(modifier = Modifier) {
            Text(
                title,
                style = textStyle.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier,
            )
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text,
                style = textStyle,
                modifier = Modifier,
            )
        }
    }
}

@PhonePreview
@Composable
private fun AboutPreviewCompact() {
    SharedElementContextPreview {
        AboutScreenContents(
            isMediumWindowSize = false,
            onBackPressed = {},
            onTermsClicked = {},
            onPrivacyClicked = {},
            onLicensesClicked = {},
        )
    }
}

@LargeScreensPreview
@Composable
private fun AboutPreviewLargeScreens() {
    SharedElementContextPreview {
        AboutScreenContents(
            isMediumWindowSize = true,
            onBackPressed = {},
            onTermsClicked = {},
            onPrivacyClicked = {},
            onLicensesClicked = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BulletPointPreview() {
    AndroidifyTheme {
        BulletPoint("1", "Photo", "Take a photo")
    }
}
