package com.appsrui.music.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.appsrui.music.mediaPlayer.MediaPlayer
import javax.inject.Inject

class MusicWidgetActions: ActionCallback {
    companion object {
        val onPlayPauseKey = "widgetActionPlayPause"
        val onSkipPreviousKey = "widgetSkipPrevious"
        val onSkipNextKey = "widgetSkipNext"
    }

    @Inject
    lateinit var mediaPlayer: MediaPlayer

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        when (parameters[ActionParameters.Key("action")]) {
            onPlayPauseKey -> mediaPlayer.onPlayPause()
            onSkipPreviousKey -> mediaPlayer.onSkipPrevious()
            onSkipNextKey -> mediaPlayer.onSkipNext()
        }
    }
}