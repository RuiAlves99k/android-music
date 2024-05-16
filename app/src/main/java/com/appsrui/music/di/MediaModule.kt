package com.appsrui.music.di

import android.app.Application
import com.appsrui.music.mediaPlayer.MediaPlayer
import com.appsrui.music.mediaPlayer.MusicMediaPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    @Provides
    @Singleton
    fun provideMediaPlayer(app: Application): MediaPlayer {
        return MusicMediaPlayer(app)
    }
}