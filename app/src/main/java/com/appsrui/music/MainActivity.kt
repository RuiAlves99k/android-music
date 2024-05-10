package com.appsrui.music

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.appsrui.music.ui.PlayerScreen
import com.appsrui.music.ui.theme.MusicTheme

class MainActivity : ComponentActivity() {
    private val model by viewModels<PlayerScreenViewModel>{
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }

    private val player by lazy {  ExoPlayer.Builder(application).build() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background
                ) {
                    val playerScreenState by model.playerScreenState.collectAsState()
                    PlayerScreen(playerScreenState = playerScreenState)
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    override fun onStart() {
        super.onStart()
        model.onStart(player)
    }

    override fun onStop() {
        super.onStop()
        model.onStop()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MusicTheme {
        Greeting("Android")
    }
}