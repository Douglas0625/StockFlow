package com.leon.stockflow.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.leon.stockflow.data.model.HistorialProducto
import kotlinx.coroutines.flow.Flow

@Dao
interface DetalleMovimientoDao {

    /** Historial de entradas y salidas de un producto, del más reciente al más antiguo. */
    @Query(
        """
        SELECT m.id AS id_movimiento, m.tipo AS tipo, m.fecha AS fecha,
               m.observaciones AS observaciones, d.cantidad AS cantidad
        FROM detalle_movimiento d
        INNER JOIN movimiento m ON m.id = d.id_movimiento
        WHERE d.id_producto = :idProducto
        ORDER BY m.fecha DESC
        """
    )
    fun obtenerHistorial(idProducto: Long): Flow<List<HistorialProducto>>
}