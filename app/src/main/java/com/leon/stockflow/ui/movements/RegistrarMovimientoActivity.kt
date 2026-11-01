package com.leon.stockflow.ui.movements

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.leon.stockflow.R
import com.leon.stockflow.databinding.ActivityRegistrarMovimientoBinding
import com.leon.stockflow.ui.common.aplicarInsetsSistema
import com.leon.stockflow.ui.common.configurarBarraInferior


class RegistrarMovimientoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrarMovimientoBinding

    /** -1 = sin producto inicial; si viene del Detalle del Producto, ese producto ya va en la lista. */
    private val idProductoInicial: Long by lazy { intent.getLongExtra(EXTRA_ID_PRODUCTO, -1L) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegistrarMovimientoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.aplicarInsetsSistema()
        // Pantalla secundaria: resalta la pestaña de la que viene (esPantallaRaiz = false)
        configurarBarraInferior(binding.bottomNav, R.id.nav_movimientos, esPantallaRaiz = false)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.tvInfo.text =
            if (idProductoInicial == -1L) getString(R.string.new_item) else getString(R.string.received_id, idProductoInicial)
    }

    companion object {
        private const val EXTRA_ID_PRODUCTO = "id_producto"

        fun crearIntent(context: Context, idProductoInicial: Long? = null): Intent =
            Intent(context, RegistrarMovimientoActivity::class.java).apply {
                if (idProductoInicial != null) putExtra(EXTRA_ID_PRODUCTO, idProductoInicial)
            }
    }
}
