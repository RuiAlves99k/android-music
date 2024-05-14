package com.appsrui.music.mediaPlayer

import androidx.media3.common.Player
import com.appsrui.music.model.Song

data class MediaPlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPosition: Long = 0L,
    val bufferedPosition: Long = 0L,
    val currentSong: Song? = null,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val isShuffleModeActive: Boolean = false,
    val playList: List<Song> = emptyList(),
    val error: Exception? = null,
)