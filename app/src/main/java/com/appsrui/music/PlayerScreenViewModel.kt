package com.appsrui.music

import android.app.Application
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
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
import com.appsrui.music.model.SongList
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class PlayerScreenViewModel(app: Application) : AndroidViewModel(app) {
    private val _playerScreenState = MutableStateFlow(PlayerScreenState())
    val playerScreenState: StateFlow<PlayerScreenState> = _playerScreenState

    private lateinit var mediaControllerFuture: ListenableFuture<MediaController>
    private val mediaController: MediaController?
        get() = when (mediaControllerFuture.isDone && !mediaControllerFuture.isCancelled) {
            true -> mediaControllerFuture.get()
            else -> null
        }

    private fun updatePlayerScreenState(update: PlayerScreenState.() -> PlayerScreenState) {
        _playerScreenState.value = _playerScreenState.value.update()
    }

    private var progressUpdateJob: Job? = null

    private fun setupPlayer() {
        val player = mediaController ?: return
        if (player.mediaItemCount == 0) {
            val mediaItems = SongList.map { song ->
                val mediaUri = when (song.source.startsWith("http")) {
                    true -> song.source.toUri()
                    else -> {
                        val context = getApplication<Application>()
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

        if (player.playbackState == Player.STATE_IDLE) {
            player.prepare()
        }

        player.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                updatePlayerScreenState {
                    copy(currentSong = SongList.find { it.id.toString() == mediaItem?.mediaId })
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlayerScreenState {
                    copy(
                        isPlaying = isPlaying
                    )
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                updatePlayerScreenState {
                    val error = when (playbackState != Player.STATE_IDLE && error != null) {
                        true -> null
                        else -> error
                    }
                    copy(isBuffering = playbackState == Player.STATE_BUFFERING, error = error)
                }
            }

            override fun onIsLoadingChanged(isLoading: Boolean) {
                updatePlayerScreenState {
                    copy(
                        isBuffering = true
                    )
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                updatePlayerScreenState {
                    copy(error = error)
                }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                updatePlayerScreenState {
                    copy(
                        currentPosition = newPosition.positionMs
                    )
                }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                updatePlayerScreenState {
                    copy(
                        repeatMode = repeatMode
                    )
                }
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                updatePlayerScreenState {
                    copy(
                        isShuffleModeActive = shuffleModeEnabled
                    )
                }
            }

        })

        setInitialPlayerScreenState(player)
        setupProgressUpdateJob(player)
    }

    private fun setInitialPlayerScreenState(player: Player) {
        updatePlayerScreenState {
            copy(
                onSongClick = { song ->
                    for (index in 0 until player.mediaItemCount) {
                        if (player.getMediaItemAt(index).mediaId == song.id.toString()) {
                            if (index != player.currentMediaItemIndex) {
                                player.seekTo(index, 0)
                                break
                            }
                        }
                    }
                    if (player.playbackState == Player.STATE_IDLE) {
                        player.prepare()
                    }
                    player.play()
                },
                onPlayPause = {
                    if (player.isPlaying) {
                        player.pause()
                    } else {
                        if (player.playbackState == Player.STATE_IDLE) {
                            player.prepare()
                        }
                        player.play()
                    }
                },
                onSkipPrevious = {
                    if (player.playbackState == Player.STATE_IDLE) {
                        player.prepare()
                    }
                    player.seekToPreviousMediaItem()
                },
                onSkipNext = {
                    if (player.playbackState == Player.STATE_IDLE) {
                        player.prepare()
                    }
                    player.seekToNextMediaItem()
                },
                onSeek = { seconds ->
                    if (player.duration != C.TIME_UNSET) {
                        player.seekTo(TimeUnit.SECONDS.toMillis(seconds.toLong()))
                    }
                },
                onChangeRepeatMode = {
                    player.repeatMode = when (player.repeatMode) {
                        REPEAT_MODE_OFF -> REPEAT_MODE_ALL
                        REPEAT_MODE_ALL -> REPEAT_MODE_ONE
                        else -> REPEAT_MODE_OFF
                    }
                },
                onChangeShuffleMode = {
                    player.shuffleModeEnabled = !player.shuffleModeEnabled
                },
                currentPosition = player.currentPosition,
                isPlaying = player.isPlaying,
                isBuffering = player.isLoading,
                currentSong = SongList.find { it.id.toString() ==  player.currentMediaItem?.mediaId},
                error = player.playerError,
                repeatMode = player.repeatMode,
                isShuffleModeActive = player.shuffleModeEnabled,
            )
        }
    }


    private fun setupProgressUpdateJob(player: Player) {
        progressUpdateJob?.cancel()
        progressUpdateJob = viewModelScope.launch {
            while (true) {
                updatePlayerScreenState {
                    copy(
                        currentPosition = player.currentPosition,
                        bufferedPosition = player.bufferedPosition,
                    )
                }
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    fun onStart() {
        val context: Context = getApplication()
        mediaControllerFuture = MediaController.Builder(
            context,
            SessionToken(context, ComponentName(context, PlaybackService::class.java))
        ).buildAsync()
        mediaControllerFuture.addListener({
            setupPlayer()
                                          }, ContextCompat.getMainExecutor(context))
        updatePlayerScreenState {
            copy(playlist = SongList)
        }
    }

    fun onStop() {
        MediaController.releaseFuture(mediaControllerFuture)
    }
}