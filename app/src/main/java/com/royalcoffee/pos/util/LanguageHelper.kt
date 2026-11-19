package com.royalcoffee.pos.util

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
cd C:\AndroidProjects\RoyalCoffeePOS
object LanguageHelper {
    private const val PREFS_NAME = "royal_coffee_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE, "English") ?: "English"
    }

    fun saveLanguage(context: Context, language: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_LANGUAGE, language).apply()
    }

    fun getLanguageCode(language: String): String {
        return when (language.lowercase().trim()) {
            "fr", "french", "français", "francais" -> "fr"
            else -> "en"
        }
    }

    fun applySavedLanguage(context: Context) {
        val language = getSavedLanguage(context)
        val code = getLanguageCode(language)
        val appLocales = LocaleListCompat.forLanguageTags(code)
        AppCompatDelegate.setApplicationLocales(appLocales)
    }

    fun applyLanguage(language: String) {
        val code = getLanguageCode(language)
        val appLocales = LocaleListCompat.forLanguageTags(code)
        AppCompatDelegate.setApplicationLocales(appLocales)
    }
}
