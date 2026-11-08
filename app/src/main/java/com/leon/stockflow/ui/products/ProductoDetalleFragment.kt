
package com.leon.stockflow.ui.products

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.leon.stockflow.R
import com.leon.stockflow.databinding.FragmentProductoDetalleBinding
import com.leon.stockflow.ui.common.nombreCategoriaLocalizado
import com.leon.stockflow.ui.common.Args
import com.leon.stockflow.ui.common.irAFormularioProducto
import com.leon.stockflow.ui.common.irARegistrarMovimiento
import com.leon.stockflow.ui.common.volver
import kotlinx.coroutines.launch
import androidx.recyclerview.widget.LinearLayoutManager
import android.net.Uri
import java.io.File
import androidx.core.content.ContextCompat


class ProductoDetalleFragment :
    Fragment(R.layout.fragment_producto_detalle) {

    private var _binding: FragmentProductoDetalleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProductosViewModel by viewModels()

    private val idProducto: Long by lazy {
        arguments?.getLong(
            Args.ID_PRODUCTO,
            Args.SIN_ID
        ) ?: Args.SIN_ID
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentProductoDetalleBinding.bind(view)


// Preparar el historial de movimientos.
        val historialAdapter = HistorialProductoAdapter()

        binding.rvHistorialProducto.layoutManager =
            LinearLayoutManager(requireContext())

        binding.rvHistorialProducto.adapter = historialAdapter


// Observar el historial de movimientos del producto.
        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                viewModel.observarHistorialProducto(idProducto)
                    .collect { movimientos ->

                        // Mostrar mensaje cuando no haya movimientos.
                        binding.tvHistorialVacio.visibility =
                            if (movimientos.isEmpty()) {
                                View.VISIBLE
                            } else {
                                View.GONE
                            }

                        // Mostrar la lista cuando existan movimientos.
                        binding.rvHistorialProducto.visibility =
                            if (movimientos.isEmpty()) {
                                View.GONE
                            } else {
                                View.VISIBLE
                            }

                        // Actualizar las filas del historial.
                        historialAdapter.submitList(movimientos)
                    }
            }
        }



        // Regresar al inventario.
        binding.toolbar.setNavigationOnClickListener {
            volver()
        }

        // Editar el producto seleccionado.
        binding.btnEditar.setOnClickListener {
            irAFormularioProducto(idProducto)
        }

        // Abrir el registro de movimientos.
        binding.btnMovimiento.setOnClickListener {
            irARegistrarMovimiento(idProducto)
        }

        // Mostrar información actualizada desde Room.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {
                viewModel.observarProducto(idProducto).collect { item ->


                    if (item == null) {

                        binding.tvNombreDetalle.text =
                            getString(R.string.producto_no_encontrado)

                        binding.ivFotoDetalle.setImageDrawable(null)
                        binding.ivFotoDetalle.visibility = View.GONE
                        binding.layoutFotoPlaceholder.visibility = View.VISIBLE


                        binding.tvCategoriaDetalle.text = ""
                        binding.tvDescripcionDetalle.text = ""
                        binding.tvPrecioCompraDetalle.text = ""
                        binding.tvPrecioVentaDetalle.text = ""
                        binding.tvStockActualDetalle.text = ""
                        binding.tvStockMinimoDetalle.text = ""

                        binding.btnEditar.isEnabled = false
                        binding.btnMovimiento.isEnabled = false

                    } else {

                        val producto = item.producto


// Mostrar fotografía del producto.
                        val fotoUri = producto.fotoUri

                        val uri = fotoUri?.takeIf {
                            it.isNotBlank()
                        }?.let {
                            Uri.parse(it)
                        }

                        val fotoExiste = uri != null &&
                                uri.scheme == "file" &&
                                uri.path?.let { File(it).isFile } == true

                        if (fotoExiste) {

                            binding.ivFotoDetalle.setImageURI(uri)
                            binding.ivFotoDetalle.visibility = View.VISIBLE

                            binding.layoutFotoPlaceholder.visibility = View.GONE

                        } else {

                            binding.ivFotoDetalle.setImageDrawable(null)
                            binding.ivFotoDetalle.visibility = View.GONE

                            binding.layoutFotoPlaceholder.visibility = View.VISIBLE
                        }


                        // Nombre y categoría.
                        binding.tvNombreDetalle.text = producto.nombre
                        binding.tvCategoriaDetalle.text = nombreCategoriaLocalizado(requireContext(), item.nombreCategoria)

                        // Descripción.
                        binding.tvDescripcionDetalle.text =
                            producto.descripcion.ifBlank {
                                getString(R.string.detalle_sin_descripcion)
                            }

                        // Precio de compra.
                        binding.tvPrecioCompraDetalle.text = getString(
                            R.string.detalle_formato_precio,
                            producto.precioCompra
                        )

                        // Precio de venta.
                        binding.tvPrecioVentaDetalle.text = getString(
                            R.string.detalle_formato_precio,
                            producto.precioVenta
                        )

                        // Stock actual.
                        binding.tvStockActualDetalle.text = getString(
                            R.string.detalle_unidades,
                            producto.cantidadStock
                        )

                        // Stock mínimo.
                        binding.tvStockMinimoDetalle.text = getString(
                            R.string.detalle_unidades,
                            producto.stockMinimo
                        )


                        // Comprobar si el producto tiene stock bajo.
                        val stockBajo =
                            producto.cantidadStock <= producto.stockMinimo

                        // Destacar en naranja el stock mínimo
                        // cuando las existencias son insuficientes.
                        val colorStockMinimo = if (stockBajo) {

                            ContextCompat.getColor(
                                requireContext(),
                                R.color.stockflow_warning
                            )

                        } else {

                            // Recuperar el color normal del tema.
                            binding.tvNombreDetalle.currentTextColor
                        }

                        // Aplicar el color correspondiente.
                        binding.tvStockMinimoDetalle.setTextColor(
                            colorStockMinimo
                        )


                        // Habilitar botones.
                        binding.btnEditar.isEnabled = true
                        binding.btnMovimiento.isEnabled = true
                    }

                }
            }
        }
    }

    override fun onDestroyView() {
        binding.rvHistorialProducto.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
