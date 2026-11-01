package com.leon.stockflow.ui.products

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.leon.stockflow.R
import com.leon.stockflow.databinding.ActivityProductoDetalleBinding
import com.leon.stockflow.ui.common.aplicarInsetsSistema
import com.leon.stockflow.ui.common.configurarBarraInferior
import com.leon.stockflow.ui.movements.RegistrarMovimientoActivity


class ProductoDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductoDetalleBinding

    private val idProducto: Long by lazy { intent.getLongExtra(EXTRA_ID_PRODUCTO, -1L) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProductoDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.aplicarInsetsSistema()
        // Pantalla secundaria: resalta la pestaña de la que viene (esPantallaRaiz = false)
        configurarBarraInferior(binding.bottomNav, R.id.nav_productos, esPantallaRaiz = false)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.tvInfo.text = getString(R.string.received_id, idProducto)

        // TEMPORAL: reemplazar por los botones del diseño ("Editar Producto" y "Registrar Movimiento")
        binding.btnEditar.setOnClickListener {
            startActivity(ProductoFormularioActivity.crearIntent(this, idProducto))
        }
        binding.btnMovimiento.setOnClickListener {
            startActivity(RegistrarMovimientoActivity.crearIntent(this, idProducto))
        }
    }

    companion object {
        private const val EXTRA_ID_PRODUCTO = "id_producto"

        fun crearIntent(context: Context, idProducto: Long): Intent =
            Intent(context, ProductoDetalleActivity::class.java).putExtra(EXTRA_ID_PRODUCTO, idProducto)
    }
}
