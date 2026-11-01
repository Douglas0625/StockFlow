package com.leon.stockflow.ui.products

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.leon.stockflow.R
import com.leon.stockflow.databinding.ActivityProductosBinding
import com.leon.stockflow.ui.common.aplicarInsetsSistema
import com.leon.stockflow.ui.common.configurarBarraInferior

class ProductosActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductosBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProductosBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.aplicarInsetsSistema()
        configurarBarraInferior(binding.bottomNav, R.id.nav_productos)

        // TEMPORAL: botones de prueba. Quien haga esta pantalla los reemplaza por la lista y el botón "+".
        binding.btnAgregar.setOnClickListener {
            startActivity(ProductoFormularioActivity.crearIntent(this))
        }
        binding.btnDetalle.setOnClickListener {
            startActivity(ProductoDetalleActivity.crearIntent(this, 1L))
        }
    }
}
