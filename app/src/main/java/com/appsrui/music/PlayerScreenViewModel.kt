package com.appsrui.music

import androidx.lifecycle.ViewModel
import com.appsrui.music.model.SongList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PlayerScreenViewModel : ViewModel() {
    private val _playerScreenState = MutableStateFlow(PlayerScreenState())
    val playerScreenState: StateFlow<PlayerScreenState> = _playerScreenState

    private fun updatePlayerScreenState(update: PlayerScreenState.() -> PlayerScreenState) {
        _playerScreenState.value = _playerScreenState.value.update()
    }

    fun onStart() {
        updatePlayerScreenState {
            copy(playlist = SongList)
        }
    }

    fun onStop() {

    }
}