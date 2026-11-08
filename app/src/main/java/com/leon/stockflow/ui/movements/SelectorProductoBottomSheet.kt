package com.leon.stockflow.ui.movements

import android.app.Application
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.viewModels
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewModelScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.leon.stockflow.R
import com.leon.stockflow.data.AppDatabase
import com.leon.stockflow.data.model.ProductoConCategoria
import com.leon.stockflow.databinding.BottomSheetSelectorProductoBinding
import com.leon.stockflow.ui.products.ProductoAdapter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Hoja inferior con buscador para elegir un producto. Reutiliza ProductoAdapter (la misma
 * tarjeta de la pantalla Productos). Al tocar uno, devuelve su id a la pantalla que la abrió
 * mediante el "Fragment Result API" y se cierra.
 */
class SelectorProductoBottomSheet : BottomSheetDialogFragment(R.layout.bottom_sheet_selector_producto) {

    companion object {
        const val TAG = "selector_producto"
        const val RESULT_KEY = "selector_producto_resultado"
        const val KEY_ID_PRODUCTO = "idProducto"
    }

    private var _binding: BottomSheetSelectorProductoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SelectorProductoViewModel by viewModels()

    private val adapter = ProductoAdapter { idProducto ->
        parentFragmentManager.setFragmentResult(RESULT_KEY, bundleOf(KEY_ID_PRODUCTO to idProducto))
        dismiss()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = BottomSheetSelectorProductoBinding.bind(view)

        binding.rvProductos.adapter = adapter
        binding.etBuscar.doAfterTextChanged { viewModel.buscar(it?.toString().orEmpty()) }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.productos.collect { lista ->
                    adapter.submitList(lista)
                    binding.tvVacio.isVisible = lista.isEmpty()
                }
            }
        }
    }

    // Se abre completa para que el teclado no tape la lista.
    override fun onStart() {
        super.onStart()
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onDestroyView() {
        binding.rvProductos.adapter = null
        _binding = null
        super.onDestroyView()
    }
}

/** ViewModel del selector: lista de productos filtrada por el texto del buscador. */
class SelectorProductoViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getInstance(app)
    private val texto = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val productos: StateFlow<List<ProductoConCategoria>> =
        texto
            .flatMapLatest { db.productoDao().buscar(texto = it.trim(), idCategoria = null) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun buscar(nuevoTexto: String) {
        texto.value = nuevoTexto
    }
}