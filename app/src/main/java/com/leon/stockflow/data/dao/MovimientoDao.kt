package com.leon.stockflow.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.leon.stockflow.data.entity.DetalleMovimiento
import com.leon.stockflow.data.entity.Movimiento
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

    // ---------- Operaciones internas para editar un movimiento ----------
    @Query("UPDATE movimiento SET fecha = :fecha, observaciones = :observaciones WHERE id = :idMovimiento")
    protected abstract suspend fun actualizarDatos(idMovimiento: Long, fecha: Long, observaciones: String)

    @Query("UPDATE detalle_movimiento SET cantidad = :cantidad WHERE id = :idDetalle")
    protected abstract suspend fun actualizarCantidadDetalle(idDetalle: Long, cantidad: Int)

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

    // ---------- Editar un movimiento ----------
    /**
     * Cambia la fecha, las observaciones y las cantidades de un movimiento ya registrado,
     * corrigiendo el stock en la misma transacción. El tipo y los productos NO cambian.
     *
     * [cantidades] = idProducto -> nueva cantidad (los productos que no vengan se dejan igual).
     *
     * Cómo corrige el stock (equivale a "revertir lo anterior y aplicar lo nuevo", pero
     * calculando solo la diferencia):
     *   - ENTRADA: si la cantidad sube, el stock sube; si baja, el stock baja.
     *   - SALIDA:  si la cantidad sube, el stock baja; si baja, el stock sube.
     *
     * Lanza StockInsuficienteException si algún producto quedaría con stock negativo
     * (por ejemplo, reducir una entrada cuyas unidades ya se vendieron) y revierte TODO.
     */
    @Transaction
    open suspend fun actualizarMovimiento(
        idMovimiento: Long,
        fecha: Long,
        observaciones: String,
        cantidades: Map<Long, Int>
    ) {
        require(cantidades.values.all { it > 0 }) { "Las cantidades deben ser mayores a 0" }

        val actual = obtenerPorId(idMovimiento)
            ?: throw IllegalArgumentException("El movimiento $idMovimiento no existe")

        for (linea in actual.detalles) {
            val nueva = cantidades[linea.detalle.idProducto] ?: continue
            val diferencia = nueva - linea.detalle.cantidad
            if (diferencia == 0) continue

            // Cuánto cambia el stock: una entrada suma lo que sube; una salida resta lo que sube.
            val efectoEnStock =
                if (actual.movimiento.tipo == TipoMovimiento.ENTRADA) diferencia else -diferencia

            if (efectoEnStock > 0) {
                aumentarStock(linea.detalle.idProducto, efectoEnStock)
            } else {
                val filas = reducirStock(linea.detalle.idProducto, -efectoEnStock)
                if (filas == 0) throw StockInsuficienteException(linea.detalle.idProducto)
            }
            actualizarCantidadDetalle(linea.detalle.id, nueva)
        }

        actualizarDatos(idMovimiento, fecha, observaciones)
    }

    // ---------- Eliminar un movimiento ----------
    /**
     * Elimina un movimiento y revierte su efecto sobre el inventario dentro de una transacción.
     * - Eliminar una ENTRADA resta del stock las unidades que había agregado.
     * - Eliminar una SALIDA devuelve al stock las unidades que había descontado.
     *
     * Si ya no hay suficientes unidades para revertir una ENTRADA (por ejemplo, porque se
     * consumieron en movimientos posteriores), la operación falla y Room revierte todo.
     */
    @Transaction
    open suspend fun eliminarMovimiento(idMovimiento: Long) {
        val actual = obtenerPorId(idMovimiento)
            ?: throw IllegalArgumentException("El movimiento $idMovimiento no existe")

        for (linea in actual.detalles) {
            when (actual.movimiento.tipo) {
                TipoMovimiento.ENTRADA -> {
                    val filas = reducirStock(linea.producto.id, linea.detalle.cantidad)
                    if (filas == 0) throw StockInsuficienteException(linea.producto.id)
                }
                TipoMovimiento.SALIDA -> aumentarStock(linea.producto.id, linea.detalle.cantidad)
            }
        }

        borrarMovimientoPorId(idMovimiento)
    }

    @Query("DELETE FROM movimiento WHERE id = :idMovimiento")
    protected abstract suspend fun borrarMovimientoPorId(idMovimiento: Long)

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

    @Transaction
    @Query("SELECT * FROM movimiento ORDER BY fecha DESC LIMIT :limite")
    abstract fun obtenerRecientes(limite: Int): Flow<List<MovimientoConDetalles>>
}