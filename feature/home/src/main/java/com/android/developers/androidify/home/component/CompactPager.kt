package com.android.developers.androidify.home.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onLayoutRectChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.android.developers.androidify.home.content.MainHomeContent
import com.android.developers.androidify.theme.components.AndroidifyTopAppBar

@Composable
internal fun CompactPager(
    videoLink: String?,
    dancingBotLink: String?,
    onClick: (IntOffset) -> Unit,
    onAboutClicked: () -> Unit,
) {
    val pagerState: PagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 2 },
    )
    var buttonPosition by remember { mutableStateOf(IntOffset.Zero) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AndroidifyTopAppBar(
            aboutEnabled = true,
            onAboutClicked = onAboutClicked,
        )
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(.8f),
            beyondViewportPageCount = 1,
        ) { page ->
            if (page == 0) {
                MainHomeContent(
                    dancingBotLink = dancingBotLink,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .align(Alignment.CenterHorizontally),
                )
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    VideoPlayerRotatedCard(
                        videoLink = videoLink,
                        modifier = Modifier
                            .padding(horizontal = 32.dp)
                            .align(Alignment.Center),
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .weight(.1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(pagerState.pageCount) { iteration ->

                val isCurrent by remember { derivedStateOf { pagerState.currentPage == iteration } }
                val animatedColor by animateColorAsState(
                    targetValue = if (isCurrent) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onTertiary,
                    label = "animatedFirstColor",
                )

                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(RoundedCornerShape(size = 16.dp))
                        .animateContentSize()
                        .background(
                            color = animatedColor,
                            shape = RoundedCornerShape(size = 16.dp),
                        )
                        .height(16.dp)
                        .width(if (isCurrent) 40.dp else 16.dp),
                )
            }
        }
        Spacer(modifier = Modifier.size(12.dp))
        HomePageButton(
            modifier = Modifier
                .onLayoutRectChanged {
                    buttonPosition = it.boundsInWindow.center
                }
                .padding(bottom = 16.dp)
                .height(64.dp)
                .width(220.dp),
            onClick = { onClick(buttonPosition) },
        )
    }
}