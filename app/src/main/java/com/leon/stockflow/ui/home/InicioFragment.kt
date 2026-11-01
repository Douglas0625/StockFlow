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
 * Pantalla Inicio. ESTE ARCHIVO ES EL EJEMPLO a seguir para las demás pantallas:
 *  - Fragment(R.layout.fragment_inicio): le indica qué layout usar.
 *  - onViewCreated: aquí se conecta todo (binding, botones, datos).
 *  - onDestroyView: se suelta el binding para no gastar memoria.
 */
class InicioFragment : Fragment(R.layout.fragment_inicio) {

    private var _binding: FragmentInicioBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels()

    // Al tocar un movimiento se abre su detalle
    private val adapter = MovimientoAdapter { idMovimiento -> irADetalleMovimiento(idMovimiento) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentInicioBinding.bind(view)

        // viewModel.cargarDatosDePrueba() // TEMPORAL: descomentar, correr UNA vez y volver a comentar

        binding.rvMovimientos.adapter = adapter
        binding.tvVerTodos.setOnClickListener { irAPestana(R.id.movimientosFragment) }
        observarDatos()
    }

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

    override fun onDestroyView() {
        binding.rvMovimientos.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
