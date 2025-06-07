package com.android.developers.androidify.home.component

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import com.android.developers.androidify.home.R
import com.android.developers.androidify.home.util.onVisibilityChanged

@Composable
internal fun VideoPlayerRotatedCard(
    videoLink: String?,
    modifier: Modifier = Modifier,
) {
    val aspectRatio: Float = 280f / 380f
    val videoInstructionText = stringResource(R.string.instruction_video_transcript)

    Box(
        modifier = modifier
            .focusable()
            .semantics { contentDescription = videoInstructionText }
            .aspectRatio(ratio = aspectRatio)
            .rotate(degrees = -3f)
            .shadow(
                elevation = 8.dp,
                shape = MaterialTheme.shapes.large,
            )
            .background(
                color = Color.White,
                shape = MaterialTheme.shapes.large,
            ),
    ) {
        VideoPlayer(
            videoLink = videoLink,
            modifier = Modifier
                .aspectRatio(aspectRatio)
                .align(Alignment.Center)
                .clip(MaterialTheme.shapes.large)
                .clipToBounds(),
        )
    }
}

@OptIn(UnstableApi::class) // New Media3 Compose artifact is currently experimental
@Composable
private fun VideoPlayer(
    videoLink: String?,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current) return // Layoutlib does not support ExoPlayer

    val context = LocalContext.current
    var player by remember { mutableStateOf<Player?>(null) }
    var videoFullyOnScreen by remember { mutableStateOf(false) }

    LifecycleStartEffect(videoLink) {
        if (videoLink != null) {
            player = ExoPlayer.Builder(context).build().apply {
                setMediaItem(MediaItem.fromUri(videoLink))
                repeatMode = Player.REPEAT_MODE_ONE
                prepare()
            }
        }
        onStopOrDispose {
            player?.release()
            player = null
        }
    }

    Box(
        Modifier
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .onVisibilityChanged(
                containerWidth = LocalView.current.width,
                containerHeight = LocalView.current.height,
            ) { fullyVisible ->
                videoFullyOnScreen = fullyVisible
            }
            .then(modifier),
    ) {
        player?.let { currentPlayer ->
            LaunchedEffect(videoFullyOnScreen) {
                if (videoFullyOnScreen) currentPlayer.play() else currentPlayer.pause()
            }

            // Render the video
            PlayerSurface(currentPlayer, surfaceType = SURFACE_TYPE_TEXTURE_VIEW)

            // Show a play / pause button
            val playPauseButtonState = rememberPlayPauseButtonState(currentPlayer)
            OutlinedIconButton(
                onClick = playPauseButtonState::onClick,
                enabled = playPauseButtonState.isEnabled,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                colors = IconButtonDefaults.outlinedIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
            ) {
                val icon =
                    if (playPauseButtonState.showPlay) R.drawable.rounded_play_arrow_24 else R.drawable.rounded_pause_24
                val contentDescription =
                    if (playPauseButtonState.showPlay) R.string.play else R.string.pause

                Icon(
                    painterResource(icon),
                    stringResource(contentDescription),
                )
            }
        }
    }
}