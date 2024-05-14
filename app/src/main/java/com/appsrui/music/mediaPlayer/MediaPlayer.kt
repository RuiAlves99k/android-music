package com.appsrui.music.mediaPlayer

import com.appsrui.music.model.Song
import kotlinx.coroutines.flow.StateFlow

interface MediaPlayer {
    fun selectSong(song: Song)
    fun onPlayPause()
    fun onSkipPrevious()
    fun onSkipNext()
    fun onSeek(seconds: Float)
    fun onStart(onSetup: () -> Unit = {})
    fun setPlaylist(playlist: List<Song>)
    fun onStop()
    fun changeRepeatMode()
    fun changeShuffleMode()
    fun getPlayerState(): StateFlow<MediaPlayerState>
    fun changeUpdateTime(millis: Long)
}