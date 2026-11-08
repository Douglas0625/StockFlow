
package com.leon.stockflow.ui.products

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.leon.stockflow.R
import com.leon.stockflow.data.entity.Categoria
import com.leon.stockflow.databinding.FragmentProductosBinding
import com.leon.stockflow.ui.common.nombreCategoriaLocalizado
import com.leon.stockflow.ui.common.irADetalleProducto
import com.leon.stockflow.ui.common.irAFormularioProducto
import kotlinx.coroutines.launch

class ProductosFragment : Fragment(R.layout.fragment_productos) {

    // ViewBinding de la pantalla.
    private var _binding: FragmentProductosBinding? = null
    private val binding get() = _binding!!

    // ViewModel que consulta los productos y categorías de Room.
    private val viewModel: ProductosViewModel by viewModels()

    // Adaptador para mostrar los productos.
    private val productoAdapter = ProductoAdapter { idProducto ->

        // Abrir el detalle del producto seleccionado.
        irADetalleProducto(idProducto)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentProductosBinding.bind(view)

        // 1. Configurar la lista de productos.
        binding.rvProductos.layoutManager =
            LinearLayoutManager(requireContext())

        binding.rvProductos.adapter = productoAdapter

        // Restaurar la búsqueda anterior.
        binding.etBuscarProducto.setText(
            viewModel.busquedaActual
        )


        // 2. Abrir el formulario para agregar productos.
        binding.btnAgregarProducto.setOnClickListener {
            irAFormularioProducto()
        }

        // 3. Buscar productos mientras el usuario escribe.
        binding.etBuscarProducto.doAfterTextChanged { texto ->

            viewModel.buscarProducto(
                texto?.toString().orEmpty()
            )
        }

        // 4. Mostrar productos de todas las categorías.
        binding.chipTodos.setOnClickListener {
            viewModel.filtrarPorCategoria(null)
        }

        // 5. Escuchar los productos y categorías de Room.
        observarDatos()
    }

    // Consultar y observar información de Room.
    private fun observarDatos() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                // Observar la lista de productos.
                launch {

                    viewModel.productos.collect { productos ->

                        // Actualizar las tarjetas.
                        productoAdapter.submitList(productos)

                        // Mostrar mensaje si no hay resultados.
                        binding.tvEstadoVacio.visibility =
                            if (productos.isEmpty()) {
                                View.VISIBLE
                            } else {
                                View.GONE
                            }
                    }
                }

                // Observar las categorías disponibles.
                launch {

                    viewModel.categorias.collect { categorias ->

                        // Mostrar los filtros dinámicamente.
                        mostrarCategorias(categorias)
                    }
                }
            }
        }
    }

    // Crear los filtros según las categorías registradas.

    private fun mostrarCategorias(categorias: List<Categoria>) {

        val grupo = binding.chipGroupCategorias

        // Eliminar chips anteriores, excepto Todos.
        if (grupo.childCount > 1) {
            grupo.removeViews(1, grupo.childCount - 1)
        }

        // Recuperar la categoría que estaba seleccionada.
        val categoriaActual = viewModel.categoriaActual

        categorias.forEach { categoria ->

            val chip = Chip(requireContext()).apply {

                id = View.generateViewId()
                text = nombreCategoriaLocalizado(requireContext(), categoria.nombre)
                isCheckable = true
                isCheckedIconVisible = false

                setOnClickListener {
                    viewModel.filtrarPorCategoria(categoria.id)
                }
            }

            grupo.addView(chip)

            // Mantener marcada la categoría seleccionada.
            if (categoria.id == categoriaActual) {
                chip.isChecked = true
            }
        }

        // Marcar Todos solamente si no hay categoría activa.
        binding.chipTodos.isChecked =
            categoriaActual == null
    }


    // Liberar las referencias al destruir la vista.
    override fun onDestroyView() {

        binding.rvProductos.adapter = null

        _binding = null

        super.onDestroyView()
    }
}
