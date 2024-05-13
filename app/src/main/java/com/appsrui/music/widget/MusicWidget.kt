package com.appsrui.music.widget

import android.content.Context
import android.os.Build
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.glance.Button
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
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
import androidx.glance.layout.size
import androidx.glance.text.Text
import coil.compose.AsyncImage
import com.appsrui.music.R
import com.appsrui.music.ui.theme.GlanceColorScheme

/**
 * Implementation of App Widget functionality.
 */
class MusicWidget : GlanceAppWidget() {
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
            modifier = GlanceModifier.fillMaxSize()
                .then(
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
                ).appWidgetBackground(),
            contentAlignment = Alignment.Center,
        ) {
            when (state.song.id) {
                Int.MIN_VALUE -> InitialView()
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
            Text(text = "Loading data...Compose")
        }
    }

    @Composable
    fun WidgetBody(state: MusicWidgetState) {
        Row(modifier = GlanceModifier.fillMaxSize()) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                AsyncImage(
                    model = state.song.thumb.takeIf { it.isNotBlank() },
                    fallback = painterResource(id = R.drawable.icon_music_note),
                    placeholder = painterResource(id = R.drawable.icon_music_note),
                    contentDescription = stringResource(id = R.string.now_playing_album_cover_text),
                )
            }
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Text(text = state.song.title)
                Text(text = state.song.artist)
                Text(text = state.song.durationSeconds.toString())
                Slider(value = 0F, onValueChange = {})
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    Icon(
                        painter = painterResource(id = R.drawable.icon_skip_previous),
                        contentDescription = stringResource(
                            id = R.string.skip_previous_action_text
                        )
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.icon_play),
                        contentDescription = stringResource(
                            id = R.string.play_action_text
                        )
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.icon_skip_previous),
                        contentDescription = stringResource(
                            id = R.string.skip_next_action_text
                        )
                    )
                }
            }
        }
    }
}
