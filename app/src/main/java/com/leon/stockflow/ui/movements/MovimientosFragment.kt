package com.leon.stockflow.ui.movements

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.leon.stockflow.R
import com.leon.stockflow.data.model.TipoMovimiento
import com.leon.stockflow.databinding.FragmentMovimientosBinding
import com.leon.stockflow.ui.common.irADetalleMovimiento
import com.leon.stockflow.ui.common.irARegistrarMovimiento
import com.leon.stockflow.ui.home.MovimientoAdapter
import kotlinx.coroutines.launch

/**
 * PANTALLA: Movimientos (pestaña de la barra inferior). Historial de entradas y salidas.
 *   - Buscador por nombre de producto.
 *   - Chips Todos / Entradas / Salidas.
 *   - Botón flotante "+"   ->  irARegistrarMovimiento()
 *   - Tocar un movimiento  ->  irADetalleMovimiento(id)
 */

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

    private var _binding: FragmentMovimientosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MovimientosViewModel by viewModels()

    // Reutiliza el adapter de Inicio. Al tocar una fila se abre el detalle de ese movimiento.
    private val adapter = MovimientoAdapter { idMovimiento -> irADetalleMovimiento(idMovimiento) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMovimientosBinding.bind(view)

        binding.rvMovimientos.adapter = adapter

        // Restaurar filtros ANTES de poner los listeners (así no se disparan al restaurar).
        binding.etBuscar.setText(viewModel.busquedaActual)
        binding.chipGroupTipo.check(
            when (viewModel.tipoActual) {
                TipoMovimiento.ENTRADA -> R.id.chipEntradas
                TipoMovimiento.SALIDA -> R.id.chipSalidas
                null -> R.id.chipTodos
            }
        )

        // Buscar mientras se escribe.
        binding.etBuscar.doAfterTextChanged { texto ->
            viewModel.buscar(texto?.toString().orEmpty())
        }

        // Cambiar el filtro por tipo.
        binding.chipGroupTipo.setOnCheckedStateChangeListener { _, ids ->
            val tipo = when (ids.firstOrNull()) {
                R.id.chipEntradas -> TipoMovimiento.ENTRADA
                R.id.chipSalidas -> TipoMovimiento.SALIDA
                else -> null
            }
            viewModel.filtrarPorTipo(tipo)
        }

        // Botón flotante: registrar un movimiento nuevo.
        binding.fabRegistrar.setOnClickListener { irARegistrarMovimiento() }

        observarDatos()
    }

    // Escucha la lista del ViewModel y actualiza la pantalla cuando cambia (Room avisa solo).
    private fun observarDatos() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.movimientos.collect { lista ->
                    adapter.submitList(lista)
                    binding.tvVacio.isVisible = lista.isEmpty()
                }
            }
        }
    }

    override fun onDestroyView() {
        binding.rvMovimientos.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
