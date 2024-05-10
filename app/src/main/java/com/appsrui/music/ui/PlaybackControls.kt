package com.appsrui.music.ui

import android.media.session.MediaController.TransportControls
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderPositions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player.REPEAT_MODE_ALL
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.Player.RepeatMode
import com.appsrui.music.R
import com.appsrui.music.model.Song
import com.appsrui.music.model.SongList
import com.appsrui.music.ui.theme.MusicTheme
import com.appsrui.music.ui.theme.toFormatterDuration
import java.lang.Exception
import java.util.concurrent.TimeUnit

@Composable
fun PlaybackControls(
    song: Song?,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    isBuffering: Boolean = false,
    currentProgress: Long = 0L,
    bufferedProgress: Long = 0L,
    error: Exception? = null,
    repeatMode: Int = REPEAT_MODE_OFF,
    shuffleMode: Boolean = false,
    onSeek: (seconds: Float) -> Unit = {},
    onPlayPause: () -> Unit = {},
    onSkipPrevious: () -> Unit = {},
    onSkipNext: () -> Unit = {},
    onChangeRepeatMode: () -> Unit = {},
    onChangeShuffleMode: () -> Unit = {},
) {
    Card(
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Crossfade(targetState = song, label = "NowPlaying") { song ->
                if (song != null) {
                    NowPlaying(song = song, error = error)
                }
            }
            PlayerSlider(
                song = song,
                isBuffering = isBuffering,
                currentProgress = currentProgress,
                bufferedProgress = bufferedProgress,
                onSeek = onSeek,
            )
            Spacer(
                modifier = Modifier.height(8.dp)
            )
            TransportControlButtons(
                isPlaying = isPlaying,
                repeatMode = repeatMode,
                shuffleMode = shuffleMode,
                onPlayPause = onPlayPause,
                onSkipPrevious = onSkipPrevious,
                onSkipNext = onSkipNext,
                onChangeRepeatMode = onChangeRepeatMode,
                onChangeShuffleMode = onChangeShuffleMode
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlayerSlider(
    song: Song?,
    isBuffering: Boolean,
    currentProgress: Long,
    bufferedProgress: Long,
    onSeek: (seconds: Float) -> Unit,
) {
    Column {
        if (isBuffering) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.End)
            )
        } else {
            Spacer(modifier = Modifier.size(16.dp))
        }
        var sliderState by remember {
            mutableStateOf(TimeUnit.MILLISECONDS.toSeconds(currentProgress).toFloat())
        }
        val interactionSource = remember {
            MutableInteractionSource()
        }
        val isSeeking by interactionSource.collectIsDraggedAsState()
        val sliderValue = when (isSeeking) {
            true -> sliderState
            else -> TimeUnit.MILLISECONDS.toSeconds(currentProgress).toFloat()
        }
        Slider(value = sliderValue,
            interactionSource = interactionSource,
            valueRange = 0f..(song?.durationSeconds?.toFloat() ?: 0f),
            onValueChange = { sliderState = it },
            onValueChangeFinished = { onSeek(sliderState) },
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(),
            track = { sliderPositions ->
                Box {
                    val bufferedSeconds = (TimeUnit.MILLISECONDS.toSeconds(
                        bufferedProgress
                    ).toFloat()) / (song?.durationSeconds ?: 1)
                    SliderDefaults.Track(
                        colors = SliderDefaults.colors(),
                        enabled = false,
                        sliderPositions = SliderPositions(0f..bufferedSeconds),
                    )
                    SliderDefaults.Track(
                        colors = SliderDefaults.colors(inactiveTrackColor = Color.Transparent),
                        enabled = true,
                        sliderPositions = sliderPositions,
                    )
                }
            }
        )
        Row {
            Text(
                text = sliderValue.toFormatterDuration(),
                modifier = Modifier.fillMaxWidth(0.5f),
                textAlign = TextAlign.Start
            )
            Text(
                text = (song?.durationSeconds?.toLong() ?: 0L).toFormatterDuration(),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
            )
        }
    }
}

@Composable
private fun TransportControlButtons(
    isPlaying: Boolean,
    shuffleMode: Boolean,
    repeatMode: Int,
    onPlayPause: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSkipNext: () -> Unit,
    onChangeShuffleMode: () -> Unit,
    onChangeRepeatMode: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onChangeRepeatMode,
            modifier = Modifier.size(32.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            val (repeatModeIconId, repeatModeContentDescription) = when (repeatMode) {
                REPEAT_MODE_ALL -> R.drawable.icon_repeat to R.string.repeat_mode_action_text
                REPEAT_MODE_ONE -> R.drawable.icon_repeat_one to R.string.repeat_mode_action_text
                else -> R.drawable.icon_repeat_off to R.string.repeat_mode_action_text
            }
            Icon(
                painter = painterResource(id = repeatModeIconId),
                contentDescription = stringResource(id = repeatModeContentDescription)
            )
        }
        Button(
            onClick = onSkipPrevious,
            modifier = Modifier.size(48.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icon_skip_previous),
                contentDescription = stringResource(id = R.string.skip_previous_action_text)
            )
        }
        Button(
            onClick = onPlayPause,
            modifier = Modifier.size(60.dp),
            contentPadding = PaddingValues(8.dp),
        ) {
            Crossfade(targetState = isPlaying, label = "PlayPauseButton") { isPlaying ->
                val (playPauseIconId, playPauseContentDescription) = when (isPlaying) {
                    true -> R.drawable.icon_pause to R.string.pause_action_text
                    else -> R.drawable.icon_play to R.string.play_action_text
                }
                Icon(
                    painter = painterResource(id = playPauseIconId),
                    contentDescription = stringResource(id = playPauseContentDescription),
                    modifier = Modifier.size(36.dp),
                )
            }
        }
        Button(
            onClick = onSkipNext,
            modifier = Modifier.size(48.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.icon_skip_next),
                contentDescription = stringResource(id = R.string.skip_next_action_text)
            )
        }
        Button(
            onClick = onChangeShuffleMode,
            modifier = Modifier.size(32.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            val (shuffleIconId, shuffleContentDescription) = when (shuffleMode) {
                true -> R.drawable.icon_shuffle to R.string.shuffle_action_text
                else -> R.drawable.icon_shuffle_off to R.string.shuffle_action_text
            }
            Icon(
                painter = painterResource(id = shuffleIconId),
                contentDescription = stringResource(id = shuffleContentDescription)
            )
        }
    }
}

@Preview
@Composable
fun PlaybackControlsPreview() {
    MusicTheme {
        PlaybackControls(
            song = SongList[0],
            isBuffering = true,
            isPlaying = false
        )
    }
}