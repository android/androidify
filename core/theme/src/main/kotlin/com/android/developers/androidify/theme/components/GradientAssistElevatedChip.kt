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
package com.android.developers.androidify.theme.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ChipColors
import androidx.compose.material3.ChipElevation
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.IntrinsicMeasurable
import androidx.compose.ui.layout.IntrinsicMeasureScope
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasurePolicy
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.layout.Placeable
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.offset
import androidx.compose.ui.util.fastFirst
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastMaxOfOrNull
import androidx.compose.ui.util.fastSumBy
import com.android.developers.androidify.util.dpToPx

@Composable
fun gradientChipColorDefaults(): GradientChipColors = GradientChipColors(
    containerColor = SolidColor(MaterialTheme.colorScheme.surfaceContainerLow),
    labelColor = MaterialTheme.colorScheme.onSurface,
    leadingIconContentColor = MaterialTheme.colorScheme.primary,
    trailingIconContentColor = MaterialTheme.colorScheme.primary,
    disabledContainerColor = SolidColor(
        MaterialTheme.colorScheme.onSurface
            .copy(alpha = 0.12f),
    ),
    disabledLabelColor = MaterialTheme.colorScheme.onSurface,
    disabledLeadingIconContentColor = MaterialTheme.colorScheme.onSurface,
    disabledTrailingIconContentColor = MaterialTheme.colorScheme.onSurface
        .copy(alpha = 0.38f),
)

/**
 * This is a duplication of AssistElevatedChip, with the option to set a Brush as a color for the container.
 */
@Composable
fun GradientAssistElevatedChip(
    onClick: () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    shape: Shape = AssistChipDefaults.shape,
    colors: GradientChipColors = gradientChipColorDefaults(),
    elevation: ChipElevation? = AssistChipDefaults.elevatedAssistChipElevation(),
    border: BorderStroke? = null,
    interactionSource: MutableInteractionSource? = null,
) =
    Chip(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        label = label,
        labelTextStyle = MaterialTheme.typography.labelLarge,
        labelColor = colors.labelColor(enabled),
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        elevation = elevation,
        colors = colors,
        minHeight = AssistChipDefaults.Height,
        paddingValues = AssistChipPadding,
        shape = shape,
        border = border,
        interactionSource = interactionSource,
    )

@Composable
private fun Chip(
    modifier: Modifier,
    onClick: () -> Unit,
    enabled: Boolean,
    label: @Composable () -> Unit,
    labelTextStyle: TextStyle,
    labelColor: Color,
    leadingIcon: @Composable (() -> Unit)?,
    trailingIcon: @Composable (() -> Unit)?,
    shape: Shape,
    colors: GradientChipColors,
    elevation: ChipElevation?,
    border: BorderStroke?,
    minHeight: Dp,
    paddingValues: PaddingValues,
    interactionSource: MutableInteractionSource?,
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    Surface(
        onClick = onClick,
        modifier = modifier
            .semantics { role = Role.Button },
        enabled = enabled,
        shape = shape,
        tonalElevation = elevation?.elevation
            ?: 0.dp, // this is not 100% reflective of what Material does under the hood.
        border = border,
        interactionSource = interactionSource,
        color = Color.Transparent,
    ) {
        ChipContent(
            label = label,
            labelTextStyle = labelTextStyle,
            labelColor = labelColor,
            leadingIcon = leadingIcon,
            avatar = null,
            trailingIcon = trailingIcon,
            leadingIconColor = colors.leadingIconContentColor(enabled),
            trailingIconColor = colors.trailingIconContentColor(enabled),
            minHeight = minHeight,
            paddingValues = paddingValues,
            modifier = Modifier.graphicsLayer(
                shadowElevation = (elevation?.elevation ?: 0.dp).dpToPx(),
                shape = shape,
                clip = false,
            )
                .background(colors.containerColor(enabled), shape)
                .clip(shape),
        )
    }
}

