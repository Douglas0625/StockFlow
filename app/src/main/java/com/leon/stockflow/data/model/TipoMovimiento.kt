package com.leon.stockflow.data.model

/**
 * Tipo de operación de inventario.
 * Room guarda este enum como texto ("ENTRADA" / "SALIDA") sin configuración extra.
 */
enum class TipoMovimiento {
    ENTRADA,
    SALIDA
}