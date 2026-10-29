package com.leon.stockflow.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Producto del inventario.
 *
 * - idCategoria es llave foránea hacia Categoria. RESTRICT impide borrar una
 *   categoría que todavía tenga productos.
 * - fotoUri es opcional: guarda la ruta de la foto si el usuario sube una.
 */
@Entity(
    tableName = "producto",
    foreignKeys = [
        ForeignKey(
            entity = Categoria::class,
            parentColumns = ["id"],
            childColumns = ["id_categoria"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index(value = ["id_categoria"])] // acelera filtrar por categoría
)
data class Producto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nombre: String,
    val descripcion: String = "",
    @ColumnInfo(name = "precio_compra") val precioCompra: Double,
    @ColumnInfo(name = "precio_venta") val precioVenta: Double,
    @ColumnInfo(name = "cantidad_stock") val cantidadStock: Int,
    @ColumnInfo(name = "stock_minimo") val stockMinimo: Int,
    @ColumnInfo(name = "id_categoria") val idCategoria: Long,
    @ColumnInfo(name = "foto_uri") val fotoUri: String? = null
)