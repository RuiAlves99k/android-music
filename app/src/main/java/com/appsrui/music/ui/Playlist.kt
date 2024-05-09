package com.appsrui.music.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appsrui.music.model.Song
import com.appsrui.music.model.SongList
import com.appsrui.music.ui.theme.MusicTheme

@Composable
fun Playlist(
    modifier: Modifier = Modifier,
    playlist: List<Song> = emptyList(),
    onSongClick: (Song) -> Unit = {},
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(playlist, key = { it.id }) { song ->
            SongListItem(
                modifier = Modifier.clickable { onSongClick(song) },
                song = song,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlaylistPreview() {
    MusicTheme {
        Playlist(playlist = SongList)
    }
}