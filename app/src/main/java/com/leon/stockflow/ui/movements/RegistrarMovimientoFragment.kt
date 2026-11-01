package com.leon.stockflow.ui.movements

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.leon.stockflow.R
import com.leon.stockflow.databinding.FragmentRegistrarMovimientoBinding
import com.leon.stockflow.ui.common.Args
import com.leon.stockflow.ui.common.volver

/**
 * PANTALLA: Registrar Movimiento (pantalla secundaria, se abre desde Movimientos o desde el
 * Detalle del Producto). Si viene del Detalle del Producto, idProductoInicial trae ese producto
 * y debe aparecer ya en la lista; si vale Args.SIN_ID (-1), la lista empieza vacía.
 *
 * QUÉ DEBE TENER (según Figma): selector Entrada / Salida, fecha (MaterialDatePicker),
 * observaciones (opcional), lista de productos con cantidad (- / +) y botón de quitar,
 * "Añadir Producto" (abre el selector de producto: una hoja inferior con buscador, por ejemplo
 * un BottomSheetDialogFragment que reutilice db.productoDao().buscar(...)), y el botón
 * "Registrar Entrada" / "Registrar Salida" (cambia según el tipo elegido).
 *
 * GUARDAR (ya existe y hace todo, incluido actualizar el stock):
 *   db.movimientoDao().registrarMovimiento(
 *       Movimiento(tipo = TipoMovimiento.ENTRADA, fecha = fechaEnMilisegundos, observaciones = "..."),
 *       listOf(DetalleMovimiento(idMovimiento = 0, idProducto = id, cantidad = n), ...)
 *   )
 *  - Es suspend: llamarlo dentro de viewModelScope.launch { ... }.
 *  - En una SALIDA con más unidades que el stock lanza StockInsuficienteException y NO guarda
 *    nada: capturarla (try/catch) y mostrar un mensaje claro al usuario.
 *  - Reglas: al menos un producto, cantidades mayores a 0, sin repetir el mismo producto.
 *  - Al terminar bien: volver().

 * ANTES DE EMPEZAR: leer InicioFragment.kt y MainViewModel.kt (ejemplo completo y comentado).
 * Si hay botones marcados "TEMPORAL", solo prueban la navegación: reemplazarlos por el diseño.
 * Pasos: (1) reemplazar el layout fragment_xxx.xml por el diseño de Figma, (2) crear un
 * ViewModel copiando el patrón de MainViewModel, (3) mostrar los datos en el Fragment,
 * (4) borrar los botones temporales y la línea "En construcción".
 */
class RegistrarMovimientoFragment : Fragment(R.layout.fragment_registrar_movimiento) {

    // Binding: acceso a las vistas del layout (ver el ejemplo comentado en InicioFragment).
    private var _binding: FragmentRegistrarMovimientoBinding? = null
    private val binding get() = _binding!!

    /** Producto que ya va en la lista (viene del Detalle del Producto). Args.SIN_ID (-1) = ninguno. */
    private val idProductoInicial: Long by lazy { arguments?.getLong(Args.ID_PRODUCTO, Args.SIN_ID) ?: Args.SIN_ID }

    // Aquí se conecta todo: botones, listas y datos.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegistrarMovimientoBinding.bind(view)
        binding.toolbar.setNavigationOnClickListener { volver() }
        binding.tvInfo.text =
            if (idProductoInicial == Args.SIN_ID) getString(R.string.new_item) else getString(R.string.received_id, idProductoInicial)
    }

    // Se suelta el binding cuando la vista se destruye.
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
