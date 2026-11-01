package com.leon.stockflow.ui.products

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.leon.stockflow.R
import com.leon.stockflow.databinding.FragmentProductosBinding
import com.leon.stockflow.ui.common.irADetalleProducto
import com.leon.stockflow.ui.common.irAFormularioProducto

/**
 * PANTALLA: Productos (pestaña de la barra inferior). Título en el diseño: "Inventario de Productos".
 *
 * QUÉ DEBE TENER (según Figma):
 *  - Buscador "Buscar producto..." y chips de filtro: "Todos" + una por categoría.
 *  - Lista de productos: nombre, categoría, precio de venta y etiqueta "STOCK: N UDS"
 *    (naranja si cantidadStock <= stockMinimo, verde si no).
 *  - Botón flotante "+"  ->  irAFormularioProducto()
 *  - Tocar un producto   ->  irADetalleProducto(producto.id)
 *
 * DATOS QUE YA EXISTEN (en data/dao, desde el ViewModel con AppDatabase.getInstance(app)):
 *  - db.productoDao().buscar(texto, idCategoria)  Flow<List<ProductoConCategoria>>
 *        texto = "" trae todos; idCategoria = null trae todas las categorías. Cada resultado
 *        trae .producto (el Producto) y .nombreCategoria.
 *  - db.categoriaDao().obtenerTodas()             Flow<List<Categoria>>  (para los chips)
 *
 * Para la lista copiar el patrón de MovimientoAdapter (ui/home).
 * La búsqueda y el filtro vuelven a llamar a buscar(...) cada vez que el usuario los cambia.

 * ANTES DE EMPEZAR: leer InicioFragment.kt y MainViewModel.kt (ejemplo completo y comentado).
 * Si hay botones marcados "TEMPORAL", solo prueban la navegación: reemplazarlos por el diseño.
 * Pasos: (1) reemplazar el layout fragment_xxx.xml por el diseño de Figma, (2) crear un
 * ViewModel copiando el patrón de MainViewModel, (3) mostrar los datos en el Fragment,
 * (4) borrar los botones temporales y la línea "En construcción".
 */
class ProductosFragment : Fragment(R.layout.fragment_productos) {

    // Binding: acceso a las vistas del layout (ver el ejemplo comentado en InicioFragment).
    private var _binding: FragmentProductosBinding? = null
    private val binding get() = _binding!!

    // Aquí se conecta todo: botones, listas y datos.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProductosBinding.bind(view)
        // TEMPORAL: botones de prueba. Quien haga esta pantalla los reemplaza por lo que pide el diseño.
        binding.btnAgregar.setOnClickListener { irAFormularioProducto() }
        binding.btnDetalle.setOnClickListener { irADetalleProducto(1L) }
    }

    // Se suelta el binding cuando la vista se destruye.
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
