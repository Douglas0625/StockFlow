package com.leon.stockflow

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.leon.stockflow.databinding.ActivityMainBinding
import com.leon.stockflow.ui.home.MainViewModel
import com.leon.stockflow.ui.home.MovimientoAdapter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private val adapter = MovimientoAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // viewModel.cargarDatosDePrueba() // TEMPORAL: descomentar, correr UNA vez y volver a comentar

        binding.rvMovimientos.adapter = adapter
        configurarBarraInferior()
        configurarClicks()
        observarDatos()
    }

    private fun configurarClicks() {
        binding.tvVerTodos.setOnClickListener {
            // TODO: abrir la pantalla de Movimientos
            mostrarProximamente()
        }
    }

    private fun configurarBarraInferior() {
        binding.bottomNav.selectedItemId = R.id.nav_inicio
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> true
                else -> {
                    // TODO: reemplazar por startActivity(...) cuando existan las otras pantallas
                    mostrarProximamente()
                    false // se queda marcado "Inicio"
                }
            }
        }
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

    private fun mostrarProximamente() {
        Toast.makeText(this, R.string.coming_soon, Toast.LENGTH_SHORT).show()
    }
}