package com.leon.stockflow.ui.products

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.leon.stockflow.R
import com.leon.stockflow.databinding.ActivityProductoFormularioBinding
import com.leon.stockflow.ui.common.aplicarInsetsSistema
import com.leon.stockflow.ui.common.configurarBarraInferior


class ProductoFormularioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductoFormularioBinding

    /** -1 = producto nuevo; cualquier otro valor = editar ese producto. */
    private val idProducto: Long by lazy { intent.getLongExtra(EXTRA_ID_PRODUCTO, -1L) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProductoFormularioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.aplicarInsetsSistema()
        // Pantalla secundaria: resalta la pestaña de la que viene (esPantallaRaiz = false)
        configurarBarraInferior(binding.bottomNav, R.id.nav_productos, esPantallaRaiz = false)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.tvInfo.text =
            if (idProducto == -1L) getString(R.string.new_item) else getString(R.string.received_id, idProducto)
    }

    companion object {
        private const val EXTRA_ID_PRODUCTO = "id_producto"

        /** Sin id = agregar producto. Con id = editar ese producto. */
        fun crearIntent(context: Context, idProducto: Long? = null): Intent =
            Intent(context, ProductoFormularioActivity::class.java).apply {
                if (idProducto != null) putExtra(EXTRA_ID_PRODUCTO, idProducto)
            }
    }
}
