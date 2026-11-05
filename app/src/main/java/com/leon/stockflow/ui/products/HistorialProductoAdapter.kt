
package com.leon.stockflow.ui.products

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.leon.stockflow.R
import com.leon.stockflow.data.model.HistorialProducto
import com.leon.stockflow.data.model.TipoMovimiento
import com.leon.stockflow.databinding.ItemHistorialProductoBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class HistorialProductoAdapter :
    ListAdapter<HistorialProducto,
            HistorialProductoAdapter.ViewHolder>(Diff) {

    class ViewHolder(
        val binding: ItemHistorialProductoBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding = ItemHistorialProductoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val movimiento = getItem(position)
        val binding = holder.binding
        val contexto = binding.root.context

        // Identificar si es una entrada o salida.
        val esEntrada =
            movimiento.tipo == TipoMovimiento.ENTRADA

        // Colores existentes de StockFlow.
        val color = ContextCompat.getColor(
            contexto,
            if (esEntrada) {
                R.color.stockflow_success
            } else {
                R.color.stockflow_error
            }
        )

        // Mostrar tipo de movimiento.
        binding.tvTipoMovimiento.text = contexto.getString(
            if (esEntrada) {
                R.string.movement_in
            } else {
                R.string.movement_out
            }
        )

        // Mostrar flecha.
        binding.tvIndicador.text = contexto.getString(
            if (esEntrada) {
                R.string.historial_flecha_entrada
            } else {
                R.string.historial_flecha_salida
            }
        )

        binding.tvIndicador.setTextColor(color)

        // Formatear fecha.
        val formatoFecha = SimpleDateFormat(
            "yyyy-MM-dd",
            Locale.getDefault()
        )

        binding.tvFechaMovimiento.text =
            formatoFecha.format(Date(movimiento.fecha))

        // Mostrar cantidad con signo.
        binding.tvCantidadMovimiento.text = contexto.getString(
            if (esEntrada) {
                R.string.historial_cantidad_entrada
            } else {
                R.string.historial_cantidad_salida
            },
            abs(movimiento.cantidad)
        )

        binding.tvCantidadMovimiento.setTextColor(color)

        // Mostrar observaciones cuando existan.
        if (movimiento.observaciones.isNotBlank()) {

            binding.tvObservacionesMovimiento.text =
                movimiento.observaciones

            binding.tvObservacionesMovimiento.visibility =
                View.VISIBLE

        } else {
            binding.tvObservacionesMovimiento.visibility =
                View.GONE
        }
    }

    private object Diff :
        DiffUtil.ItemCallback<HistorialProducto>() {

        override fun areItemsTheSame(
            anterior: HistorialProducto,
            nuevo: HistorialProducto
        ): Boolean {
            return anterior.idMovimiento == nuevo.idMovimiento
        }

        override fun areContentsTheSame(
            anterior: HistorialProducto,
            nuevo: HistorialProducto
        ): Boolean {
            return anterior == nuevo
        }
    }
}
