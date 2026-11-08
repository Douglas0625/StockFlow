package com.leon.stockflow.ui.home


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.leon.stockflow.R
import com.leon.stockflow.data.model.TipoMovimiento
import com.leon.stockflow.databinding.ItemMovimientoBinding

/**
 * Lo que necesita pintar cada fila de la lista, ya formateado como texto.
 * Se separa de la entidad de la base de datos para que el adapter no haga cálculos.
 */
data class MovimientoUi(
    val id: Long,
    val titulo: String,
    val fecha: String,
    val cantidad: String,
    val tipo: TipoMovimiento,
    val observaciones: String = ""
)

/**
 * ADAPTER = el "traductor" entre una lista de datos y las filas que se ven en un RecyclerView.
 * EJEMPLO a copiar para las listas de Productos y Movimientos.
 *
 * Cómo funciona un RecyclerView (la lista de Android):
 *   - Solo crea las filas que caben en pantalla y las REUTILIZA al hacer scroll. Por eso es rápido.
 *   - onCreateViewHolder: crea una fila vacía (infla item_movimiento.xml). Se llama pocas veces.
 *   - onBindViewHolder:   llena una fila con los datos de una posición. Se llama muchas veces.
 *   - DiffUtil (abajo):   compara la lista vieja con la nueva y solo actualiza lo que cambió.
 *
 * Para usarlo: binding.rvMovimientos.adapter = adapter   y   adapter.submitList(nuevaLista)
 *
 * alTocar = qué hacer cuando se toca una fila; la pantalla que usa el adapter lo decide.
 */


/**
 * Lo que necesita pintar cada fila de la lista, ya formateado como texto.
 * Se separa de la entidad de la base de datos para que el adapter no haga cálculos.
 */


/**
 * ADAPTER = el "traductor" entre una lista de datos y las filas que se ven en un RecyclerView.
 * EJEMPLO a copiar para las listas de Productos y Movimientos.
 *
 * Cómo funciona un RecyclerView (la lista de Android):
 *   - Solo crea las filas que caben en pantalla y las REUTILIZA al hacer scroll. Por eso es rápido.
 *   - onCreateViewHolder: crea una fila vacía (infla item_movimiento.xml). Se llama pocas veces.
 *   - onBindViewHolder:   llena una fila con los datos de una posición. Se llama muchas veces.
 *   - DiffUtil (abajo):   compara la lista vieja con la nueva y solo actualiza lo que cambió.
 *
 * Para usarlo: binding.rvMovimientos.adapter = adapter   y   adapter.submitList(nuevaLista)
 *
 * alTocar = qué hacer cuando se toca una fila; la pantalla que usa el adapter lo decide.
 */
class MovimientoAdapter(
    private val alTocar: (idMovimiento: Long) -> Unit
) :
    ListAdapter<MovimientoUi, MovimientoAdapter.ViewHolder>(Diff) {

    // El ViewHolder guarda las vistas de UNA fila para no buscarlas cada vez.
    class ViewHolder(val binding: ItemMovimientoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMovimientoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    // Llena la fila de la posición "position" con sus datos.
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val b = holder.binding
        val ctx = b.root.context
        val esEntrada = item.tipo == TipoMovimiento.ENTRADA
        val color = ContextCompat.getColor(
            ctx,
            if (esEntrada) R.color.stockflow_success else R.color.stockflow_error
        )

        // Al tocar toda la fila se avisa a la pantalla con el id del movimiento.
        b.root.setOnClickListener { alTocar(item.id) }
        b.tvTitulo.text = item.titulo
        b.tvFecha.text = item.fecha
        // La observación es opcional: si está vacía, se oculta la línea.
        b.tvObservacion.isVisible = item.observaciones.isNotBlank()
        b.tvObservacion.text = ctx.getString(R.string.movement_note, item.observaciones)
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

    // Le dice a la lista cuándo dos elementos son "el mismo" (mismo id) y cuándo cambió su contenido.
    private object Diff : DiffUtil.ItemCallback<MovimientoUi>() {
        override fun areItemsTheSame(old: MovimientoUi, new: MovimientoUi) = old.id == new.id
        override fun areContentsTheSame(old: MovimientoUi, new: MovimientoUi) = old == new
    }
}