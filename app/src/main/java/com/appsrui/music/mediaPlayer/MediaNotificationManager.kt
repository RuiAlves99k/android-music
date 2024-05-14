package com.appsrui.music.mediaPlayer

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerNotificationManager
import androidx.media3.ui.PlayerNotificationManager.MediaDescriptionAdapter
import androidx.media3.ui.PlayerNotificationManager.NotificationListener
import coil.imageLoader
import coil.request.ImageRequest
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@UnstableApi
class MediaNotificationManager(
    private val context: Context,
    sessionToken: SessionToken,
    private val player: Player,
    notificationListener: NotificationListener,
) {
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private val notificationManager: PlayerNotificationManager

    val NOW_PLAYING_CHANNEL_ID = "media.NOW_PLAYING"
    val NOW_PLAYING_NOTIFICATION_ID = 0xb339

    init {
        val mediaController = MediaController.Builder(context, sessionToken).buildAsync()

        notificationManager = PlayerNotificationManager.Builder(
            context,
            NOW_PLAYING_NOTIFICATION_ID,
            NOW_PLAYING_CHANNEL_ID
        )
//            .setChannelNameResourceId(R.string.media_notification_channel)
//            .setChannelDescriptionResourceId(R.string.media_notification_channel_description)
            .setMediaDescriptionAdapter(DescriptionAdapter(mediaController))
            .setNotificationListener(notificationListener)
            .build()
            .apply {
                setPlayer(player)
                setUseRewindAction(true)
                setUseFastForwardAction(true)
                setUseRewindActionInCompactView(true)
                setUseFastForwardActionInCompactView(true)
            }
    }

    fun hideNotification() {
        notificationManager.setPlayer(null)
    }

    fun showNotificationPlayer(player: Player) {
        notificationManager.setPlayer(player)
    }

    private inner class DescriptionAdapter(private val controller: ListenableFuture<MediaController>) :
        MediaDescriptionAdapter {
        var currentIconUri: Uri? = null
        var currentBitmap: Bitmap? = null
        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return controller.get().sessionActivity
        }

        override fun getCurrentContentText(player: Player): CharSequence? {
            return ""
        }

        override fun getCurrentContentTitle(player: Player): CharSequence {
            return controller.get().mediaMetadata.title.toString()
        }

        override fun getCurrentLargeIcon(
            player: Player,
            callback: PlayerNotificationManager.BitmapCallback
        ): Bitmap? {
            val iconUri = controller.get().mediaMetadata.artworkUri
            return if (currentIconUri != iconUri || currentBitmap == null) {
                currentIconUri = iconUri
                serviceScope.launch {
                    currentBitmap = iconUri?.let {
                        resolveUriAsBitmap(it)
                    }
                    currentBitmap?.let { callback.onBitmap(it) }
                }
                null
            } else {
                currentBitmap
            }
        }

        private suspend fun resolveUriAsBitmap(uri: Uri): Bitmap? {
            return withContext(Dispatchers.IO) {
                val request = ImageRequest.Builder(context)
                    .data(uri)
                    .build()
                (context.imageLoader.execute(request).drawable as BitmapDrawable).bitmap
            }
        }
    }

}