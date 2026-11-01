package com.leon.stockflow.ui.movements

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.leon.stockflow.R
import com.leon.stockflow.databinding.FragmentMovimientoDetalleBinding
import com.leon.stockflow.ui.common.Args
import com.leon.stockflow.ui.common.volver

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
class MovimientoDetalleFragment : Fragment(R.layout.fragment_movimiento_detalle) {

    // Binding: acceso a las vistas del layout (ver el ejemplo comentado en InicioFragment).
    private var _binding: FragmentMovimientoDetalleBinding? = null
    private val binding get() = _binding!!

    /** Id del movimiento que se está mostrando. */
    private val idMovimiento: Long by lazy { arguments?.getLong(Args.ID_MOVIMIENTO, Args.SIN_ID) ?: Args.SIN_ID }

    // Aquí se conecta todo: botones, listas y datos.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMovimientoDetalleBinding.bind(view)
        binding.toolbar.setNavigationOnClickListener { volver() }
        binding.tvInfo.text = getString(R.string.received_id, idMovimiento)
    }

    // Se suelta el binding cuando la vista se destruye.
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
