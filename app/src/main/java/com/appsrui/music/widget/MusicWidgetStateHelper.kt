package com.appsrui.music.widget

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.appsrui.music.model.Song

object MusicWidgetStateHelper {
    private const val idKey = "widgetIdKey"
    private const val songNameKey = "widgetSongNameKey"
    private const val artistNameKey = "artistNameKey"
    private const val sourceKey = "widgetSourceKey"
    private const val thumbKey = "widgetThumbKey"
    private const val durationSecondsKey = "widgetDurationSecondsKey"

    fun save(prefs: MutablePreferences, state: Song) {
        prefs[intPreferencesKey(idKey)] = state.id
        prefs[stringPreferencesKey(songNameKey)] = state.title
        prefs[stringPreferencesKey(artistNameKey)] = state.artist
        prefs[stringPreferencesKey(sourceKey)] = state.source
        prefs[stringPreferencesKey(thumbKey)] = state.thumb
        prefs[intPreferencesKey(durationSecondsKey)] = state.durationSeconds
    }

    fun isStored(prefs: MutablePreferences, song: Song): Boolean =
        prefs[intPreferencesKey(idKey)] == song.id &&
                prefs[stringPreferencesKey(songNameKey)] == song.title &&
                prefs[stringPreferencesKey(artistNameKey)] == song.artist &&
                prefs[stringPreferencesKey(sourceKey)] == song.source &&
                prefs[stringPreferencesKey(thumbKey)] == song.thumb &&
                prefs[intPreferencesKey(durationSecondsKey)] == song.durationSeconds

    fun getState(prefs: MutablePreferences): MusicWidgetState {
        val id = prefs[intPreferencesKey(idKey)] ?: Int.MIN_VALUE
        val songName = prefs[stringPreferencesKey(songNameKey)] ?: ""
        val artistName = prefs[stringPreferencesKey(artistNameKey)] ?: ""
        val source = prefs[stringPreferencesKey(sourceKey)] ?: ""
        val thumb = prefs[stringPreferencesKey(thumbKey)] ?: ""
        val durationSeconds = prefs[intPreferencesKey(durationSecondsKey)] ?: Int.MIN_VALUE
        return MusicWidgetState(
            song = Song(
                id = id,
                title = songName,
                artist = artistName,
                source = source,
                thumb = thumb,
                durationSeconds = durationSeconds,
            )
        )
    }
}