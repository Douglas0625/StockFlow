package com.leon.stockflow.ui.movements

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.leon.stockflow.R
import com.leon.stockflow.databinding.ItemLineaMovimientoBinding

/** Una línea de la lista "Productos a afectar" de Registrar Movimiento. */
data class LineaMovimientoUi(
    val idProducto: Long,
    val nombre: String,
    val fotoUri: String?,
    val stock: Int,      // stock al momento de añadirlo (solo informativo)
    val cantidad: Int
)

/**
 * Adapter de la lista de productos del movimiento.
 *   alCambiarCantidad(idProducto, +1 / -1)  ->  botones + y -
 *   alQuitar(idProducto)                    ->  ícono de basura (opcional: si es null, el ícono
 *                                               se oculta; así lo usa Detalle de Movimiento)
 */
class LineaMovimientoAdapter(
    private val alCambiarCantidad: (idProducto: Long, delta: Int) -> Unit,
    private val alQuitar: ((idProducto: Long) -> Unit)? = null
) : ListAdapter<LineaMovimientoUi, LineaMovimientoAdapter.ViewHolder>(Diff) {

    class ViewHolder(val binding: ItemLineaMovimientoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLineaMovimientoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val b = holder.binding

        b.tvNombre.text = item.nombre
        b.tvStock.text = b.root.context.getString(R.string.registrar_stock_disponible, item.stock)
        b.tvCantidad.text = item.cantidad.toString()

        // Foto del producto (si tiene). Se limpia primero porque las filas se reutilizan.
        b.ivProducto.setImageDrawable(null)
        if (!item.fotoUri.isNullOrBlank()) {
            try {
                b.ivProducto.setImageURI(Uri.parse(item.fotoUri))
            } catch (e: Exception) {
                b.ivProducto.setImageDrawable(null)
            }
        }

        b.btnMenos.setOnClickListener { alCambiarCantidad(item.idProducto, -1) }
        b.btnMas.setOnClickListener { alCambiarCantidad(item.idProducto, +1) }
        b.btnQuitar.isVisible = alQuitar != null
        b.btnQuitar.setOnClickListener { alQuitar?.invoke(item.idProducto) }
    }

    private object Diff : DiffUtil.ItemCallback<LineaMovimientoUi>() {
        override fun areItemsTheSame(old: LineaMovimientoUi, new: LineaMovimientoUi) =
            old.idProducto == new.idProducto

        override fun areContentsTheSame(old: LineaMovimientoUi, new: LineaMovimientoUi) = old == new
    }
}