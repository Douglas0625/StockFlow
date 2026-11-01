package com.leon.stockflow.ui.products

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.leon.stockflow.R
import com.leon.stockflow.databinding.FragmentProductoDetalleBinding
import com.leon.stockflow.ui.common.Args
import com.leon.stockflow.ui.common.irAFormularioProducto
import com.leon.stockflow.ui.common.irARegistrarMovimiento
import com.leon.stockflow.ui.common.volver

/**
 * PANTALLA: Detalle del Producto (pantalla secundaria, se abre al tocar un producto).
 * Recibe idProducto (ya leído en la propiedad de abajo).
 *
 * QUÉ DEBE TENER (según Figma): foto, nombre, categoría, descripción, precio de compra y de
 * venta, stock actual, stock mínimo (en naranja si el stock es bajo, o sea cantidadStock <=
 * stockMinimo), el "Historial de movimientos" de ese producto (Entrada +N en verde, Salida -N
 * en rojo, con fecha) y dos botones:
 *  - "Editar Producto"       ->  irAFormularioProducto(idProducto)
 *  - "Registrar Movimiento"  ->  irARegistrarMovimiento(idProducto)  (el producto ya va en la lista)
 *
 * DATOS QUE YA EXISTEN:
 *  - db.productoDao().observarPorId(id)                 Flow<ProductoConCategoria?>
 *  - db.detalleMovimientoDao().obtenerHistorial(id)     Flow<List<HistorialProducto>>
 *        cada fila: tipo (ENTRADA/SALIDA), fecha (milisegundos), observaciones y cantidad.
 *
 * Copiar el patrón de InicioFragment para observar los Flow. Para la lista del historial,
 * el de MovimientoAdapter.

 * ANTES DE EMPEZAR: leer InicioFragment.kt y MainViewModel.kt (ejemplo completo y comentado).
 * Si hay botones marcados "TEMPORAL", solo prueban la navegación: reemplazarlos por el diseño.
 * Pasos: (1) reemplazar el layout fragment_xxx.xml por el diseño de Figma, (2) crear un
 * ViewModel copiando el patrón de MainViewModel, (3) mostrar los datos en el Fragment,
 * (4) borrar los botones temporales y la línea "En construcción".
 */
class ProductoDetalleFragment : Fragment(R.layout.fragment_producto_detalle) {

    // Binding: acceso a las vistas del layout (ver el ejemplo comentado en InicioFragment).
    private var _binding: FragmentProductoDetalleBinding? = null
    private val binding get() = _binding!!

    /** Id del producto que se está mostrando. */
    private val idProducto: Long by lazy { arguments?.getLong(Args.ID_PRODUCTO, Args.SIN_ID) ?: Args.SIN_ID }

    // Aquí se conecta todo: botones, listas y datos.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProductoDetalleBinding.bind(view)
        binding.toolbar.setNavigationOnClickListener { volver() }
        binding.tvInfo.text = getString(R.string.received_id, idProducto)

        // TEMPORAL: botones de prueba. Quien haga esta pantalla los reemplaza por lo que pide el diseño.
        binding.btnEditar.setOnClickListener { irAFormularioProducto(idProducto) }
        binding.btnMovimiento.setOnClickListener { irARegistrarMovimiento(idProducto) }
    }

    // Se suelta el binding cuando la vista se destruye.
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
