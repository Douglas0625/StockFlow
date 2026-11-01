package com.leon.stockflow.ui.products

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.leon.stockflow.R
import com.leon.stockflow.databinding.FragmentProductosBinding
import com.leon.stockflow.ui.common.irADetalleProducto
import com.leon.stockflow.ui.common.irAFormularioProducto

class ProductosFragment : Fragment(R.layout.fragment_productos) {

    private var _binding: FragmentProductosBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProductosBinding.bind(view)
        // TEMPORAL: botones de prueba. Quien haga esta pantalla los reemplaza por lo que pide el diseño.
        binding.btnAgregar.setOnClickListener { irAFormularioProducto() }
        binding.btnDetalle.setOnClickListener { irADetalleProducto(1L) }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
