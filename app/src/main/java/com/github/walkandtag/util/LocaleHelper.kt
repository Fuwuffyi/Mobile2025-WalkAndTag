package com.github.walkandtag.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import java.util.Locale

fun Context.updateLocale(locale: Locale?): Context {
    if (locale == null) return this.updateLocale(Resources.getSystem().configuration.locales.get(0))
    val config = Configuration(resources.configuration)
    Locale.setDefault(locale)
    config.setLocale(locale)
    return createConfigurationContext(config)
}
