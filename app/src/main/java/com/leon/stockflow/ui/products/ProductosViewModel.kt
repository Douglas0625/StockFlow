package com.leon.stockflow.ui.products

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.leon.stockflow.data.AppDatabase
import com.leon.stockflow.data.entity.Categoria
import com.leon.stockflow.data.entity.Producto
import com.leon.stockflow.data.model.HistorialProducto
import com.leon.stockflow.data.model.ProductoConCategoria
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

/**
 * ViewModel del módulo de Productos de StockFlow.
 *
 * Funciones:
 * - Consultar productos.
 * - Buscar y filtrar productos.
 * - Consultar categorías.
 * - Registrar productos con fotografías.
 * - Editar productos existentes.
 * - Consultar el detalle de un producto.
 * - Consultar el historial de movimientos.
 */
class ProductosViewModel(
    app: Application
) : AndroidViewModel(app) {

    // Base de datos Room del proyecto.
    private val db = AppDatabase.getInstance(app)

    // Texto del buscador.
    private val textoBusqueda = MutableStateFlow("")

    // Categoría seleccionada.
    // null significa mostrar todas.
    private val categoriaSeleccionada =
        MutableStateFlow<Long?>(null)

    private val compartir =
        SharingStarted.WhileSubscribed(5_000)

    // =====================================
    // 1. OBTENER LAS CATEGORÍAS
    // =====================================

    val categorias: StateFlow<List<Categoria>> =
        db.categoriaDao()
            .obtenerTodas()
            .stateIn(
                viewModelScope,
                compartir,
                emptyList()
            )

    // =====================================
    // 2. CONSULTAR Y FILTRAR PRODUCTOS
    // =====================================

    @OptIn(ExperimentalCoroutinesApi::class)
    val productos: StateFlow<List<ProductoConCategoria>> =
        combine(
            textoBusqueda,
            categoriaSeleccionada
        ) { texto, categoriaId ->

            texto to categoriaId

        }.flatMapLatest { (texto, categoriaId) ->

            db.productoDao().buscar(
                texto = texto,
                idCategoria = categoriaId
            )

        }.stateIn(
            viewModelScope,
            compartir,
            emptyList()
        )

    // Actualizar la búsqueda.
    fun buscarProducto(texto: String) {
        textoBusqueda.value = texto
    }

    // Aplicar filtro de categoría.
    fun filtrarPorCategoria(idCategoria: Long?) {
        categoriaSeleccionada.value = idCategoria
    }

    // Recuperar los filtros actuales al volver a la pantalla.
    val categoriaActual: Long?
        get() = categoriaSeleccionada.value

    val busquedaActual: String
        get() = textoBusqueda.value


    // =====================================
    // 3. GUARDAR PRODUCTO NUEVO
    // =====================================

    fun guardarProductoNuevo(
        producto: Producto,
        fotoSeleccionada: Uri?,
        alGuardar: () -> Unit,
        alError: (Exception) -> Unit
    ) {
        viewModelScope.launch {

            try {
                withContext(Dispatchers.IO) {

                    var archivoFoto: File? = null

                    try {

                        // Copiar la fotografía seleccionada.
                        if (fotoSeleccionada != null) {
                            archivoFoto = copiarFotoInterna(
                                fotoSeleccionada
                            )
                        }

                        // Obtener la ruta de la fotografía.
                        val rutaFoto = archivoFoto?.let {
                            Uri.fromFile(it).toString()
                        }

                        // Incluir la fotografía en el producto.
                        val productoConFoto = producto.copy(
                            fotoUri = rutaFoto
                        )

                        // Guardar en Room.
                        db.productoDao().insertar(
                            productoConFoto
                        )

                    } catch (e: Exception) {

                        // Eliminar la copia si falla el registro.
                        archivoFoto?.delete()

                        throw e
                    }
                }

                // Notificar que se guardó.
                alGuardar()

            } catch (e: Exception) {

                alError(e)
            }
        }
    }

    // =====================================
    // 4. COPIAR FOTOGRAFÍA INTERNAMENTE
    // =====================================

    private fun copiarFotoInterna(uriOrigen: Uri): File {

        val app = getApplication<Application>()

        // Carpeta privada de fotografías.
        val carpeta = File(
            app.filesDir,
            "fotos_productos"
        )

        // Crear carpeta si no existe.
        if (!carpeta.exists() && !carpeta.mkdirs()) {
            throw IOException(
                "No se pudo crear la carpeta de fotografías"
            )
        }

        // Determinar la extensión.
        val extension = when (
            app.contentResolver.getType(uriOrigen)
        ) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            "image/heic", "image/heif" -> "heic"
            else -> "jpg"
        }

        // Generar nombre único.
        val archivo = File(
            carpeta,
            "${UUID.randomUUID()}.$extension"
        )

        try {

            // Abrir la imagen seleccionada.
            val entrada = app.contentResolver
                .openInputStream(uriOrigen)
                ?: throw IOException(
                    "No se pudo abrir la fotografía"
                )

            // Copiar al almacenamiento interno.
            entrada.use { origen ->

                archivo.outputStream().use { destino ->
                    origen.copyTo(destino)
                }
            }

        } catch (e: Exception) {

            archivo.delete()

            throw e
        }

        return archivo
    }

    // =====================================
    // 5. CARGAR PRODUCTO PARA EDITAR
    // =====================================

    fun cargarProductoParaEditar(
        id: Long,
        alCargar: (Producto?) -> Unit,
        alError: (Exception) -> Unit
    ) {
        viewModelScope.launch {

            try {

                val producto =
                    db.productoDao().obtenerPorId(id)

                alCargar(producto)

            } catch (e: Exception) {

                alError(e)
            }
        }
    }

    // =====================================
    // 6. ACTUALIZAR PRODUCTO
    // =====================================


    // Actualizar un producto y su fotografía opcional.
    fun actualizarProducto(
        producto: Producto,
        fotoSeleccionada: Uri?,
        alGuardar: () -> Unit,
        alError: (Exception) -> Unit
    ) {
        viewModelScope.launch {

            try {
                withContext(Dispatchers.IO) {

                    var nuevaFoto: File? = null

                    try {
                        // Copiar una nueva fotografía, si fue elegida.
                        if (fotoSeleccionada != null) {
                            nuevaFoto = copiarFotoInterna(
                                fotoSeleccionada
                            )
                        }

                        // Conservar la foto anterior si no se cambia.
                        val rutaFoto = nuevaFoto?.let {
                            Uri.fromFile(it).toString()
                        } ?: producto.fotoUri

                        val productoActualizado = producto.copy(
                            fotoUri = rutaFoto
                        )

                        // Actualizar el producto existente.
                        db.productoDao().actualizar(
                            productoActualizado
                        )

                    } catch (e: Exception) {
                        nuevaFoto?.delete()
                        throw e
                    }
                }

                alGuardar()

            } catch (e: Exception) {
                alError(e)
            }
        }
    }


    // =====================================
    // 7. OBSERVAR DETALLE DEL PRODUCTO
    // =====================================

    fun observarProducto(
        id: Long
    ): Flow<ProductoConCategoria?> {

        return db.productoDao()
            .observarPorId(id)
    }

    // =====================================
    // 8. OBSERVAR HISTORIAL DE MOVIMIENTOS
    // =====================================

    fun observarHistorialProducto(
        idProducto: Long
    ): Flow<List<HistorialProducto>> {

        return db.detalleMovimientoDao()
            .obtenerHistorial(idProducto)
    }
}
