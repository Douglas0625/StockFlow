package com.leon.stockflow.ui.movements

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.leon.stockflow.R
import com.leon.stockflow.databinding.ActivityMovimientoDetalleBinding
import com.leon.stockflow.ui.common.aplicarInsetsSistema
import com.leon.stockflow.ui.common.configurarBarraInferior


class MovimientoDetalleActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovimientoDetalleBinding

    private val idMovimiento: Long by lazy { intent.getLongExtra(EXTRA_ID_MOVIMIENTO, -1L) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMovimientoDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.aplicarInsetsSistema()
        // Pantalla secundaria: resalta la pestaña de la que viene (esPantallaRaiz = false)
        configurarBarraInferior(binding.bottomNav, R.id.nav_movimientos, esPantallaRaiz = false)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.tvInfo.text = getString(R.string.received_id, idMovimiento)
    }

    companion object {
        private const val EXTRA_ID_MOVIMIENTO = "id_movimiento"

        fun crearIntent(context: Context, idMovimiento: Long): Intent =
            Intent(context, MovimientoDetalleActivity::class.java).putExtra(EXTRA_ID_MOVIMIENTO, idMovimiento)
    }
}
