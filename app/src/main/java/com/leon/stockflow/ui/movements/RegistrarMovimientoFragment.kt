package com.leon.stockflow.ui.movements

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.leon.stockflow.R
import com.leon.stockflow.databinding.FragmentRegistrarMovimientoBinding
import com.leon.stockflow.ui.common.Args
import com.leon.stockflow.ui.common.volver

class RegistrarMovimientoFragment : Fragment(R.layout.fragment_registrar_movimiento) {

    private var _binding: FragmentRegistrarMovimientoBinding? = null
    private val binding get() = _binding!!

    /** Producto que ya va en la lista (viene del Detalle del Producto). Args.SIN_ID (-1) = ninguno. */
    private val idProductoInicial: Long by lazy { arguments?.getLong(Args.ID_PRODUCTO, Args.SIN_ID) ?: Args.SIN_ID }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegistrarMovimientoBinding.bind(view)
        binding.toolbar.setNavigationOnClickListener { volver() }
        binding.tvInfo.text =
            if (idProductoInicial == Args.SIN_ID) getString(R.string.new_item) else getString(R.string.received_id, idProductoInicial)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
