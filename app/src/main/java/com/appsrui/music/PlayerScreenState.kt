package com.appsrui.music

import com.appsrui.music.model.Song
import java.lang.Exception

data class PlayerScreenState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentPosition: Long = 0L,
    val bufferedPosition: Long = 0L,
    val playlist: List<Song> = emptyList(),
    val currentSong: Song? = null,
    val onPlayPause: () -> Unit = {},
    val onSkipPrevious: () -> Unit = {},
    val onSkipNext: () -> Unit = {},
    val onSeek: (Float) -> Unit = {},
    val onSongClick: (Song) -> Unit = {},
    val error: Exception? = null,
)