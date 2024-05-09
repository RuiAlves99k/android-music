package com.appsrui.music.ui.theme

import java.util.concurrent.TimeUnit

fun Number.toFormatterDuration(timeUnit: TimeUnit = TimeUnit.SECONDS): String {
    val thisLong = toLong()
    return "%02d:%02d".format(
        timeUnit.toMinutes(thisLong),
        timeUnit.toSeconds(thisLong) % 60
    )
}