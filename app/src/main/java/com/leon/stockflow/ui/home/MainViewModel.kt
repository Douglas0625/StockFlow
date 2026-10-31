package com.leon.stockflow.ui.home

import android.app.Application
import android.text.format.DateUtils
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.leon.stockflow.R
import com.leon.stockflow.data.local.AppDatabase
import com.leon.stockflow.data.local.entity.DetalleMovimiento
import com.leon.stockflow.data.local.entity.Movimiento
import com.leon.stockflow.data.local.entity.Producto
import com.leon.stockflow.data.model.MovimientoConDetalles
import com.leon.stockflow.data.model.TipoMovimiento
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getInstance(app)
    private val compartir = SharingStarted.WhileSubscribed(5_000)

    val totalUnidades: StateFlow<Int> =
        db.productoDao().totalUnidades().stateIn(viewModelScope, compartir, 0)

    val totalProductos: StateFlow<Int> =
        db.productoDao().contarProductos().stateIn(viewModelScope, compartir, 0)

    val stockBajo: StateFlow<Int> =
        db.productoDao().contarStockBajo().stateIn(viewModelScope, compartir, 0)

    val movimientosRecientes: StateFlow<List<MovimientoUi>> =
        db.movimientoDao().obtenerRecientes(3)
            .map { lista -> lista.map { it.aUi() } }
            .stateIn(viewModelScope, compartir, emptyList())

    // ---------- Conversión a lo que se muestra en pantalla ----------
    private fun MovimientoConDetalles.aUi(): MovimientoUi {
        val ctx = getApplication<Application>()

        val primero = detalles.firstOrNull()?.producto?.nombre ?: ctx.getString(R.string.no_products)
        val extra = detalles.size - 1
        val titulo = if (extra > 0) ctx.getString(R.string.and_more, primero, extra) else primero

        val total = detalles.sumOf { it.detalle.cantidad }
        val signo = if (movimiento.tipo == TipoMovimiento.ENTRADA) "+" else "-"

        return MovimientoUi(
            id = movimiento.id,
            titulo = titulo,
            fecha = formatearFecha(movimiento.fecha),
            cantidad = "$signo$total",
            tipo = movimiento.tipo
        )
    }

    private fun formatearFecha(millis: Long): String {
        val ctx = getApplication<Application>()
        val hora = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(millis))
        return when {
            DateUtils.isToday(millis) -> ctx.getString(R.string.date_today, hora)
            DateUtils.isToday(millis + DateUtils.DAY_IN_MILLIS) -> ctx.getString(R.string.date_yesterday, hora)
            else -> SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis)) + ", $hora"
        }
    }

    // ---------- TEMPORAL: datos de prueba (borrar cuando ya no se necesite) ----------
    fun cargarDatosDePrueba() = viewModelScope.launch {
        val productoDao = db.productoDao()
        val movimientoDao = db.movimientoDao()

        val cajas = productoDao.insertar(
            Producto(nombre = "Cajas de Cartón A4", precioCompra = 2.5, precioVenta = 4.0,
                cantidadStock = 0, stockMinimo = 20, idCategoria = 1)
        )
        val cinta = productoDao.insertar(
            Producto(nombre = "Cinta de Embalaje", precioCompra = 1.0, precioVenta = 2.0,
                cantidadStock = 0, stockMinimo = 10, idCategoria = 1)
        )
        val etiquetas = productoDao.insertar(
            Producto(nombre = "Etiquetas", precioCompra = 0.5, precioVenta = 1.0,
                cantidadStock = 0, stockMinimo = 40, idCategoria = 1)
        )

        movimientoDao.registrarMovimiento(
            Movimiento(tipo = TipoMovimiento.ENTRADA, fecha = System.currentTimeMillis() - 3_600_000),
            listOf(
                DetalleMovimiento(idMovimiento = 0, idProducto = cajas, cantidad = 50),
                DetalleMovimiento(idMovimiento = 0, idProducto = cinta, cantidad = 40),
                DetalleMovimiento(idMovimiento = 0, idProducto = etiquetas, cantidad = 30)
            )
        )
        movimientoDao.registrarMovimiento(
            Movimiento(tipo = TipoMovimiento.SALIDA, fecha = System.currentTimeMillis()),
            listOf(
                DetalleMovimiento(idMovimiento = 0, idProducto = cinta, cantidad = 10),
                DetalleMovimiento(idMovimiento = 0, idProducto = cajas, cantidad = 5)
            )
        )
    }
}