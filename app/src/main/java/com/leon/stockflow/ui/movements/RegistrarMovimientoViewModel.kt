package com.leon.stockflow.ui.movements

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.leon.stockflow.R
import com.leon.stockflow.data.AppDatabase
import com.leon.stockflow.data.entity.DetalleMovimiento
import com.leon.stockflow.data.entity.Movimiento
import com.leon.stockflow.data.model.StockInsuficienteException
import com.leon.stockflow.data.model.TipoMovimiento
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.TimeZone

/** Cosas que el ViewModel le avisa UNA sola vez al Fragment (no son estado, son eventos). */
sealed interface EventoRegistro {
    data object Guardado : EventoRegistro
    data class Mensaje(val texto: String) : EventoRegistro
}

/**
 * ViewModel de Registrar Movimiento. Guarda el borrador (tipo, fecha, productos) para que no
 * se pierda al rotar el celular, y al final llama a movimientoDao().registrarMovimiento(...).
 *
 * Las observaciones NO se guardan aquí: el EditText las conserva solo y el Fragment las
 * entrega al llamar a registrar().
 */
class RegistrarMovimientoViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getInstance(app)

    // ----- Estado que la pantalla observa -----
    private val _tipo = MutableStateFlow(TipoMovimiento.ENTRADA)
    val tipo: StateFlow<TipoMovimiento> = _tipo

    private val _fecha = MutableStateFlow(System.currentTimeMillis())
    val fecha: StateFlow<Long> = _fecha

    private val _lineas = MutableStateFlow<List<LineaMovimientoUi>>(emptyList())
    val lineas: StateFlow<List<LineaMovimientoUi>> = _lineas

    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando

    private val _eventos = Channel<EventoRegistro>(Channel.BUFFERED)
    val eventos: Flow<EventoRegistro> = _eventos.receiveAsFlow()

    private var inicialCargado = false

    // ---------- Tipo y fecha ----------
    fun cambiarTipo(tipo: TipoMovimiento) {
        _tipo.value = tipo
    }

    /**
     * El calendario (MaterialDatePicker) devuelve el día como medianoche UTC. Aquí se
     * convierte a ese mismo día en hora local, conservando la hora actual, para que el
     * historial siga ordenándose bien ("Hoy, 10:30 AM").
     */
    fun elegirDia(millisUtc: Long) {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = millisUtc }
        val ahora = Calendar.getInstance()
        val local = Calendar.getInstance().apply {
            set(
                utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH),
                ahora.get(Calendar.HOUR_OF_DAY), ahora.get(Calendar.MINUTE), ahora.get(Calendar.SECOND)
            )
        }
        _fecha.value = local.timeInMillis
    }

    /** La fecha actual como medianoche UTC, que es lo que el calendario espera para marcarla. */
    fun fechaParaCalendario(): Long {
        val local = Calendar.getInstance().apply { timeInMillis = _fecha.value }
        return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
            clear()
            set(local.get(Calendar.YEAR), local.get(Calendar.MONTH), local.get(Calendar.DAY_OF_MONTH))
        }.timeInMillis
    }

    // ---------- Productos de la lista ----------
    /** Producto que viene del Detalle del Producto. Solo se agrega la primera vez. */
    fun cargarProductoInicial(idProducto: Long) {
        if (inicialCargado) return
        inicialCargado = true
        agregarProducto(idProducto)
    }

    fun agregarProducto(idProducto: Long) {
        viewModelScope.launch {
            // Un producto no puede repetirse dentro del mismo movimiento.
            if (_lineas.value.any { it.idProducto == idProducto }) {
                avisar(R.string.registrar_error_repetido)
                return@launch
            }
            val producto = db.productoDao().obtenerPorId(idProducto) ?: return@launch
            _lineas.value = _lineas.value + LineaMovimientoUi(
                idProducto = producto.id,
                nombre = producto.nombre,
                fotoUri = producto.fotoUri,
                stock = producto.cantidadStock,
                cantidad = 1
            )
        }
    }

    /** delta = +1 o -1. La cantidad mínima es 1 (para quitar el producto está la basura). */
    fun cambiarCantidad(idProducto: Long, delta: Int) {
        _lineas.value = _lineas.value.map {
            if (it.idProducto == idProducto) it.copy(cantidad = (it.cantidad + delta).coerceAtLeast(1)) else it
        }
    }

    fun quitarProducto(idProducto: Long) {
        _lineas.value = _lineas.value.filterNot { it.idProducto == idProducto }
    }

    // ---------- Guardar ----------
    fun registrar(observaciones: String) {
        if (_guardando.value) return // evita doble toque

        val lista = _lineas.value
        if (lista.isEmpty()) {
            avisar(R.string.registrar_error_sin_productos)
            return
        }

        _guardando.value = true
        viewModelScope.launch {
            try {
                db.movimientoDao().registrarMovimiento(
                    Movimiento(
                        tipo = _tipo.value,
                        fecha = _fecha.value,
                        observaciones = observaciones.trim()
                    ),
                    lista.map {
                        DetalleMovimiento(idMovimiento = 0, idProducto = it.idProducto, cantidad = it.cantidad)
                    }
                )
                _eventos.send(EventoRegistro.Guardado)
            } catch (e: StockInsuficienteException) {
                // Salida con más unidades que el stock: no se guardó nada.
                val nombre = lista.firstOrNull { it.idProducto == e.idProducto }?.nombre.orEmpty()
                _eventos.send(
                    EventoRegistro.Mensaje(
                        getApplication<Application>().getString(R.string.registrar_error_stock, nombre)
                    )
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                avisar(R.string.registrar_error_generico)
            } finally {
                _guardando.value = false
            }
        }
    }

    private fun avisar(mensajeRes: Int) {
        _eventos.trySend(EventoRegistro.Mensaje(getApplication<Application>().getString(mensajeRes)))
    }
}