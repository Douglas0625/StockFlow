package com.leon.stockflow.ui.common

import android.content.Context
import android.text.format.DateUtils
import com.leon.stockflow.R
import com.leon.stockflow.data.model.MovimientoConDetalles
import com.leon.stockflow.data.model.TipoMovimiento
import com.leon.stockflow.ui.home.MovimientoUi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Conversión compartida de un movimiento de la base de datos a lo que se muestra en una fila.
 * La usan Inicio (MainViewModel) y Movimientos (MovimientosViewModel), así no se copia.
 *
 * La base guarda "datos crudos" (fecha en milisegundos, cantidades sueltas); aquí se
 * convierten en texto listo para pintar.
 */
fun MovimientoConDetalles.aUi(ctx: Context): MovimientoUi {
    val primero = detalles.firstOrNull()?.producto?.nombre ?: ctx.getString(R.string.no_products)
    val extra = detalles.size - 1
    val titulo = if (extra > 0) ctx.getString(R.string.and_more, primero, extra) else primero

    val total = detalles.sumOf { it.detalle.cantidad }
    val signo = if (movimiento.tipo == TipoMovimiento.ENTRADA) "+" else "-"

    return MovimientoUi(
        id = movimiento.id,
        titulo = titulo,
        fecha = formatearFecha(ctx, movimiento.fecha),
        cantidad = "$signo$total",
        tipo = movimiento.tipo,
        observaciones = movimiento.observaciones.trim()
    )
}

/** Convierte milisegundos en "Hoy, 10:30 AM", "Ayer, 04:45 PM" o "22/10/2026, 11:00 AM". */
fun formatearFecha(ctx: Context, millis: Long): String {
    val hora = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(millis))
    return when {
        DateUtils.isToday(millis) -> ctx.getString(R.string.date_today, hora)
        DateUtils.isToday(millis + DateUtils.DAY_IN_MILLIS) -> ctx.getString(R.string.date_yesterday, hora)
        else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis)) + ", $hora"
    }
}