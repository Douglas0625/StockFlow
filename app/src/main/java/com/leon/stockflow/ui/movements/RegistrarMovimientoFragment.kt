package com.leon.stockflow.ui.movements

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.leon.stockflow.R
import com.leon.stockflow.data.model.TipoMovimiento
import com.leon.stockflow.databinding.FragmentRegistrarMovimientoBinding
import com.leon.stockflow.ui.common.Args
import com.leon.stockflow.ui.common.volver
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * PANTALLA: Registrar Movimiento (pantalla secundaria).
 * Si viene del Detalle del Producto, idProductoInicial trae ese producto y ya aparece en la
 * lista; si vale Args.SIN_ID (-1), la lista empieza vacía.
 */

/**
 * PANTALLA: Registrar Movimiento (pantalla secundaria, se abre desde Movimientos o desde el
 * Detalle del Producto). Si viene del Detalle del Producto, idProductoInicial trae ese producto
 * y debe aparecer ya en la lista; si vale Args.SIN_ID (-1), la lista empieza vacía.
 *
 * QUÉ DEBE TENER (según Figma): selector Entrada / Salida, fecha (MaterialDatePicker),
 * observaciones (opcional), lista de productos con cantidad (- / +) y botón de quitar,
 * "Añadir Producto" (abre el selector de producto: una hoja inferior con buscador, por ejemplo
 * un BottomSheetDialogFragment que reutilice db.productoDao().buscar(...)), y el botón
 * "Registrar Entrada" / "Registrar Salida" (cambia según el tipo elegido).
 *
 * GUARDAR (ya existe y hace todo, incluido actualizar el stock):
 *   db.movimientoDao().registrarMovimiento(
 *       Movimiento(tipo = TipoMovimiento.ENTRADA, fecha = fechaEnMilisegundos, observaciones = "..."),
 *       listOf(DetalleMovimiento(idMovimiento = 0, idProducto = id, cantidad = n), ...)
 *   )
 *  - Es suspend: llamarlo dentro de viewModelScope.launch { ... }.
 *  - En una SALIDA con más unidades que el stock lanza StockInsuficienteException y NO guarda
 *    nada: capturarla (try/catch) y mostrar un mensaje claro al usuario.
 *  - Reglas: al menos un producto, cantidades mayores a 0, sin repetir el mismo producto.
 *  - Al terminar bien: volver().

 * ANTES DE EMPEZAR: leer InicioFragment.kt y MainViewModel.kt (ejemplo completo y comentado).
 * Si hay botones marcados "TEMPORAL", solo prueban la navegación: reemplazarlos por el diseño.
 * Pasos: (1) reemplazar el layout fragment_xxx.xml por el diseño de Figma, (2) crear un
 * ViewModel copiando el patrón de MainViewModel, (3) mostrar los datos en el Fragment,
 * (4) borrar los botones temporales y la línea "En construcción".
 */

class RegistrarMovimientoFragment : Fragment(R.layout.fragment_registrar_movimiento) {

    private var _binding: FragmentRegistrarMovimientoBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegistrarMovimientoViewModel by viewModels()

    /** Producto que ya va en la lista (viene del Detalle del Producto). Args.SIN_ID (-1) = ninguno. */
    private val idProductoInicial: Long by lazy { arguments?.getLong(Args.ID_PRODUCTO, Args.SIN_ID) ?: Args.SIN_ID }

    private val adapter = LineaMovimientoAdapter(
        alCambiarCantidad = { id, delta -> viewModel.cambiarCantidad(id, delta) },
        alQuitar = { id -> viewModel.quitarProducto(id) }
    )

    private val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegistrarMovimientoBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener { volver() }
        binding.rvLineas.adapter = adapter

        if (idProductoInicial != Args.SIN_ID) viewModel.cargarProductoInicial(idProductoInicial)

        // Marcar el tipo guardado ANTES de poner el listener (así no se dispara al restaurar).
        binding.toggleTipo.check(
            if (viewModel.tipo.value == TipoMovimiento.SALIDA) R.id.btnSalida else R.id.btnEntrada
        )
        binding.toggleTipo.addOnButtonCheckedListener { _, idBoton, marcado ->
            if (marcado) {
                viewModel.cambiarTipo(
                    if (idBoton == R.id.btnSalida) TipoMovimiento.SALIDA else TipoMovimiento.ENTRADA
                )
            }
        }

        binding.etFecha.setOnClickListener { mostrarCalendario() }

        // Selector de producto: la hoja inferior devuelve el id del producto elegido.
        binding.btnAnadirProducto.setOnClickListener {
            SelectorProductoBottomSheet().show(childFragmentManager, SelectorProductoBottomSheet.TAG)
        }
        childFragmentManager.setFragmentResultListener(
            SelectorProductoBottomSheet.RESULT_KEY, viewLifecycleOwner
        ) { _, resultado ->
            viewModel.agregarProducto(resultado.getLong(SelectorProductoBottomSheet.KEY_ID_PRODUCTO))
        }

        binding.btnRegistrar.setOnClickListener {
            viewModel.registrar(binding.etObservaciones.text?.toString().orEmpty())
        }

        observarDatos()
    }

    private fun mostrarCalendario() {
        val calendario = MaterialDatePicker.Builder.datePicker()
            .setTitleText(R.string.registrar_fecha_titulo)
            .setSelection(viewModel.fechaParaCalendario())
            // No se permiten fechas futuras.
            .setCalendarConstraints(
                CalendarConstraints.Builder().setValidator(DateValidatorPointBackward.now()).build()
            )
            .build()
        calendario.addOnPositiveButtonClickListener { viewModel.elegirDia(it) }
        calendario.show(childFragmentManager, "calendario")
    }

    private fun observarDatos() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.lineas.collect { lista ->
                        adapter.submitList(lista)
                        binding.tvContador.text = getString(R.string.registrar_items, lista.size)
                    }
                }
                launch {
                    viewModel.tipo.collect { tipo ->
                        val esEntrada = tipo == TipoMovimiento.ENTRADA
                        binding.btnRegistrar.setText(
                            if (esEntrada) R.string.registrar_boton_entrada else R.string.registrar_boton_salida
                        )
                        binding.tvInfo.setText(
                            if (esEntrada) R.string.registrar_info_entrada else R.string.registrar_info_salida
                        )
                    }
                }
                launch {
                    viewModel.fecha.collect { binding.etFecha.setText(formatoFecha.format(Date(it))) }
                }
                launch {
                    viewModel.guardando.collect { binding.btnRegistrar.isEnabled = !it }
                }
                launch {
                    viewModel.eventos.collect { evento ->
                        when (evento) {
                            is EventoRegistro.Mensaje ->
                                Toast.makeText(requireContext(), evento.texto, Toast.LENGTH_LONG).show()
                            EventoRegistro.Guardado -> {
                                Toast.makeText(requireContext(), R.string.registrar_exito, Toast.LENGTH_SHORT).show()
                                volver()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        binding.rvLineas.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
