package com.leon.stockflow.ui.movements

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.leon.stockflow.R
import com.leon.stockflow.databinding.ActivityMovimientosBinding
import com.leon.stockflow.ui.common.aplicarInsetsSistema
import com.leon.stockflow.ui.common.configurarBarraInferior

class MovimientosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovimientosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMovimientosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.aplicarInsetsSistema()
        configurarBarraInferior(binding.bottomNav, R.id.nav_movimientos)

        // TEMPORAL: botones de prueba. Quien haga esta pantalla los reemplaza por la lista y el botón "+".
        binding.btnRegistrar.setOnClickListener {
            startActivity(RegistrarMovimientoActivity.crearIntent(this))
        }
        binding.btnDetalle.setOnClickListener {
            startActivity(MovimientoDetalleActivity.crearIntent(this, 1L))
        }
    }
}
