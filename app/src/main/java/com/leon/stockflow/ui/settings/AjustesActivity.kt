package com.leon.stockflow.ui.settings

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.leon.stockflow.R
import com.leon.stockflow.databinding.ActivityAjustesBinding
import com.leon.stockflow.ui.common.aplicarInsetsSistema
import com.leon.stockflow.ui.common.configurarBarraInferior

class AjustesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAjustesBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAjustesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.root.aplicarInsetsSistema()
        configurarBarraInferior(binding.bottomNav, R.id.nav_ajustes)
    }
}
