package com.appsrui.music.widget

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.action
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.Text
import coil.ImageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.appsrui.music.MainActivity
import com.appsrui.music.R
import com.appsrui.music.mediaPlayer.MediaPlayer
import com.appsrui.music.ui.theme.GlanceColorScheme
import javax.inject.Inject

/**
 * Implementation of App Widget functionality.
 */
class MusicWidget() : GlanceAppWidget() {

    @Inject
    lateinit var mediaPlayer: MediaPlayer

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme(GlanceColorScheme.colors) {
                MusicContent()
            }
        }
    }

    @Composable
    private fun MusicContent() {
        val state = MusicWidgetStateHelper.getState(prefs = currentState())
        Box(
            modifier = GlanceModifier.fillMaxSize().then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    GlanceModifier.background(GlanceTheme.colors.tertiaryContainer)
                        .cornerRadius(6.dp)
                } else {
                    GlanceModifier.background(
                        ImageProvider(
                            resId = R.drawable.shape_widget_small
                        )
                    )
                }
            ).appWidgetBackground().clickable(
                onClick = actionStartActivity(
                    activity = MainActivity::class.java,
                )
            ),
            contentAlignment = Alignment.Center,
        ) {
            when (state.song?.id) {
                null, Int.MIN_VALUE -> InitialView()
                else -> WidgetBody(state)
            }
        }
    }

    @Composable
    private fun InitialView() {
        Column(
            modifier = GlanceModifier.padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator()
            Spacer()
            Text(text = "Loading data...")
        }
    }

    @Composable
    fun WidgetBody(state: MusicWidgetState) {
        val context = LocalContext.current
        val song = state.song!!
        val imageUrl = song.thumb
        var songImage by remember(imageUrl) { mutableStateOf<Bitmap?>(null) }
        LaunchedEffect(imageUrl) {
            songImage = getImage(context = context, url = song.thumb)
        }
        Row(modifier = GlanceModifier.fillMaxSize()) {
            if (songImage != null) {
                Image(
                    provider = ImageProvider(songImage!!),
                    contentDescription = LocalContext.current.getString(R.string.now_playing_album_cover_text),
                )
            } else {
                CircularProgressIndicator()
            }
            Column(
                modifier = GlanceModifier.fillMaxSize().background(GlanceTheme.colors.secondary)
            ) {
                Text(text = song.title)
                Text(text = song.artist)
                Text(text = song.durationSeconds.toString())
//                Slider(value = 0F, onValueChange = {})
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    Image(
                        modifier = GlanceModifier.clickable(
                            onClick = action {
                                mediaPlayer.onSkipPrevious()
                            }
                        ),
                        provider = ImageProvider(resId = R.drawable.icon_skip_previous),
                        contentDescription =
                        LocalContext.current.getString(R.string.skip_previous_action_text)
                    )
                    Image(
                        modifier = GlanceModifier.clickable(
                            onClick = action {
                                mediaPlayer.onPlayPause()
                            }
                        ),
                        provider = ImageProvider(resId = R.drawable.icon_play),
                        contentDescription = LocalContext.current.getString(
                            R.string.play_action_text
                        )
                    )
                    Image(
                        modifier = GlanceModifier.clickable(
                            onClick = action {
                                mediaPlayer.onSkipNext()
                            }
                        ),
                        provider = ImageProvider(resId = R.drawable.icon_skip_previous),
                        contentDescription = LocalContext.current.getString(
                            R.string.skip_next_action_text
                        )
                    )
                }
            }
        }
    }

    private suspend fun getImage(context: Context, url: String): Bitmap? {
        val request = ImageRequest.Builder(context).data(url).build()
        return when (val result = ImageLoader(context).execute(request)) {
            is ErrorResult -> throw result.throwable
            is SuccessResult -> result.drawable.toBitmapOrNull()
        }
    }
}
