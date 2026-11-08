package com.leon.stockflow.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.leon.stockflow.data.AppDatabase
import com.leon.stockflow.ui.common.aUi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel de la pantalla Inicio. EJEMPLO a copiar para los ViewModels de las demás pantallas
 * (ProductosViewModel, MovimientosViewModel...).
 *
 * ¿Qué es un ViewModel? El intermediario entre la base de datos y la pantalla:
 *   - Pide los datos a la base (a través de los DAOs de data/dao).
 *   - Los transforma a lo que se muestra (por ejemplo "+50" o "Hoy, 10:30 AM").
 *   - Sobrevive cuando se rota el celular, así no se vuelven a pedir los datos.
 *   - NO conoce vistas ni botones: eso es trabajo del Fragment.
 *
 * Es un AndroidViewModel porque necesita el "contexto" de la app para abrir la base de datos
 * y leer textos de strings.xml.
 */
class MainViewModel(app: Application) : AndroidViewModel(app) {

    // Punto de acceso a la base de datos. Hay UNA sola instancia en toda la app (singleton).
    // De aquí salen los DAOs: db.productoDao(), db.movimientoDao(), db.categoriaDao()...
    private val db = AppDatabase.getInstance(app)
    // Configuración común de abajo: mantiene la escucha viva 5 segundos después de que la
    // pantalla se va (evita recargar si el usuario solo rota el celular).
    private val compartir = SharingStarted.WhileSubscribed(5_000)

    // ----- Datos que la pantalla observa -----
    // Patrón: el DAO devuelve un Flow (se actualiza solo cuando cambia la base de datos) y
    // stateIn lo convierte en un StateFlow, que siempre guarda el último valor.
    // El último argumento (0, emptyList()) es el valor inicial mientras llegan los datos reales.
    val totalUnidades: StateFlow<Int> =
        db.productoDao().totalUnidades().stateIn(viewModelScope, compartir, 0)

    val totalProductos: StateFlow<Int> =
        db.productoDao().contarProductos().stateIn(viewModelScope, compartir, 0)

    val stockBajo: StateFlow<Int> =
        db.productoDao().contarStockBajo().stateIn(viewModelScope, compartir, 0)

    val movimientosRecientes: StateFlow<List<MovimientoUi>> =
        db.movimientoDao().obtenerRecientes(5)
            .map { lista -> lista.map { it.aUi(app) } }
            .stateIn(viewModelScope, compartir, emptyList())
}