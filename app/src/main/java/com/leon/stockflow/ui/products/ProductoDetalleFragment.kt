package com.leon.stockflow.ui.products

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.leon.stockflow.R
import com.leon.stockflow.databinding.FragmentProductoDetalleBinding
import com.leon.stockflow.ui.common.Args
import com.leon.stockflow.ui.common.irAFormularioProducto
import com.leon.stockflow.ui.common.irARegistrarMovimiento
import com.leon.stockflow.ui.common.volver

class ProductoDetalleFragment : Fragment(R.layout.fragment_producto_detalle) {

    private var _binding: FragmentProductoDetalleBinding? = null
    private val binding get() = _binding!!

    /** Id del producto que se está mostrando. */
    private val idProducto: Long by lazy { arguments?.getLong(Args.ID_PRODUCTO, Args.SIN_ID) ?: Args.SIN_ID }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProductoDetalleBinding.bind(view)
        binding.toolbar.setNavigationOnClickListener { volver() }
        binding.tvInfo.text = getString(R.string.received_id, idProducto)

        // TEMPORAL: botones de prueba. Quien haga esta pantalla los reemplaza por lo que pide el diseño.
        binding.btnEditar.setOnClickListener { irAFormularioProducto(idProducto) }
        binding.btnMovimiento.setOnClickListener { irARegistrarMovimiento(idProducto) }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
