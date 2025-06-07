package com.android.developers.androidify.home.component

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.android.developers.androidify.home.R
import com.android.developers.androidify.theme.Blue

@Preview
@Composable
internal fun HomePageButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val style = MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight(700),
        letterSpacing = .15f.sp,
    )

    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors().copy(containerColor = Blue),
    ) {
        Text(
            text = stringResource(R.string.home_button_label),
            style = style,
        )
    }
}