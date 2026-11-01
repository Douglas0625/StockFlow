package com.leon.stockflow.ui.movements

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.leon.stockflow.R
import com.leon.stockflow.databinding.FragmentMovimientoDetalleBinding
import com.leon.stockflow.ui.common.Args
import com.leon.stockflow.ui.common.volver

class MovimientoDetalleFragment : Fragment(R.layout.fragment_movimiento_detalle) {

    private var _binding: FragmentMovimientoDetalleBinding? = null
    private val binding get() = _binding!!

    /** Id del movimiento que se está mostrando. */
    private val idMovimiento: Long by lazy { arguments?.getLong(Args.ID_MOVIMIENTO, Args.SIN_ID) ?: Args.SIN_ID }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMovimientoDetalleBinding.bind(view)
        binding.toolbar.setNavigationOnClickListener { volver() }
        binding.tvInfo.text = getString(R.string.received_id, idMovimiento)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
