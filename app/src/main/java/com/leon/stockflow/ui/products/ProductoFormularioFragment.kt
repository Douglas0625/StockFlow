package com.leon.stockflow.ui.products

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.leon.stockflow.R
import com.leon.stockflow.databinding.FragmentProductoFormularioBinding
import com.leon.stockflow.ui.common.Args
import com.leon.stockflow.ui.common.volver

/**
 * PANTALLA: Agregar / Editar Producto (pantalla secundaria, se abre desde Productos o desde el
 * Detalle del Producto).
 *
 * Modo: si idProducto == Args.SIN_ID (-1) es un producto NUEVO; si trae un id, es EDITAR ese
 * producto (cargarlo con productoDao().obtenerPorId(id) y rellenar los campos).
 *
 * QUÉ DEBE TENER (según Figma): foto (opcional, pendiente de decidir), nombre, categoría
 * (menú desplegable con las categorías predefinidas, el usuario NO crea categorías),
 * descripción, precio de compra, precio de venta, stock actual, stock mínimo, y los botones
 * "Cancelar" (volver()) y "Guardar Producto".
 *
 * DATOS QUE YA EXISTEN:
 *  - db.categoriaDao().obtenerTodas()            Flow<List<Categoria>>  (opciones del desplegable)
 *  - db.productoDao().obtenerPorId(id)           suspend, devuelve Producto?  (para editar)
 *  - db.productoDao().insertar(producto)         suspend  (producto nuevo)
 *  - db.productoDao().actualizar(producto)       suspend  (guardar cambios; el id debe ser el existente)
 *
 * Al guardar con éxito: volver(). Validar antes que nombre, categoría y precios no estén vacíos.
 * Crear un Producto: Producto(nombre = ..., precioCompra = ..., precioVenta = ...,
 * cantidadStock = ..., stockMinimo = ..., idCategoria = ..., fotoUri = null).

 * ANTES DE EMPEZAR: leer InicioFragment.kt y MainViewModel.kt (ejemplo completo y comentado).
 * Si hay botones marcados "TEMPORAL", solo prueban la navegación: reemplazarlos por el diseño.
 * Pasos: (1) reemplazar el layout fragment_xxx.xml por el diseño de Figma, (2) crear un
 * ViewModel copiando el patrón de MainViewModel, (3) mostrar los datos en el Fragment,
 * (4) borrar los botones temporales y la línea "En construcción".
 */
class ProductoFormularioFragment : Fragment(R.layout.fragment_producto_formulario) {

    // Binding: acceso a las vistas del layout (ver el ejemplo comentado en InicioFragment).
    private var _binding: FragmentProductoFormularioBinding? = null
    private val binding get() = _binding!!

    /** Id del producto a editar. Args.SIN_ID (-1) = producto nuevo. */
    private val idProducto: Long by lazy { arguments?.getLong(Args.ID_PRODUCTO, Args.SIN_ID) ?: Args.SIN_ID }

    // Aquí se conecta todo: botones, listas y datos.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProductoFormularioBinding.bind(view)
        binding.toolbar.setNavigationOnClickListener { volver() }
        binding.tvInfo.text =
            if (idProducto == Args.SIN_ID) getString(R.string.new_item) else getString(R.string.received_id, idProducto)
    }

    // Se suelta el binding cuando la vista se destruye.
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
