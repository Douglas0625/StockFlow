package com.leon.stockflow.ui.products

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.leon.stockflow.R
import com.leon.stockflow.data.model.ProductoConCategoria
import com.leon.stockflow.databinding.ItemProductoBinding
import java.text.NumberFormat
import java.util.Locale
import android.net.Uri

import android.graphics.drawable.GradientDrawable
import androidx.core.graphics.ColorUtils


/**
 * Adaptador para mostrar los productos del inventario.
 * Utiliza item_producto.xml para cada tarjeta.
 */
class ProductoAdapter(
    private val alTocarProducto: (Long) -> Unit
) : ListAdapter<ProductoConCategoria, ProductoAdapter.ViewHolder>(Diff) {

    // Representa una tarjeta de producto.
    class ViewHolder(
        val binding: ItemProductoBinding
    ) : RecyclerView.ViewHolder(binding.root)

    // Crea la vista de cada tarjeta.
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val binding = ItemProductoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(binding)
    }

    // Coloca los datos reales en cada tarjeta.
    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val item = getItem(position)
        val producto = item.producto
        val binding = holder.binding
        val contexto = binding.root.context

        // Nombre y categoría
        binding.tvNombreProducto.text = producto.nombre
        binding.tvCategoriaProducto.text = item.nombreCategoria

        // Precio de venta
        val formatoPrecio = NumberFormat.getCurrencyInstance(Locale.US)
        binding.tvPrecioProducto.text =
            formatoPrecio.format(producto.precioVenta)

        // Cantidad disponible
        binding.tvStockProducto.text = contexto.getString(
            R.string.productos_stock,
            producto.cantidadStock
        )

        // Determinar si el stock es bajo
        val stockBajo =
            producto.cantidadStock <= producto.stockMinimo

        val colorStock = if (stockBajo) {
            R.color.stockflow_warning
        } else {
            R.color.stockflow_success
        }

        binding.tvStockProducto.setTextColor(
            ContextCompat.getColor(contexto, colorStock)
        )


// Crear la etiqueta con fondo y borde según el stock.
        val colorEtiqueta = ContextCompat.getColor(
            contexto,
            colorStock
        )

        val densidad = contexto.resources.displayMetrics.density

        val fondoEtiqueta = GradientDrawable().apply {

            // Bordes redondeados.
            cornerRadius = 4f * densidad

            // Fondo suave del mismo color.
            setColor(
                ColorUtils.setAlphaComponent(
                    colorEtiqueta,
                    28
                )
            )

            // Borde del indicador.
            setStroke(
                densidad.toInt().coerceAtLeast(1),
                colorEtiqueta
            )
        }

// Aplicar fondo a la etiqueta de stock.
        binding.tvStockProducto.background = fondoEtiqueta



        // Mostrar la fotografía guardada del producto.
        val fotoUri = producto.fotoUri

        // Limpiar la imagen anterior al reutilizar la tarjeta.
        binding.ivProducto.setImageDrawable(null)

        if (!fotoUri.isNullOrBlank()) {
            try {
                // Cargar fotografía desde la URI guardada.
                binding.ivProducto.setImageURI(
                    Uri.parse(fotoUri)
                )
            } catch (e: Exception) {
                // Mantener el espacio vacío si la foto falla.
                binding.ivProducto.setImageDrawable(null)
            }
        }


        // Abrir el detalle del producto seleccionado.
        binding.root.setOnClickListener {
            alTocarProducto(producto.id)
        }
    }

    // Detecta qué productos cambiaron.
    private object Diff : DiffUtil.ItemCallback<ProductoConCategoria>() {

        override fun areItemsTheSame(
            oldItem: ProductoConCategoria,
            newItem: ProductoConCategoria
        ): Boolean {
            return oldItem.producto.id == newItem.producto.id
        }

        override fun areContentsTheSame(
            oldItem: ProductoConCategoria,
            newItem: ProductoConCategoria
        ): Boolean {
            return oldItem == newItem
        }
    }
}
