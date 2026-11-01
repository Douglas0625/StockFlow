package com.leon.stockflow.ui.home

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.leon.stockflow.R
import com.leon.stockflow.databinding.FragmentInicioBinding
import com.leon.stockflow.ui.common.irADetalleMovimiento
import com.leon.stockflow.ui.common.irAPestana
import kotlinx.coroutines.launch

/**
 * PANTALLA: Inicio. ESTE ARCHIVO ES EL EJEMPLO a copiar para las demás pantallas.
 *
 * Cómo está armada una pantalla en StockFlow (3 piezas):
 *   1. Layout (res/layout/fragment_inicio.xml): cómo se ve. Solo diseño, sin lógica.
 *   2. Fragment (este archivo): conecta el layout con los botones y muestra los datos.
 *      NO habla con la base de datos directamente.
 *   3. ViewModel (MainViewModel.kt): obtiene los datos de la base y los deja listos
 *      para mostrar. Sobrevive a rotar la pantalla, por eso los datos no se pierden.
 *
 * Flujo de los datos:   Base de datos (Room)  ->  ViewModel  ->  Fragment  ->  Pantalla
 *
 * Ciclo de vida de un Fragment (lo único que hay que saber):
 *   - onViewCreated: la pantalla ya existe. Aquí se conecta TODO (binding, botones, datos).
 *   - onDestroyView: la pantalla se va (aunque el Fragment siga en memoria). Aquí se suelta
 *     el binding para no gastar memoria.
 *
 * Reglas del equipo:
 *   - Textos siempre en res/values/strings.xml, nunca escritos dentro del layout o del código.
 *   - Colores del tema (?attr/colorSurface, ?attr/colorOnSurface...) y no @color/stockflow_xxx,
 *     para que el modo oscuro funcione. Solo los colores de estado (stockflow_success,
 *     stockflow_warning, stockflow_error) se pueden usar directo.
 *   - Para abrir otra pantalla, las funciones de ui/common/Navegacion.kt.
 */
// Fragment(R.layout.fragment_inicio) le dice a Android qué layout dibujar en esta pantalla.
class InicioFragment : Fragment(R.layout.fragment_inicio) {

    // ViewBinding: da acceso a las vistas del layout (binding.tvVerTodos, binding.rvMovimientos...).
    // _binding puede ser null (cuando la vista no existe); "binding" es el atajo para usarlo
    // mientras la vista SÍ existe. Este mismo par de líneas se copia en todas las pantallas.
    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!

    // "by viewModels()" crea (o recupera) el ViewModel de esta pantalla.
    private val viewModel: MainViewModel by viewModels()

    // El adapter dibuja cada fila de la lista (ver MovimientoAdapter.kt). Se le pasa qué hacer
    // cuando el usuario toca una fila: abrir el detalle de ese movimiento.
    private val adapter = MovimientoAdapter { idMovimiento -> irADetalleMovimiento(idMovimiento) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Conecta el binding con la vista que Android ya creó.
        _binding = FragmentInicioBinding.bind(view)

        // viewModel.cargarDatosDePrueba() // TEMPORAL: descomentar, correr UNA vez y volver a comentar

        // Une la lista de la pantalla (RecyclerView) con su adapter.
        binding.rvMovimientos.adapter = adapter
        // "VER TODOS" cambia a la pestaña Movimientos, igual que tocar la barra inferior.
        binding.tvVerTodos.setOnClickListener { irAPestana(R.id.movimientosFragment) }
        observarDatos()
    }

    /**
     * Escucha los datos del ViewModel y actualiza la pantalla cada vez que cambian.
     *
     * Los datos llegan como "Flow": una tubería que avisa cada vez que hay un valor nuevo.
     * Si se registra un movimiento en cualquier parte de la app, Room lo detecta y estas
     * líneas actualizan Inicio solas, sin que nadie las llame.
     *
     * repeatOnLifecycle(STARTED) = "solo escucha mientras la pantalla está visible".
     * Cada launch { ... } es una escucha independiente (una por cada dato).
     */
    private fun observarDatos() {
        // viewLifecycleOwner = "la vida de la vista": se cancela solo cuando la vista se destruye
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.totalUnidades.collect { binding.tvTotalUnidades.text = "%,d".format(it) }
                }
                launch {
                    viewModel.totalProductos.collect { binding.tvTotalProductos.text = it.toString() }
                }
                launch {
                    viewModel.stockBajo.collect { binding.tvStockBajo.text = it.toString() }
                }
                launch {
                    viewModel.movimientosRecientes.collect { lista ->
                        adapter.submitList(lista)
                        binding.tvVacio.isVisible = lista.isEmpty()
                    }
                }
            }
        }
    }

    // La vista se destruye: se suelta todo lo que apunta a ella para evitar fugas de memoria.
    override fun onDestroyView() {
        binding.rvMovimientos.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
