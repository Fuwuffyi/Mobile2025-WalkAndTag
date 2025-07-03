package com.github.walkandtag.util

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

fun Context.updateLocale(locale: Locale?): Context {
    if(locale == null)
        return this

    val config = Configuration(resources.configuration)
    Locale.setDefault(locale)
    config.setLocale(locale)
    return createConfigurationContext(config)
}