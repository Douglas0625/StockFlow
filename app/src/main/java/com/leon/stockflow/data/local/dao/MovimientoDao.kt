package com.leon.stockflow.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.leon.stockflow.data.local.entity.DetalleMovimiento
import com.leon.stockflow.data.local.entity.Movimiento
import com.leon.stockflow.data.model.MovimientoConDetalles
import com.leon.stockflow.data.model.StockInsuficienteException
import com.leon.stockflow.data.model.TipoMovimiento
import kotlinx.coroutines.flow.Flow

@Dao
abstract class MovimientoDao {

    // ---------- Operaciones internas ----------
    @Insert
    protected abstract suspend fun insertarMovimiento(movimiento: Movimiento): Long

    @Insert
    protected abstract suspend fun insertarDetalle(detalle: DetalleMovimiento): Long

    @Query("UPDATE producto SET cantidad_stock = cantidad_stock + :cantidad WHERE id = :idProducto")
    protected abstract suspend fun aumentarStock(idProducto: Long, cantidad: Int)

    /** Devuelve 0 filas afectadas si no hay suficiente stock. */
    @Query(
        """
        UPDATE producto SET cantidad_stock = cantidad_stock - :cantidad
        WHERE id = :idProducto AND cantidad_stock >= :cantidad
        """
    )
    protected abstract suspend fun reducirStock(idProducto: Long, cantidad: Int): Int

    // ---------- Registrar entrada/salida ----------
    /**
     * Guarda el movimiento con sus líneas y ajusta el stock de cada producto.
     * Lanza StockInsuficienteException si una SALIDA excede el stock (y revierte todo).
     * Devuelve el id del movimiento creado.
     */
    @Transaction
    open suspend fun registrarMovimiento(
        movimiento: Movimiento,
        detalles: List<DetalleMovimiento>
    ): Long {
        require(detalles.isNotEmpty()) { "El movimiento debe tener al menos un producto" }
        require(detalles.all { it.cantidad > 0 }) { "Las cantidades deben ser mayores a 0" }

        val idMovimiento = insertarMovimiento(movimiento)

        for (detalle in detalles) {
            insertarDetalle(detalle.copy(idMovimiento = idMovimiento))

            when (movimiento.tipo) {
                TipoMovimiento.ENTRADA -> aumentarStock(detalle.idProducto, detalle.cantidad)
                TipoMovimiento.SALIDA -> {
                    val filas = reducirStock(detalle.idProducto, detalle.cantidad)
                    if (filas == 0) throw StockInsuficienteException(detalle.idProducto)
                }
            }
        }
        return idMovimiento
    }

    // ---------- Consultas ----------
    @Transaction
    @Query("SELECT * FROM movimiento ORDER BY fecha DESC")
    abstract fun obtenerTodos(): Flow<List<MovimientoConDetalles>>

    @Transaction
    @Query("SELECT * FROM movimiento WHERE tipo = :tipo ORDER BY fecha DESC")
    abstract fun obtenerPorTipo(tipo: TipoMovimiento): Flow<List<MovimientoConDetalles>>

    @Transaction
    @Query("SELECT * FROM movimiento WHERE id = :id")
    abstract suspend fun obtenerPorId(id: Long): MovimientoConDetalles?
}