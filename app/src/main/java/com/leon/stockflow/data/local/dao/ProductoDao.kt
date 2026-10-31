package com.leon.stockflow.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.leon.stockflow.data.local.entity.Producto
import com.leon.stockflow.data.model.ProductoConCategoria
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {

    // ---------- CRUD ----------
    @Insert
    suspend fun insertar(producto: Producto): Long

    @Update
    suspend fun actualizar(producto: Producto)

    /** Falla con SQLiteConstraintException si el producto tiene movimientos (RESTRICT). */
    @Delete
    suspend fun eliminar(producto: Producto)

    @Query("SELECT * FROM producto WHERE id = :id")
    suspend fun obtenerPorId(id: Long): Producto?

    // ---------- Listados ----------
    /**
     * Lista con búsqueda por nombre y filtro opcional por categoría.
     * - texto = "" trae todos.
     * - idCategoria = null trae todas las categorías.
     */
    @Query(
        """
        SELECT p.*, c.nombre AS nombre_categoria
        FROM producto p
        INNER JOIN categoria c ON c.id = p.id_categoria
        WHERE p.nombre LIKE '%' || :texto || '%'
          AND (:idCategoria IS NULL OR p.id_categoria = :idCategoria)
        ORDER BY p.nombre
        """
    )
    fun buscar(texto: String = "", idCategoria: Long? = null): Flow<List<ProductoConCategoria>>

    @Query(
        """
        SELECT p.*, c.nombre AS nombre_categoria
        FROM producto p
        INNER JOIN categoria c ON c.id = p.id_categoria
        WHERE p.id = :id
        """
    )
    fun observarPorId(id: Long): Flow<ProductoConCategoria?>

    // ---------- Alertas y resumen ----------
    @Query(
        """
        SELECT p.*, c.nombre AS nombre_categoria
        FROM producto p
        INNER JOIN categoria c ON c.id = p.id_categoria
        WHERE p.cantidad_stock <= p.stock_minimo
        ORDER BY p.cantidad_stock
        """
    )
    fun obtenerStockBajo(): Flow<List<ProductoConCategoria>>

    @Query("SELECT COUNT(*) FROM producto")
    fun contarProductos(): Flow<Int>

    @Query("SELECT COUNT(*) FROM producto WHERE cantidad_stock <= stock_minimo")
    fun contarStockBajo(): Flow<Int>

    /** Valor total del inventario a precio de compra. */
    @Query("SELECT COALESCE(SUM(cantidad_stock * precio_compra), 0) FROM producto")
    fun valorInventario(): Flow<Double>

    /** Suma de todas las unidades en stock (para "Total de unidades"). */
    @Query("SELECT COALESCE(SUM(cantidad_stock), 0) FROM producto")
    fun totalUnidades(): Flow<Int>
}