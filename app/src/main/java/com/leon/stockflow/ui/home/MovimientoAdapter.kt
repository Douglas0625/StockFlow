package com.leon.stockflow.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.leon.stockflow.R
import com.leon.stockflow.data.model.TipoMovimiento
import com.leon.stockflow.databinding.ItemMovimientoBinding

/** Lo que necesita pintar cada fila de la lista (ya formateado). */
data class MovimientoUi(
    val id: Long,
    val titulo: String,
    val fecha: String,
    val cantidad: String,
    val tipo: TipoMovimiento
)

class MovimientoAdapter :
    ListAdapter<MovimientoUi, MovimientoAdapter.ViewHolder>(Diff) {

    class ViewHolder(val binding: ItemMovimientoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMovimientoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val b = holder.binding
        val ctx = b.root.context
        val esEntrada = item.tipo == TipoMovimiento.ENTRADA
        val color = ContextCompat.getColor(
            ctx,
            if (esEntrada) R.color.stockflow_success else R.color.stockflow_error
        )

        b.tvTitulo.text = item.titulo
        b.tvFecha.text = item.fecha
        b.tvCantidad.text = item.cantidad
        b.tvCantidad.setTextColor(color)
        b.tvTipo.text = ctx.getString(
            if (esEntrada) R.string.movement_in else R.string.movement_out
        ).uppercase()
        b.tvTipo.setTextColor(color)
        b.tvTipo.setBackgroundResource(
            if (esEntrada) R.drawable.bg_badge_entrada else R.drawable.bg_badge_salida
        )
    }

    private object Diff : DiffUtil.ItemCallback<MovimientoUi>() {
        override fun areItemsTheSame(old: MovimientoUi, new: MovimientoUi) = old.id == new.id
        override fun areContentsTheSame(old: MovimientoUi, new: MovimientoUi) = old == new
    }
}