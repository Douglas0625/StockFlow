package com.leon.stockflow

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.leon.stockflow.databinding.ActivityMainBinding
import com.leon.stockflow.ui.common.aplicarInsetsSistema
import com.leon.stockflow.ui.common.configurarBarraInferior
import com.leon.stockflow.ui.common.irAPestana
import com.leon.stockflow.ui.home.MainViewModel
import com.leon.stockflow.ui.home.MovimientoAdapter
import com.leon.stockflow.ui.movements.MovimientoDetalleActivity
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    // Al tocar un movimiento se abre su detalle
    private val adapter = MovimientoAdapter { idMovimiento ->
        startActivity(MovimientoDetalleActivity.crearIntent(this, idMovimiento))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.main.aplicarInsetsSistema()

        // viewModel.cargarDatosDePrueba() // TEMPORAL: descomentar, correr UNA vez y volver a comentar

        binding.rvMovimientos.adapter = adapter
        configurarBarraInferior(binding.bottomNav, R.id.nav_inicio)
        binding.tvVerTodos.setOnClickListener { irAPestana(R.id.nav_movimientos) }
        observarDatos()
    }

    private fun observarDatos() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
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
}
