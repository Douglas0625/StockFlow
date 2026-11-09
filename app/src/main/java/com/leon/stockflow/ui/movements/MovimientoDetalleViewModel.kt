package com.leon.stockflow.ui.movements

import android.app.Application
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.leon.stockflow.R
import com.leon.stockflow.data.AppDatabase
import com.leon.stockflow.data.model.TipoMovimiento
import com.leon.stockflow.databinding.FragmentMovimientoDetalleBinding
import com.leon.stockflow.ui.common.Args
import com.leon.stockflow.ui.common.volver
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import com.leon.stockflow.data.model.StockInsuficienteException
import kotlinx.coroutines.CancellationException

/**
 * PANTALLA: Detalles de Movimiento (se abre al tocar un movimiento en Inicio o Movimientos).
 *   - Tipo: etiqueta ENTRADA / SALIDA (no editable).
 *   - Fecha, observaciones y cantidades: editables. Los productos no se pueden quitar.
 *   - Cancelar -> volver().  Guardar Cambios -> corrige el movimiento y el stock.
 */


/** Avisos de una sola vez para el Fragment. */
sealed interface EventoDetalle {
    /** Solo mostrar un mensaje (la pantalla sigue abierta). */
    data class Mensaje(val texto: String) : EventoDetalle

    /** Mostrar un mensaje y cerrar la pantalla (guardado correcto o movimiento inexistente). */
    data class Cerrar(val texto: String) : EventoDetalle
}

/**
 * ViewModel de Detalle de Movimiento.
 * Carga el movimiento una sola vez y guarda el borrador de los cambios (fecha, observaciones
 * y cantidades) para que no se pierdan al rotar. Nada se escribe en la base hasta guardar().
 *
 * Solo se pueden editar: fecha, observaciones y cantidades. El tipo y los productos son fijos.
 */
class MovimientoDetalleViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getInstance(app)

    // ----- Estado que la pantalla observa -----
    private val _tipo = MutableStateFlow<TipoMovimiento?>(null) // null = todavía cargando
    val tipo: StateFlow<TipoMovimiento?> = _tipo

    private val _fecha = MutableStateFlow(System.currentTimeMillis())
    val fecha: StateFlow<Long> = _fecha

    private val _observaciones = MutableStateFlow("")
    val observaciones: StateFlow<String> = _observaciones

    private val _lineas = MutableStateFlow<List<LineaMovimientoUi>>(emptyList())
    val lineas: StateFlow<List<LineaMovimientoUi>> = _lineas

    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando

    private val _eventos = Channel<EventoDetalle>(Channel.BUFFERED)
    val eventos: Flow<EventoDetalle> = _eventos.receiveAsFlow()

    private var idMovimiento = Args.SIN_ID
    private var cargado = false

    // ---------- Cargar ----------
    /** Se puede llamar cada vez que se crea la vista: solo carga la primera vez. */
    fun cargar(id: Long) {
        if (cargado) return
        cargado = true
        idMovimiento = id

        viewModelScope.launch {
            try {
                val completo = db.movimientoDao().obtenerPorId(id)
                if (completo == null) {
                    cerrar(R.string.detalle_mov_no_encontrado)
                    return@launch
                }
                _fecha.value = completo.movimiento.fecha
                _observaciones.value = completo.movimiento.observaciones
                _lineas.value = completo.detalles
                    .sortedBy { it.detalle.id }
                    .map {
                        LineaMovimientoUi(
                            idProducto = it.producto.id,
                            nombre = it.producto.nombre,
                            fotoUri = it.producto.fotoUri,
                            stock = it.producto.cantidadStock,
                            cantidad = it.detalle.cantidad
                        )
                    }
                _tipo.value = completo.movimiento.tipo // al final: indica que ya hay datos
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                cerrar(R.string.detalle_mov_error_carga)
            }
        }
    }

    // ---------- Edición del borrador ----------
    fun cambiarObservaciones(texto: String) {
        _observaciones.value = texto
    }

    /**
     * El calendario devuelve el día como medianoche UTC. Se cambia solo el DÍA del movimiento
     * y se conserva su hora original, para no alterar el orden dentro del historial.
     */
    fun elegirDia(millisUtc: Long) {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = millisUtc }
        val nueva = Calendar.getInstance().apply {
            timeInMillis = _fecha.value
            set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH))
        }
        _fecha.value = nueva.timeInMillis
    }

    /** La fecha actual como medianoche UTC, que es lo que el calendario espera para marcarla. */
    fun fechaParaCalendario(): Long {
        val local = Calendar.getInstance().apply { timeInMillis = _fecha.value }
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis
    }

    /** delta = +1 o -1. La cantidad mínima es 1. */
    fun cambiarCantidad(idProducto: Long, delta: Int) {
        _lineas.value = _lineas.value.map {
            if (it.idProducto == idProducto) it.copy(cantidad = (it.cantidad + delta).coerceAtLeast(1)) else it
        }
    }

    // ---------- Eliminar ----------
    fun eliminar() {
        if (_guardando.value || _tipo.value == null) return
        _guardando.value = true
        viewModelScope.launch {
            try {
                db.movimientoDao().eliminarMovimiento(idMovimiento)
                cerrar(R.string.detalle_mov_eliminado)
            } catch (e: StockInsuficienteException) {
                val nombre = _lineas.value.firstOrNull { it.idProducto == e.idProducto }?.nombre.orEmpty()
                _eventos.send(
                    EventoDetalle.Mensaje(
                        getApplication<Application>().getString(
                            R.string.detalle_mov_error_eliminar_stock, nombre
                        )
                    )
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _eventos.send(
                    EventoDetalle.Mensaje(
                        getApplication<Application>().getString(R.string.detalle_mov_error_eliminar)
                    )
                )
            } finally {
                _guardando.value = false
            }
        }
    }

    // ---------- Guardar ----------
    fun guardar() {
        if (_guardando.value || _tipo.value == null) return // evita doble toque o guardar sin datos

        val lista = _lineas.value
        _guardando.value = true
        viewModelScope.launch {
            try {
                db.movimientoDao().actualizarMovimiento(
                    idMovimiento = idMovimiento,
                    fecha = _fecha.value,
                    observaciones = _observaciones.value.trim(),
                    cantidades = lista.associate { it.idProducto to it.cantidad }
                )
                cerrar(R.string.detalle_mov_exito)
            } catch (e: StockInsuficienteException) {
                // Algún producto quedaría con stock negativo: no se cambió nada.
                val nombre = lista.firstOrNull { it.idProducto == e.idProducto }?.nombre.orEmpty()
                _eventos.send(
                    EventoDetalle.Mensaje(
                        getApplication<Application>().getString(R.string.detalle_mov_error_stock, nombre)
                    )
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _eventos.send(
                    EventoDetalle.Mensaje(
                        getApplication<Application>().getString(R.string.detalle_mov_error_generico)
                    )
                )
            } finally {
                _guardando.value = false
            }
        }
    }

    private suspend fun cerrar(mensajeRes: Int) {
        _eventos.send(EventoDetalle.Cerrar(getApplication<Application>().getString(mensajeRes)))
    }
}