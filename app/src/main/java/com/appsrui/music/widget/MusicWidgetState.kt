package com.appsrui.music.widget

import com.appsrui.music.model.Song

data class MusicWidgetState(
    val song: Song? = null,
    val currentPosition: Long = 0L,
    val onPlayPause: () -> Unit = {},
    val onSkipPrevious: () -> Unit = {},
    val onSkipNext: () -> Unit = {},
)