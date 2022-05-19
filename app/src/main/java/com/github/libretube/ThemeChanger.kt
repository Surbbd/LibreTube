package com.github.libretube

import android.content.Context
import androidx.preference.PreferenceManager

fun updateAccentColor(context: Context) {
    val colorAccent = PreferenceManager.getDefaultSharedPreferences(context).getString("accent_color", "red")
    when (colorAccent) {
        "red" -> context.setTheme(R.style.Theme_LibreTube)
        "blue" -> context.setTheme(R.style.Theme_Blue)
        "yellow" -> context.setTheme(R.style.Theme_Yellow)
        "green" -> context.setTheme(R.style.Theme_Green)
        "purple" -> context.setTheme(R.style.Theme_Purple)
    }
}