@Composable
private fun ChipContent(
    label: @Composable () -> Unit,
    labelTextStyle: TextStyle,
    labelColor: Color,
    leadingIcon: @Composable (() -> Unit)?,
    avatar: @Composable (() -> Unit)?,
    trailingIcon: @Composable (() -> Unit)?,
    leadingIconColor: Color,
    trailingIconColor: Color,
    minHeight: Dp,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(
        LocalContentColor provides labelColor,
        LocalTextStyle provides labelTextStyle,
    ) {
        Layout(
            modifier = modifier
                .defaultMinSize(minHeight = minHeight)
                .padding(paddingValues),
            content = {
                if (avatar != null || leadingIcon != null) {
                    Box(
                        modifier = Modifier.layoutId(LeadingIconLayoutId),
                        contentAlignment = Alignment.Center,
                        content = {
                            if (avatar != null) {
                                avatar()
                            } else if (leadingIcon != null) {
                                CompositionLocalProvider(
                                    LocalContentColor provides leadingIconColor,
                                    content = leadingIcon,
                                )
                            }
                        },
                    )
                }
                Row(
                    modifier =
                    Modifier
                        .layoutId(LabelLayoutId)
                        .padding(HorizontalElementsPadding, 0.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                    content = { label() },
                )
                if (trailingIcon != null) {
                    Box(
                        modifier = Modifier.layoutId(TrailingIconLayoutId),
                        contentAlignment = Alignment.Center,
                        content = {
                            CompositionLocalProvider(
                                LocalContentColor provides trailingIconColor,
                                content = trailingIcon,
                            )
                        },
                    )
                }
            },
            measurePolicy = remember { ChipLayoutMeasurePolicy() },
        )
    }
}

private class ChipLayoutMeasurePolicy : MeasurePolicy {
    override fun MeasureScope.measure(
        measurables: List<Measurable>,
        constraints: Constraints,
    ): MeasureResult {
        val leadingIconPlaceable: Placeable? =
            measurables
                .fastFirstOrNull { it.layoutId == LeadingIconLayoutId }
                ?.measure(constraints.copy(minWidth = 0, minHeight = 0))
        val leadingIconWidth = leadingIconPlaceable.widthOrZero
        val leadingIconHeight = leadingIconPlaceable.heightOrZero

        val trailingIconPlaceable: Placeable? =
            measurables
                .fastFirstOrNull { it.layoutId == TrailingIconLayoutId }
                ?.measure(constraints.copy(minWidth = 0, minHeight = 0))
        val trailingIconWidth = trailingIconPlaceable.widthOrZero
        val trailingIconHeight = trailingIconPlaceable.heightOrZero

        val labelPlaceable =
            measurables
                .fastFirst { it.layoutId == LabelLayoutId }
                .measure(constraints.offset(horizontal = -(leadingIconWidth + trailingIconWidth)))

        val width = leadingIconWidth + labelPlaceable.width + trailingIconWidth
        val height = maxOf(leadingIconHeight, labelPlaceable.height, trailingIconHeight)

        return layout(width, height) {
            leadingIconPlaceable?.placeRelative(
                0,
                Alignment.CenterVertically.align(leadingIconHeight, height),
            )
            labelPlaceable.placeRelative(leadingIconWidth, 0)
            trailingIconPlaceable?.placeRelative(
                leadingIconWidth + labelPlaceable.width,
                Alignment.CenterVertically.align(trailingIconHeight, height),
            )
        }
    }

    override fun IntrinsicMeasureScope.minIntrinsicHeight(
        measurables: List<IntrinsicMeasurable>,
        width: Int,
    ): Int = measurables.fastMaxOfOrNull { it.minIntrinsicHeight(width) } ?: 0

    override fun IntrinsicMeasureScope.maxIntrinsicHeight(
        measurables: List<IntrinsicMeasurable>,
        width: Int,
    ): Int = measurables.fastMaxOfOrNull { it.maxIntrinsicHeight(width) } ?: 0

    override fun IntrinsicMeasureScope.minIntrinsicWidth(
        measurables: List<IntrinsicMeasurable>,
        height: Int,
    ): Int = measurables.fastSumBy { it.minIntrinsicWidth(height) }

    override fun IntrinsicMeasureScope.maxIntrinsicWidth(
        measurables: List<IntrinsicMeasurable>,
        height: Int,
    ): Int = measurables.fastSumBy { it.maxIntrinsicWidth(height) }
}

private val HorizontalElementsPadding = 8.dp

/** Returns the [PaddingValues] for the assist chip. */
private val AssistChipPadding = PaddingValues(horizontal = HorizontalElementsPadding)

private const val LeadingIconLayoutId = "leadingIcon"
private const val LabelLayoutId = "label"
private const val TrailingIconLayoutId = "trailingIcon"

internal val Placeable?.widthOrZero: Int
    get() = this?.width ?: 0

internal val Placeable?.heightOrZero: Int
    get() = this?.height ?: 0

@Immutable
class GradientChipColors(
    val containerColor: Brush,
    val labelColor: Color,
    val leadingIconContentColor: Color,
    val trailingIconContentColor: Color,
    val disabledContainerColor: Brush,
    val disabledLabelColor: Color,
    val disabledLeadingIconContentColor: Color,
    val disabledTrailingIconContentColor: Color,
    // TODO(b/113855296): Support other states: hover, focus, drag
) {
    /**
     * Returns a copy of this ChipColors, optionally overriding some of the values. This uses the
     * Color.Unspecified to mean “use the value from the source”
     */
    fun copy(
        containerColor: Brush = this.containerColor,
        labelColor: Color = this.labelColor,
        leadingIconContentColor: Color = this.leadingIconContentColor,
        trailingIconContentColor: Color = this.trailingIconContentColor,
        disabledContainerColor: Brush = this.disabledContainerColor,
        disabledLabelColor: Color = this.disabledLabelColor,
        disabledLeadingIconContentColor: Color = this.disabledLeadingIconContentColor,
        disabledTrailingIconContentColor: Color = this.disabledTrailingIconContentColor,
    ) =
        GradientChipColors(
            containerColor,
            labelColor.takeOrElse { this.labelColor },
            leadingIconContentColor.takeOrElse { this.leadingIconContentColor },
            trailingIconContentColor.takeOrElse { this.trailingIconContentColor },
            disabledContainerColor,
            disabledLabelColor.takeOrElse { this.disabledLabelColor },
            disabledLeadingIconContentColor.takeOrElse { this.disabledLeadingIconContentColor },
            disabledTrailingIconContentColor.takeOrElse { this.disabledTrailingIconContentColor },
        )

    /**
     * Represents the container color for this chip, depending on [enabled].
     *
     * @param enabled whether the chip is enabled
     */
    @Stable
    internal fun containerColor(enabled: Boolean): Brush =
        if (enabled) containerColor else disabledContainerColor

    /**
     * Represents the label color for this chip, depending on [enabled].
     *
     * @param enabled whether the chip is enabled
     */
    @Stable
    internal fun labelColor(enabled: Boolean): Color =
        if (enabled) labelColor else disabledLabelColor

    /**
     * Represents the leading icon's content color for this chip, depending on [enabled].
     *
     * @param enabled whether the chip is enabled
     */
    @Stable
    internal fun leadingIconContentColor(enabled: Boolean): Color =
        if (enabled) leadingIconContentColor else disabledLeadingIconContentColor

    /**
     * Represents the trailing icon's content color for this chip, depending on [enabled].
     *
     * @param enabled whether the chip is enabled
     */
    @Stable
    internal fun trailingIconContentColor(enabled: Boolean): Color =
        if (enabled) trailingIconContentColor else disabledTrailingIconContentColor

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is ChipColors) return false

        if (containerColor.hashCode() != other.containerColor.hashCode()) return false
        if (labelColor != other.labelColor) return false
        if (leadingIconContentColor != other.leadingIconContentColor) return false
        if (trailingIconContentColor != other.trailingIconContentColor) return false
        if (disabledContainerColor.hashCode() != other.disabledContainerColor.hashCode()) return false
        if (disabledLabelColor != other.disabledLabelColor) return false
        if (disabledLeadingIconContentColor != other.disabledLeadingIconContentColor) return false
        if (disabledTrailingIconContentColor != other.disabledTrailingIconContentColor) return false

        return true
    }

    override fun hashCode(): Int {
        var result = containerColor.hashCode()
        result = 31 * result + labelColor.hashCode()
        result = 31 * result + leadingIconContentColor.hashCode()
        result = 31 * result + trailingIconContentColor.hashCode()
        result = 31 * result + disabledContainerColor.hashCode()
        result = 31 * result + disabledLabelColor.hashCode()
        result = 31 * result + disabledLeadingIconContentColor.hashCode()
        result = 31 * result + disabledTrailingIconContentColor.hashCode()

        return result
    }
}
