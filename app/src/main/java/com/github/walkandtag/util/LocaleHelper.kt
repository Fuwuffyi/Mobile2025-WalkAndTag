package com.github.walkandtag.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

fun Context.updateLocale(locale: Locale?): Context {
    if (locale == null) return this.updateLocale(Resources.getSystem().configuration.locales.get(0))
    val config = Configuration(resources.configuration)
    Locale.setDefault(locale)
    config.setLocale(locale)
    return createConfigurationContext(config)
}

fun getDistanceString(context: Context, distanceKilometers: Double): String {
    val locale = context.resources.configuration.locales[0]
    val numberFormat = NumberFormat.getNumberInstance(locale)
    return if (distanceKilometers < 1f) {
        val meters = (distanceKilometers * 1000).roundToInt()
        "$meters m"
    } else {
        numberFormat.maximumFractionDigits = 2
        numberFormat.minimumFractionDigits = 2
        val formatted = numberFormat.format(distanceKilometers)
        "$formatted km"
    }
}

fun getTimeString(timeHours: Double): String {
    val totalMinutes = (timeHours * 60).roundToInt()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return buildString {
        if (hours > 0) append("$hours h ")
        append("$minutes min")
    }.trim()
}
