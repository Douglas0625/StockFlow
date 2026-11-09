package com.leon.stockflow.ui.movements

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.leon.stockflow.R
import com.leon.stockflow.data.model.TipoMovimiento
import com.leon.stockflow.databinding.FragmentMovimientoDetalleBinding
import com.leon.stockflow.ui.common.Args
import com.leon.stockflow.ui.common.volver
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * PANTALLA: Detalles de Movimiento (pantalla secundaria, se abre al tocar un movimiento en
 * Inicio o en Movimientos). Recibe idMovimiento (ya leído en la propiedad de abajo).
 *
 * QUÉ DEBE TENER (según el diseño ajustado): tipo (etiqueta ENTRADA/SALIDA, NO editable),
 * fecha (editable), observaciones (editable), la lista "Productos del movimiento" con
 * cantidad editable (- / +) y SIN botón de borrar, y los botones "Cancelar" (volver()) y
 * "Guardar Cambios".
 *
 * DATOS QUE YA EXISTEN:
 *  - db.movimientoDao().obtenerPorId(id)   suspend, devuelve MovimientoConDetalles?
 *        trae .movimiento (tipo, fecha, observaciones) y .detalles (cada uno con .detalle.cantidad
 *        y .producto).
 *
 * ATENCIÓN: "Guardar Cambios" NECESITA una función nueva en data/dao/MovimientoDao.kt que todavía
 * NO existe. Al cambiar cantidades hay que corregir también el stock: revertir el efecto anterior
 * del movimiento, aplicar el nuevo y rechazar el cambio si algún producto quedaría con stock
 * negativo (todo dentro de una @Transaction, igual que registrarMovimiento). Coordinar con quien
 * mantiene data/ antes de implementar ese botón. Mientras tanto se puede construir la parte de
 * solo lectura.

 * ANTES DE EMPEZAR: leer InicioFragment.kt y MainViewModel.kt (ejemplo completo y comentado).
 * Si hay botones marcados "TEMPORAL", solo prueban la navegación: reemplazarlos por el diseño.
 * Pasos: (1) reemplazar el layout fragment_xxx.xml por el diseño de Figma, (2) crear un
 * ViewModel copiando el patrón de MainViewModel, (3) mostrar los datos en el Fragment,
 * (4) borrar los botones temporales y la línea "En construcción".
 */


/**
 * PANTALLA: Detalles de Movimiento (se abre al tocar un movimiento en Inicio o Movimientos).
 *   - Tipo: etiqueta ENTRADA / SALIDA (no editable).
 *   - Fecha, observaciones y cantidades: editables. Los productos no se pueden quitar.
 *   - Cancelar -> volver().  Guardar Cambios -> corrige el movimiento y el stock.
 */


/**
 * PANTALLA: Detalles de Movimiento (se abre al tocar un movimiento en Inicio o Movimientos).
 *   - Tipo: etiqueta ENTRADA / SALIDA (no editable).
 *   - Fecha, observaciones y cantidades: editables. Los productos no se pueden quitar.
 *   - Cancelar -> volver().  Guardar Cambios -> corrige el movimiento y el stock.
 */
class MovimientoDetalleFragment : Fragment(R.layout.fragment_movimiento_detalle) {

    private var _binding: FragmentMovimientoDetalleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MovimientoDetalleViewModel by viewModels()

    /** Id del movimiento que se está mostrando. */
    private val idMovimiento: Long by lazy { arguments?.getLong(Args.ID_MOVIMIENTO, Args.SIN_ID) ?: Args.SIN_ID }

    // Mismo adapter de Registrar Movimiento, pero sin pasar alQuitar: así no aparece la basura.
    private val adapter = LineaMovimientoAdapter(
        alCambiarCantidad = { id, delta -> viewModel.cambiarCantidad(id, delta) }
    )

    private val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMovimientoDetalleBinding.bind(view)

        binding.toolbar.setNavigationOnClickListener { volver() }
        binding.toolbar.inflateMenu(R.menu.menu_detalle_acciones)
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_delete) {
                confirmarEliminarMovimiento()
                true
            } else false
        }
        binding.btnCancelar.setOnClickListener { volver() }
        binding.rvLineas.adapter = adapter

        viewModel.cargar(idMovimiento)

        binding.etFecha.setOnClickListener { mostrarCalendario() }
        binding.etObservaciones.doAfterTextChanged { viewModel.cambiarObservaciones(it?.toString().orEmpty()) }
        binding.btnGuardar.setOnClickListener { viewModel.guardar() }

        observarDatos()
    }

    private fun confirmarEliminarMovimiento() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.detalle_mov_eliminar_titulo)
            .setMessage(R.string.detalle_mov_eliminar_confirmacion)
            .setNegativeButton(R.string.action_cancel, null)
            .setPositiveButton(R.string.action_delete) { _, _ ->
                binding.toolbar.menu.findItem(R.id.action_delete)?.isEnabled = false
                viewModel.eliminar()
            }
            .show()
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
                launch { viewModel.tipo.collect { mostrarTipo(it) } }
                launch {
                    viewModel.fecha.collect { binding.etFecha.setText(formatoFecha.format(Date(it))) }
                }
                launch {
                    // Solo se escribe en el campo si el texto es distinto (evita saltos del cursor).
                    viewModel.observaciones.collect { texto ->
                        if (binding.etObservaciones.text?.toString() != texto) {
                            binding.etObservaciones.setText(texto)
                        }
                    }
                }
                launch { viewModel.lineas.collect { adapter.submitList(it) } }
                launch { viewModel.guardando.collect { binding.btnGuardar.isEnabled = !it } }
                launch {
                    viewModel.eventos.collect { evento ->
                        when (evento) {
                            is EventoDetalle.Mensaje -> {
                                binding.toolbar.menu.findItem(R.id.action_delete)?.isEnabled = true
                                Toast.makeText(requireContext(), evento.texto, Toast.LENGTH_LONG).show()
                            }
                            is EventoDetalle.Cerrar -> {
                                Toast.makeText(requireContext(), evento.texto, Toast.LENGTH_SHORT).show()
                                volver()
                            }
                        }
                    }
                }
            }
        }
    }

    /** Pinta la etiqueta ENTRADA (verde) o SALIDA (roja), igual que en la lista del historial. */
    private fun mostrarTipo(tipo: TipoMovimiento?) {
        binding.tvTipo.isVisible = tipo != null
        if (tipo == null) return

        val esEntrada = tipo == TipoMovimiento.ENTRADA
        val color = ContextCompat.getColor(
            requireContext(),
            if (esEntrada) R.color.stockflow_success else R.color.stockflow_error
        )
        binding.tvTipo.text = getString(if (esEntrada) R.string.movement_in else R.string.movement_out).uppercase()
        binding.tvTipo.setTextColor(color)
        binding.tvTipo.setBackgroundResource(
            if (esEntrada) R.drawable.bg_badge_entrada else R.drawable.bg_badge_salida
        )
    }

    override fun onDestroyView() {
        binding.rvLineas.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
