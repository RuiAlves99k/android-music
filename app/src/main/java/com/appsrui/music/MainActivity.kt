package com.appsrui.music

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.lifecycle.lifecycleScope
import com.appsrui.music.model.Song
import com.appsrui.music.ui.PlayerScreen
import com.appsrui.music.ui.theme.MusicTheme
import com.appsrui.music.widget.MusicWidget
import com.appsrui.music.widget.MusicWidgetStateHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<PlayerScreenViewModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    val playerScreenState by viewModel.playerScreenState.collectAsState()
                    PlayerScreen(playerScreenState = playerScreenState)
//                    updateWidgetInformation(
//                        playerScreenState.currentSong,
//                        playerScreenState.currentPosition
//                    )
                }
            }
        }
    }

    private fun updateWidgetInformation(song: Song?, currentPosition: Long?) {
        if (song == null || currentPosition == null) return
        val context = baseContext
        lifecycleScope.launch {
            val glanceId = GlanceAppWidgetManager(context).getGlanceIds(MusicWidget::class.java)
                .last()
            MusicWidget().apply {
                updateAppWidgetState(context = context, glanceId = glanceId) { prefs ->
                    MusicWidgetStateHelper.save(prefs, song, currentPosition)
                }
                update(context, glanceId)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.onStart()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onStop()
    }
}