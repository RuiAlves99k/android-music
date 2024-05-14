package com.appsrui.music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appsrui.music.mediaPlayer.MediaPlayer
import com.appsrui.music.model.SongList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerScreenViewModel @Inject constructor(
    private val mediaPlayer: MediaPlayer,
) : ViewModel() {
    private val _playerScreenState = MutableStateFlow(PlayerScreenState())
    val playerScreenState: StateFlow<PlayerScreenState> = _playerScreenState

    private fun updatePlayerScreenState(update: PlayerScreenState.() -> PlayerScreenState) {
        _playerScreenState.value = _playerScreenState.value.update()
    }

    init {
        mediaPlayer.setPlaylist(playlist = SongList)
    }

    private fun listenMediaPlayerState() {
        viewModelScope.launch {
            mediaPlayer.getPlayerState().collectLatest { mediaPlayerState ->
                updatePlayerScreenState {
                    copy(
                        isPlaying = mediaPlayerState.isPlaying,
                        isBuffering = mediaPlayerState.isBuffering,
                        currentPosition = mediaPlayerState.currentPosition,
                        bufferedPosition = mediaPlayerState.bufferedPosition,
                        playlist = mediaPlayerState.playList,
                        currentSong = mediaPlayerState.currentSong,
                        error = mediaPlayerState.error,
                        repeatMode = mediaPlayerState.repeatMode,
                        isShuffleModeActive = mediaPlayerState.isShuffleModeActive
                    )
                }
            }

        }
    }

    private fun setInitialPlayerScreenState() {
        updatePlayerScreenState {
            copy(
                onSongClick = { song ->
                    mediaPlayer.selectSong(song)
                },
                onPlayPause = {
                    mediaPlayer.onPlayPause()
                },
                onSkipPrevious = {
                    mediaPlayer.onSkipPrevious()
                },
                onSkipNext = {
                    mediaPlayer.onSkipNext()
                },
                onSeek = { seconds ->
                    mediaPlayer.onSeek(seconds)
                },
                onChangeRepeatMode = {
                    mediaPlayer.changeRepeatMode()
                },
                onChangeShuffleMode = {
                    mediaPlayer.changeShuffleMode()
                },
            )
        }
    }


    fun onStart() {
        mediaPlayer.onStart(onSetup = {
            setInitialPlayerScreenState()
            listenMediaPlayerState()
        })
    }

    fun onStop() {
        mediaPlayer.onStop()
    }
}