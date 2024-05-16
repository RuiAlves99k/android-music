package com.appsrui.music.mediaPlayer

import android.app.Application
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.REPEAT_MODE_ALL
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.appsrui.music.model.Song
import com.appsrui.music.service.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MusicMediaPlayer(private val app: Application) :
    MediaPlayer {

    private lateinit var mediaControllerFuture: ListenableFuture<MediaController>
    private val mediaController: MediaController?
        get() = when (this::mediaControllerFuture.isInitialized && mediaControllerFuture.isDone && !mediaControllerFuture.isCancelled) {
            true -> mediaControllerFuture.get()
            else -> null
        }

    private val _mediaPlayerState = MutableStateFlow(MediaPlayerState())
    private fun updateMediaPlayerState(update: MediaPlayerState.() -> MediaPlayerState) {
        _mediaPlayerState.value = _mediaPlayerState.value.update()
    }

    private var progressUpdateJob: Job? = null
    private var updateTimeMillis: Long = 1000
    private var onSetup: (() -> Unit)? = null
    private var playlist: List<Song> = listOf()


    private fun setupPlayer() {
        val player = mediaController ?: return
        if (player.mediaItemCount == 0) {
            val mediaItems = playlist.map { song ->
                val mediaUri = when (song.source.startsWith("http")) {
                    true -> song.source.toUri()
                    else -> {
                        val context = app
                        val packageName = context.packageName
                        Uri.Builder()
                            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                            .authority(packageName)
                            .appendPath(
                                context.resources.getIdentifier(song.source, "raw", packageName)
                                    .toString()
                            )
                            .build()
                    }
                }
                MediaItem.Builder()
                    .setMediaId(song.id.toString())
                    .setUri(mediaUri)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(song.title)
                            .setArtist(song.artist)
                            .setArtworkUri(song.thumb.toUri())
                            .setExtras(
                                Bundle().apply {
                                    this.putInt("id", song.id)
                                }
                            )
                            .build()
                    )
                    .build()
            }
            player.setMediaItems(mediaItems)
        }
        updateMediaPlayerState {
            copy(playList = this@MusicMediaPlayer.playlist)
        }

        if (player.playbackState == Player.STATE_IDLE) {
            player.prepare()
        }

        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updateMediaPlayerState {
                    copy(currentSong = playList.find { it.id.toString() == mediaItem?.mediaId })
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updateMediaPlayerState {
                    copy(
                        isPlaying = isPlaying
                    )
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                updateMediaPlayerState {
                    val error = when (playbackState != Player.STATE_IDLE && error != null) {
                        true -> null
                        else -> error
                    }
                    copy(isBuffering = playbackState == Player.STATE_BUFFERING, error = error)
                }
            }

            override fun onIsLoadingChanged(isLoading: Boolean) {
                updateMediaPlayerState {
                    copy(
                        isBuffering = true
                    )
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                updateMediaPlayerState {
                    copy(error = error)
                }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                updateMediaPlayerState {
                    copy(
                        currentPosition = newPosition.positionMs
                    )
                }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                updateMediaPlayerState {
                    copy(
                        repeatMode = repeatMode
                    )
                }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                updateMediaPlayerState {
                    copy(
                        isShuffleModeActive = shuffleModeEnabled
                    )
                }
            }
        })
        onSetup?.invoke()
        setupProgressUpdateJob()
    }

    private fun setupProgressUpdateJob() {
        progressUpdateJob?.cancel()
        progressUpdateJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                mediaController?.run {
                    updateMediaPlayerState {
                        copy(
                            currentPosition = mediaController!!.currentPosition,
                            bufferedPosition = mediaController!!.bufferedPosition,
                        )
                    }
                }
                delay(updateTimeMillis)
            }
        }
    }

    override fun selectSong(song: Song) {
        mediaController?.run {
            for (index in 0 until mediaItemCount) {
                if (getMediaItemAt(index).mediaId == song.id.toString()) {
                    if (index != currentMediaItemIndex) {
                        seekTo(index, 0)
                        break
                    }
                }
            }
            if (playbackState == Player.STATE_IDLE) {
                prepare()
            }
            play()
        }
    }

    override fun onPlayPause() {
        mediaController?.run {
            if (isPlaying) {
                pause()
            } else {
                if (playbackState == Player.STATE_IDLE) {
                    prepare()
                }
                play()
            }
        }
    }

    override fun onSkipPrevious() {
        mediaController?.run {
            if (playbackState == Player.STATE_IDLE) {
                prepare()
            }
            seekToPreviousMediaItem()
        }
    }

    override fun onSkipNext() {
        if (mediaController?.playbackState == Player.STATE_IDLE) {
            mediaController?.prepare()
        }
        mediaController?.seekToNextMediaItem()
    }

    override fun onSeek(seconds: Float) {
        if (mediaController != null && mediaController!!.duration != C.TIME_UNSET) {
            mediaController?.seekTo(TimeUnit.SECONDS.toMillis(seconds.toLong()))
        }
    }

    override fun onStart(onSetup: () -> Unit) {
        this.onSetup = onSetup
        val context: Context = app
        setupPlayer()
        mediaControllerFuture = MediaController.Builder(
            context,
            SessionToken(context, ComponentName(context, PlaybackService::class.java))
        ).buildAsync()
        mediaControllerFuture.addListener({
            setupPlayer()
        }, ContextCompat.getMainExecutor(context))
    }

    override fun onStop() {
        MediaController.releaseFuture(mediaControllerFuture)
        progressUpdateJob?.cancel()
    }

    override fun changeRepeatMode() {
        mediaController?.run {
            repeatMode = when (repeatMode) {
                REPEAT_MODE_OFF -> REPEAT_MODE_ALL
                REPEAT_MODE_ALL -> REPEAT_MODE_ONE
                else -> REPEAT_MODE_OFF
            }
        }
    }

    override fun changeShuffleMode() {
        mediaController?.run {
            shuffleModeEnabled = !shuffleModeEnabled
        }
    }

    override fun getPlayerState(): StateFlow<MediaPlayerState> = _mediaPlayerState

    override fun changeUpdateTime(millis: Long) {
        updateTimeMillis = millis
    }

    override fun setPlaylist(playlist: List<Song>) {
        mediaController?.clearMediaItems()
        this.playlist = playlist
        setupPlayer()
    }

}