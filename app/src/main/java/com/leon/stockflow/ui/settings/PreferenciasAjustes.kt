package com.leon.stockflow.ui.settings

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate

/**
 * Gestión de preferencias de la pantalla de Ajustes (tema claro/oscuro e idioma).
 */
object PreferenciasAjustes {

    private const val PREFS_NAME = "stockflow_settings_prefs"
    private const val KEY_THEME_IS_DARK = "key_theme_is_dark"

    /**
     * Guarda la elección de modo oscuro en SharedPreferences.
     */
    fun guardarModoOscuro(context: Context, esOscuro: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_THEME_IS_DARK, esOscuro)
            .apply()
    }

    /**
     * Retorna true si el usuario tiene seleccionado el modo oscuro.
     * Si nunca ha seleccionado uno, toma el estado actual del sistema.
     */
    fun esModoOscuro(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.contains(KEY_THEME_IS_DARK)) {
            return prefs.getBoolean(KEY_THEME_IS_DARK, false)
        }
        val currentNightMode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }

    /**
     * Aplica el modo guardado al arrancar la app. Se debe llamar en MainActivity.onCreate
     * antes de super.onCreate().
     */
    fun aplicarTemaGuardado(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.contains(KEY_THEME_IS_DARK)) {
            val esOscuro = prefs.getBoolean(KEY_THEME_IS_DARK, false)
            val modo = if (esOscuro) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
            AppCompatDelegate.setDefaultNightMode(modo)
        }
    }
}
