package com.leon.stockflow.ui.movements

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.leon.stockflow.R
import com.leon.stockflow.databinding.FragmentMovimientosBinding
import com.leon.stockflow.ui.common.irADetalleMovimiento
import com.leon.stockflow.ui.common.irARegistrarMovimiento

/**
 * PANTALLA: Movimientos (pestaña de la barra inferior). Historial de entradas y salidas.
 *
 * QUÉ DEBE TENER (según Figma): buscador "Buscar producto...", chips "Todos / Entradas /
 * Salidas", y la lista "Recientes" con título del movimiento, observación, fecha, cantidad
 * total (+N verde / -N rojo) y etiqueta ENTRADA / SALIDA. Un movimiento puede tener varios
 * productos: se muestra "Primer producto y N más". Además, un botón flotante "+".
 *  - Botón flotante "+"   ->  irARegistrarMovimiento()
 *  - Tocar un movimiento  ->  irADetalleMovimiento(movimiento.id)
 *
 * DATOS QUE YA EXISTEN:
 *  - db.movimientoDao().obtenerTodos()            Flow<List<MovimientoConDetalles>>
 *  - db.movimientoDao().obtenerPorTipo(tipo)      Flow<List<MovimientoConDetalles>>  (filtro)
 *        cada resultado trae .movimiento y .detalles (cada detalle: .detalle.cantidad y .producto).
 *
 * OJO: todavía NO hay un DAO que busque movimientos por nombre de producto. Se puede filtrar la
 * lista en el ViewModel (detalles.any { it.producto.nombre.contains(texto, ignoreCase = true) })
 * o pedir a quien mantiene data/ que agregue la consulta.
 *
 * REUTILIZAR: MovimientoAdapter y la conversión a texto de MainViewModel (hoy es privada: si se
 * va a usar en dos lugares, moverla a ui/common en vez de copiarla).

 * ANTES DE EMPEZAR: leer InicioFragment.kt y MainViewModel.kt (ejemplo completo y comentado).
 * Si hay botones marcados "TEMPORAL", solo prueban la navegación: reemplazarlos por el diseño.
 * Pasos: (1) reemplazar el layout fragment_xxx.xml por el diseño de Figma, (2) crear un
 * ViewModel copiando el patrón de MainViewModel, (3) mostrar los datos en el Fragment,
 * (4) borrar los botones temporales y la línea "En construcción".
 */
class MovimientosFragment : Fragment(R.layout.fragment_movimientos) {

    // Binding: acceso a las vistas del layout (ver el ejemplo comentado en InicioFragment).
    private var _binding: FragmentMovimientosBinding? = null
    private val binding get() = _binding!!

    // Aquí se conecta todo: botones, listas y datos.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMovimientosBinding.bind(view)
        // TEMPORAL: botones de prueba. Quien haga esta pantalla los reemplaza por lo que pide el diseño.
        binding.btnRegistrar.setOnClickListener { irARegistrarMovimiento() }
        binding.btnDetalle.setOnClickListener { irADetalleMovimiento(1L) }
    }

    // Se suelta el binding cuando la vista se destruye.
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
