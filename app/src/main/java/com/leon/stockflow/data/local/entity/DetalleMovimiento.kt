package com.leon.stockflow.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Una línea de un movimiento: qué producto y cuántas unidades.
 * Permite que un movimiento incluya varios productos.
 *
 * - Si se borra el Movimiento, se borran sus detalles (CASCADE).
 * - Un Producto con movimientos no se puede borrar (RESTRICT).
 * - El índice único evita repetir el mismo producto dentro de un movimiento.
 */
@Entity(
    tableName = "detalle_movimiento",
    foreignKeys = [
        ForeignKey(
            entity = Movimiento::class,
            parentColumns = ["id"],
            childColumns = ["id_movimiento"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Producto::class,
            parentColumns = ["id"],
            childColumns = ["id_producto"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["id_movimiento", "id_producto"], unique = true),
        Index(value = ["id_producto"]) // acelera el historial de un producto
    ]
)
data class DetalleMovimiento(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "id_movimiento") val idMovimiento: Long,
    @ColumnInfo(name = "id_producto") val idProducto: Long,
    val cantidad: Int
)