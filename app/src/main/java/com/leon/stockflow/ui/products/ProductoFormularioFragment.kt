package com.leon.stockflow.ui.products

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.leon.stockflow.R
import com.leon.stockflow.data.entity.Categoria
import com.leon.stockflow.data.entity.Producto
import com.leon.stockflow.databinding.FragmentProductoFormularioBinding
import com.leon.stockflow.ui.common.nombreCategoriaLocalizado
import com.leon.stockflow.ui.common.Args
import com.leon.stockflow.ui.common.volver
import kotlinx.coroutines.launch


class ProductoFormularioFragment :
    Fragment(R.layout.fragment_producto_formulario) {

    private var _binding: FragmentProductoFormularioBinding? = null
    private val binding get() = _binding!!

    private val productosViewModel: ProductosViewModel by viewModels()

    // Args.SIN_ID indica que se registrará un producto nuevo.
    private val idProducto: Long by lazy {
        arguments?.getLong(
            Args.ID_PRODUCTO,
            Args.SIN_ID
        ) ?: Args.SIN_ID
    }

    private var categoriasDisponibles: List<Categoria> = emptyList()
    private var idCategoriaSeleccionada: Long? = null

    // Producto existente cuando estamos editando.
    private var productoOriginal: Producto? = null

    // Fotografía seleccionada por el usuario.
    private var fotoUriSeleccionada: Uri? = null

    // Si se abandona el formulario desde la barra inferior, se descarta su back stack guardado.
    private var destinoListener: NavController.OnDestinationChangedListener? = null

    // =====================================
    // 1. SELECCIONAR FOTOGRAFÍA
    // =====================================

    private val seleccionarFoto = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->

        if (uri != null) {

            fotoUriSeleccionada = uri

            _binding?.let { vista ->

                vista.ivVistaPreviaProducto.setImageURI(uri)
                vista.ivVistaPreviaProducto.visibility = View.VISIBLE

                vista.ivFotoFormulario.visibility = View.GONE
                vista.tvSubirFoto.visibility = View.GONE
            }
        }
    }

    // =====================================
    // 2. INICIALIZAR EL FORMULARIO
    // =====================================

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentProductoFormularioBinding.bind(view)

        // NavigationUI conserva el estado de cada pestaña. Si se cambia de pestaña mientras
        // este formulario está abierto, limpiamos el estado guardado de Productos para que al
        // regresar se muestre el inventario y no reaparezca el borrador pendiente.
        val navController = findNavController()
        destinoListener = NavController.OnDestinationChangedListener { controller, destination, _ ->
            if (destination.id != R.id.productoFormularioFragment) {
                controller.clearBackStack(R.id.productosFragment)
            }
        }
        navController.addOnDestinationChangedListener(destinoListener!!)

        // Flecha para regresar.
        binding.toolbar.setNavigationOnClickListener {
            volver()
        }

        // Botón Cancelar.
        binding.btnCancelar.setOnClickListener {
            volver()
        }

        // Al editar, esperar a que carguen los datos.
        binding.btnGuardarProducto.isEnabled =
            idProducto == Args.SIN_ID

        // Permitir fotografías en productos nuevos y existentes.
        binding.cardFotoProducto.isEnabled = true

        binding.cardFotoProducto.setOnClickListener {

            seleccionarFoto.launch(
                PickVisualMediaRequest(
                    ActivityResultContracts.PickVisualMedia.ImageOnly
                )
            )
        }

        // =====================================
        // 3. SELECCIONAR CATEGORÍA
        // =====================================

        binding.actCategoria.setOnItemClickListener { parent, _, posicion, _ ->

            // Resolver por el texto visible, porque el autocompletado puede filtrar la lista.
            val nombreVisible = parent.getItemAtPosition(posicion)?.toString().orEmpty()
            idCategoriaSeleccionada = categoriasDisponibles.firstOrNull { categoria ->
                nombreCategoriaLocalizado(requireContext(), categoria.nombre) == nombreVisible
            }?.id

            // Limpiar el mensaje cuando se selecciona una categoría.
            binding.tilCategoriaProducto.error = null
        }

        // =====================================
        // 4. LIMPIAR ERRORES AL ESCRIBIR
        // =====================================

        binding.etNombreProducto.doAfterTextChanged {
            binding.tilNombreProducto.error = null
        }

        binding.etPrecioCompra.doAfterTextChanged {
            binding.tilPrecioCompra.error = null
        }

        binding.etPrecioVenta.doAfterTextChanged {
            binding.tilPrecioVenta.error = null
        }

        binding.etStockActual.doAfterTextChanged {
            binding.tilStockActual.error = null
        }

        binding.etStockMinimo.doAfterTextChanged {
            binding.tilStockMinimo.error = null
        }

        // Obtener categorías desde Room.
        observarCategorias()

        // Cargar un producto si estamos editando.
        if (idProducto != Args.SIN_ID) {
            cargarProductoExistente()
        }

        // Guardar producto nuevo o editado.
        binding.btnGuardarProducto.setOnClickListener {
            guardarProducto()
        }
    }

    // =====================================
    // 5. OBTENER CATEGORÍAS DESDE ROOM
    // =====================================

    private fun observarCategorias() {

        viewLifecycleOwner.lifecycleScope.launch {

            viewLifecycleOwner.repeatOnLifecycle(
                Lifecycle.State.STARTED
            ) {

                productosViewModel.categorias.collect { categorias ->

                    categoriasDisponibles = categorias

                    val nombres = categorias.map { nombreCategoriaLocalizado(requireContext(), it.nombre) }

                    val adaptador = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_dropdown_item_1line,
                        nombres
                    )

                    binding.actCategoria.setAdapter(adaptador)

                    mostrarCategoriaSeleccionada()
                }
            }
        }
    }

    // Mostrar la categoría existente al editar.
    private fun mostrarCategoriaSeleccionada() {

        val categoria = categoriasDisponibles.firstOrNull {
            it.id == idCategoriaSeleccionada
        }

        categoria?.let {
            binding.actCategoria.setText(nombreCategoriaLocalizado(requireContext(), it.nombre), false)
        }
    }

    // =====================================
    // 6. CARGAR PRODUCTO PARA EDITAR
    // =====================================

    private fun cargarProductoExistente() {

        productosViewModel.cargarProductoParaEditar(
            id = idProducto,

            alCargar = { producto ->

                if (_binding != null && isAdded) {

                    if (producto == null) {

                        Toast.makeText(
                            requireContext(),
                            R.string.producto_no_encontrado,
                            Toast.LENGTH_SHORT
                        ).show()

                        volver()

                    } else {

                        productoOriginal = producto

                        // Mostrar fotografía guardada.
                        producto.fotoUri?.takeIf {
                            it.isNotBlank()
                        }?.let { ruta ->

                            binding.ivVistaPreviaProducto.setImageURI(
                                Uri.parse(ruta)
                            )

                            binding.ivVistaPreviaProducto.visibility =
                                View.VISIBLE

                            binding.ivFotoFormulario.visibility =
                                View.GONE

                            binding.tvSubirFoto.visibility =
                                View.GONE
                        }

                        // Cargar la información del producto.
                        binding.etNombreProducto.setText(
                            producto.nombre
                        )

                        binding.etDescripcionProducto.setText(
                            producto.descripcion
                        )

                        binding.etPrecioCompra.setText(
                            producto.precioCompra.toString()
                        )

                        binding.etPrecioVenta.setText(
                            producto.precioVenta.toString()
                        )

                        binding.etStockActual.setText(
                            producto.cantidadStock.toString()
                        )

                        binding.etStockMinimo.setText(
                            producto.stockMinimo.toString()
                        )

                        // Recuperar la categoría.
                        idCategoriaSeleccionada = producto.idCategoria

                        mostrarCategoriaSeleccionada()

                        binding.btnGuardarProducto.isEnabled = true
                    }
                }
            },

            alError = { error ->

                Log.e(
                    "StockFlowProductos",
                    getString(R.string.producto_error_cargar_titulo),
                    error
                )

                if (_binding != null && isAdded) {

                    Toast.makeText(
                        requireContext(),
                        R.string.producto_error_cargar,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        )
    }

    // =====================================
    // 7. VALIDAR PRECIOS
    // =====================================

    private fun obtenerPrecio(texto: String): Double? {

        return texto.trim()
            .replace(',', '.')
            .toDoubleOrNull()
            ?.takeIf {
                it.isFinite() && it >= 0.0
            }
    }

    // =====================================
    // 8. VALIDAR Y GUARDAR PRODUCTO
    // =====================================

    private fun guardarProducto() {

        // Evitar editar antes de cargar los datos.
        if (
            idProducto != Args.SIN_ID &&
            productoOriginal == null
        ) {
            return
        }

        val nombre = binding.etNombreProducto.text
            ?.toString()?.trim().orEmpty()

        val descripcion = binding.etDescripcionProducto.text
            ?.toString()?.trim().orEmpty()

        // VALIDACIÓN 1: NOMBRE.
        if (nombre.isBlank()) {

            binding.tilNombreProducto.error =
                getString(R.string.producto_nombre_requerido)

            return
        }

        // VALIDACIÓN 2: CATEGORÍA.
        val categoriaId = idCategoriaSeleccionada

        if (categoriaId == null) {

            binding.tilCategoriaProducto.error =
                getString(R.string.producto_categoria_requerida)

            return
        }

        // VALIDACIÓN 3: PRECIO DE COMPRA.
        val precioCompra = obtenerPrecio(
            binding.etPrecioCompra.text?.toString().orEmpty()
        )

        if (precioCompra == null) {

            binding.tilPrecioCompra.error =
                getString(R.string.producto_precio_invalido)

            return
        }

        // VALIDACIÓN 4: PRECIO DE VENTA.
        val precioVenta = obtenerPrecio(
            binding.etPrecioVenta.text?.toString().orEmpty()
        )

        if (precioVenta == null) {

            binding.tilPrecioVenta.error =
                getString(R.string.producto_precio_invalido)

            return
        }

        // VALIDACIÓN 5: STOCK ACTUAL.
        val stockActual = binding.etStockActual.text
            ?.toString()
            ?.toIntOrNull()
            ?.takeIf { it >= 0 }

        if (stockActual == null) {

            binding.tilStockActual.error =
                getString(R.string.producto_stock_invalido)

            return
        }

        // VALIDACIÓN 6: STOCK MÍNIMO.
        val stockMinimo = binding.etStockMinimo.text
            ?.toString()
            ?.toIntOrNull()
            ?.takeIf { it >= 0 }

        if (stockMinimo == null) {

            binding.tilStockMinimo.error =
                getString(R.string.producto_stock_invalido)

            return
        }

        // =====================================
        // 9. CREAR EL OBJETO PRODUCTO
        // =====================================

        val producto = Producto(
            id = productoOriginal?.id ?: 0L,
            nombre = nombre,
            descripcion = descripcion,
            precioCompra = precioCompra,
            precioVenta = precioVenta,
            cantidadStock = stockActual,
            stockMinimo = stockMinimo,
            idCategoria = categoriaId,
            fotoUri = productoOriginal?.fotoUri
        )

        // Evitar presionar Guardar varias veces.
        binding.btnGuardarProducto.isEnabled = false

        // =====================================
        // 10. RESPUESTA AL GUARDAR
        // =====================================

        val alGuardar: () -> Unit = {

            if (_binding != null && isAdded) {

                Toast.makeText(
                    requireContext(),
                    R.string.producto_guardado_exito,
                    Toast.LENGTH_SHORT
                ).show()

                volver()
            }
        }

        // =====================================
        // 11. RESPUESTA SI OCURRE UN ERROR
        // =====================================

        val alError: (Exception) -> Unit = { error ->

            Log.e(
                "StockFlowProductos",
                getString(R.string.producto_error_guardar_titulo),
                error
            )

            if (_binding != null && isAdded) {

                binding.btnGuardarProducto.isEnabled = true

                Toast.makeText(
                    requireContext(),
                    R.string.producto_guardado_error,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        // =====================================
        // 12. REGISTRAR O ACTUALIZAR EN ROOM
        // =====================================

        if (productoOriginal == null) {

            // Registrar producto nuevo.
            productosViewModel.guardarProductoNuevo(
                producto = producto,
                fotoSeleccionada = fotoUriSeleccionada,
                alGuardar = alGuardar,
                alError = alError
            )

        } else {

            // Actualizar producto existente.
            productosViewModel.actualizarProducto(
                producto = producto,
                fotoSeleccionada = fotoUriSeleccionada,
                alGuardar = alGuardar,
                alError = alError
            )
        }
    }

    // =====================================
    // 13. LIBERAR VIEWBINDING
    // =====================================

    override fun onDestroyView() {
        destinoListener?.let { listener ->
            runCatching { findNavController().removeOnDestinationChangedListener(listener) }
        }
        destinoListener = null
        _binding = null
        super.onDestroyView()
    }
}
