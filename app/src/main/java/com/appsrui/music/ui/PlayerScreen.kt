package com.appsrui.music.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.media3.common.Player.REPEAT_MODE_OFF
import com.appsrui.music.PlayerScreenState
import com.appsrui.music.R
import com.appsrui.music.model.Song
import com.appsrui.music.model.SongList
import com.appsrui.music.ui.theme.MusicTheme

@Composable
fun PlayerScreen(playerScreenState: PlayerScreenState, modifier: Modifier = Modifier) {
    PlayerScreen(
        modifier = modifier,
        isPlaying = playerScreenState.isPlaying,
        isBuffering = playerScreenState.isBuffering,
        currentProgress = playerScreenState.currentPosition,
        bufferedProgress = playerScreenState.bufferedPosition,
        nowPlaying = playerScreenState.currentSong,
        playlist = playerScreenState.playlist,
        error = playerScreenState.error,
        onPlayPause = playerScreenState.onPlayPause,
        onSkipPrevious = playerScreenState.onSkipPrevious,
        onSkipNext = playerScreenState.onSkipNext,
        onSeek = playerScreenState.onSeek,
        onSongClick = playerScreenState.onSongClick,
        repeatMode = playerScreenState.repeatMode,
        isShuffleModeActive = playerScreenState.isShuffleModeActive,
        onChangeShuffleMode = playerScreenState.onChangeShuffleMode,
        onChangeRepeatMode = playerScreenState.onChangeRepeatMode,
    )
}

@Composable
private fun PlayerScreen(
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    isBuffering: Boolean = false,
    currentProgress: Long = 0L,
    bufferedProgress: Long = 0L,
    nowPlaying: Song? = null,
    playlist: List<Song> = emptyList(),
    error: Exception? = null,
    onPlayPause: () -> Unit = {},
    onSkipPrevious: () -> Unit = {},
    onSkipNext: () -> Unit = {},
    onSeek: (seconds: Float) -> Unit = {},
    onSongClick: (Song) -> Unit = {},
    repeatMode: Int = REPEAT_MODE_OFF,
    isShuffleModeActive: Boolean = false,
    onChangeRepeatMode: () -> Unit = {},
    onChangeShuffleMode: () -> Unit = {},
) {
    ConstraintLayout(modifier = modifier.fillMaxSize()) {
        val (header, playlistRef, controls) = createRefs()
        Text(
            text = stringResource(id = R.string.app_name),
            fontSize = 32.sp,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary)
                .padding(8.dp)
                .constrainAs(header) {
                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )
        if (playlist.isEmpty() && error == null) {
            CircularProgressIndicator()
        } else {
            Playlist(
                playlist = playlist,
                onSongClick = onSongClick,
                nowPlaying = nowPlaying?.id.toString(),
                modifier = Modifier.constrainAs(playlistRef) {
                    height = Dimension.fillToConstraints
                    start.linkTo(parent.start)
                    top.linkTo(header.bottom)
                    end.linkTo(parent.end)
                    bottom.linkTo(controls.top)
                }
            )
        }
        PlaybackControls(
            song = nowPlaying,
            isPlaying = isPlaying,
            isBuffering = isBuffering,
            currentProgress = currentProgress,
            bufferedProgress = bufferedProgress,
            shuffleMode = isShuffleModeActive,
            repeatMode = repeatMode,
            error = error,
            onPlayPause = onPlayPause,
            onSkipPrevious = onSkipPrevious,
            onSkipNext = onSkipNext,
            onSeek = onSeek,
            onChangeShuffleMode = onChangeShuffleMode,
            onChangeRepeatMode = onChangeRepeatMode,
            modifier = Modifier.constrainAs(controls) {
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MusicTheme {
        PlayerScreen(
            playlist = SongList,
            nowPlaying = SongList[0],
            isPlaying = true,
            isBuffering = true,
            repeatMode = 1,
            currentProgress = 50_000L,
            bufferedProgress = 50_000L,
            error = Exception()
        )
    }
}