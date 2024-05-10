package com.appsrui.music.ui

import android.graphics.drawable.Icon
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.PointerIcon.Companion.Text
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import coil.compose.AsyncImage
import com.appsrui.music.R
import com.appsrui.music.model.Song
import com.appsrui.music.model.SongList
import com.appsrui.music.ui.theme.MusicTheme
import java.util.concurrent.TimeUnit

@Composable
fun SongListItem(modifier: Modifier = Modifier, song: Song, isPlaying: Boolean = false) {
    SongListItem(
        modifier = modifier,
        image = song.thumb,
        title = song.title,
        subtitle = song.artist,
        duration = song.durationSeconds,
        isPlaying = isPlaying,
    )
}

@Composable
private fun SongListItem(
    modifier: Modifier = Modifier,
    image: String,
    title: String,
    subtitle: String,
    duration: Int,
    isPlaying: Boolean,
) {
    val color = if (isPlaying)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.onBackground
    ConstraintLayout(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp)
    ) {
        val (thumb, info, durationText) = createRefs()
        AsyncImage(
            model = image.takeIf { it.isNotBlank() },
            fallback = painterResource(id = R.drawable.icon_music_note),
            contentDescription = stringResource(id = R.string.album_cover_text),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(id = R.drawable.icon_music_note),
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .constrainAs(thumb) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                }
        )
        Column(
            modifier = Modifier
                .constrainAs(info) {
                    width = Dimension.fillToConstraints
                    start.linkTo(thumb.end, margin = 8.dp)
                    top.linkTo(parent.top)
                    end.linkTo(durationText.start, margin = 32.dp)
                }
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                color = color,
                modifier = Modifier.fillMaxWidth()
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_person),
                    contentDescription = "",
                    modifier = Modifier.size(16.dp),
                    tint = color,
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    maxLines = 1,
                    color = color,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Text(
            text = "${TimeUnit.SECONDS.toMinutes(duration.toLong())}:${"%02d".format(duration % 60)}",
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier
                .wrapContentWidth()
                .constrainAs(durationText) {
                    start.linkTo(info.end, margin = 8.dp)
                    top.linkTo(parent.top)
                    end.linkTo(parent.end, margin = 8.dp)
                    bottom.linkTo(parent.bottom)
                }
        )

    }
}

@Preview(showBackground = true)
@Composable
fun SongListItemPreview() {
    MusicTheme {
        SongListItem(song = SongList[0])
    }
}

@Preview(showBackground = true)
@Composable
fun SongListItemPreviewPlaying() {
    MusicTheme {
        SongListItem(song = SongList[0], isPlaying = true)
    }
}
