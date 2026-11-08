package com.leon.stockflow.ui.movements

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.leon.stockflow.data.AppDatabase
import com.leon.stockflow.data.model.TipoMovimiento
import com.leon.stockflow.ui.common.aUi
import com.leon.stockflow.ui.home.MovimientoUi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel de la pantalla Movimientos (historial de entradas y salidas).
 *
 * Combina dos filtros:
 *   - tipo (Todos / Entradas / Salidas): se pide a Room (obtenerTodos / obtenerPorTipo).
 *   - texto del buscador: se filtra aquí por nombre de producto, porque todavía no hay un
 *     DAO que busque movimientos por producto.
 */
class MovimientosViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getInstance(app)
    private val compartir = SharingStarted.WhileSubscribed(5_000)

    // Filtros actuales. tipo = null significa "Todos".
    private val textoBusqueda = MutableStateFlow("")
    private val tipoSeleccionado = MutableStateFlow<TipoMovimiento?>(null)

    // Para restaurar los filtros al volver a la pantalla (por ejemplo desde el detalle).
    val busquedaActual: String get() = textoBusqueda.value
    val tipoActual: TipoMovimiento? get() = tipoSeleccionado.value

    @OptIn(ExperimentalCoroutinesApi::class)
    val movimientos: StateFlow<List<MovimientoUi>> =
        tipoSeleccionado
            .flatMapLatest { tipo ->
                if (tipo == null) db.movimientoDao().obtenerTodos()
                else db.movimientoDao().obtenerPorTipo(tipo)
            }
            .combine(textoBusqueda) { lista, texto ->
                val buscado = texto.trim()
                if (buscado.isEmpty()) lista
                else lista.filter { mov ->
                    mov.detalles.any { it.producto.nombre.contains(buscado, ignoreCase = true) }
                }
            }
            .map { lista -> lista.map { it.aUi(app) } }
            .stateIn(viewModelScope, compartir, emptyList())

    fun buscar(texto: String) {
        textoBusqueda.value = texto
    }

    fun filtrarPorTipo(tipo: TipoMovimiento?) {
        tipoSeleccionado.value = tipo
    }
}