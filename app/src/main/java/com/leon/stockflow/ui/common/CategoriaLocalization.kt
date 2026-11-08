package com.leon.stockflow.ui.common

import android.content.Context
import com.leon.stockflow.R

/** Localiza únicamente las categorías predeterminadas; las personalizadas conservan su nombre. */
fun nombreCategoriaLocalizado(context: Context, nombre: String): String {
    val recurso = when (nombre.trim()) {
        "Alimentos" -> R.string.category_food
        "Bebidas" -> R.string.category_drinks
        "Limpieza" -> R.string.category_cleaning
        "Papelería" -> R.string.category_stationery
        "Otros" -> R.string.category_other
        else -> null
    }

    return if (recurso != null) {
        context.getString(recurso)
    } else {
        nombre
    }
}
