package com.leon.stockflow.ui.movements

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.leon.stockflow.R
import com.leon.stockflow.databinding.FragmentMovimientosBinding
import com.leon.stockflow.ui.common.irADetalleMovimiento
import com.leon.stockflow.ui.common.irARegistrarMovimiento

class MovimientosFragment : Fragment(R.layout.fragment_movimientos) {

    private var _binding: FragmentMovimientosBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMovimientosBinding.bind(view)
        // TEMPORAL: botones de prueba. Quien haga esta pantalla los reemplaza por lo que pide el diseño.
        binding.btnRegistrar.setOnClickListener { irARegistrarMovimiento() }
        binding.btnDetalle.setOnClickListener { irADetalleMovimiento(1L) }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
