package com.leon.stockflow.ui.products

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.leon.stockflow.R
import com.leon.stockflow.databinding.FragmentProductoFormularioBinding
import com.leon.stockflow.ui.common.Args
import com.leon.stockflow.ui.common.volver

class ProductoFormularioFragment : Fragment(R.layout.fragment_producto_formulario) {

    private var _binding: FragmentProductoFormularioBinding? = null
    private val binding get() = _binding!!

    /** Id del producto a editar. Args.SIN_ID (-1) = producto nuevo. */
    private val idProducto: Long by lazy { arguments?.getLong(Args.ID_PRODUCTO, Args.SIN_ID) ?: Args.SIN_ID }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProductoFormularioBinding.bind(view)
        binding.toolbar.setNavigationOnClickListener { volver() }
        binding.tvInfo.text =
            if (idProducto == Args.SIN_ID) getString(R.string.new_item) else getString(R.string.received_id, idProducto)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
