package com.leon.stockflow.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.leon.stockflow.data.dao.CategoriaDao
import com.leon.stockflow.data.dao.DetalleMovimientoDao
import com.leon.stockflow.data.dao.MovimientoDao
import com.leon.stockflow.data.dao.ProductoDao
import com.leon.stockflow.data.entity.Categoria
import com.leon.stockflow.data.entity.DetalleMovimiento
import com.leon.stockflow.data.entity.Movimiento
import com.leon.stockflow.data.entity.Producto

@Database(
    entities = [
        Categoria::class,
        Producto::class,
        Movimiento::class,
        DetalleMovimiento::class
    ],
    version = 1,
    exportSchema = false
)
/**
 * LA BASE DE DATOS de StockFlow (Room sobre SQLite). Une las 4 tablas con sus DAOs.
 *
 * Cómo se usa (SIEMPRE desde un ViewModel, nunca desde un Fragment):
 *
 *     private val db = AppDatabase.getInstance(app)
 *     db.productoDao().buscar("", null)     // lee productos
 *     db.productoDao().insertar(producto)   // guarda un producto
 *
 * Reglas de Room que conviene recordar:
 *   - Una función "suspend" se llama dentro de viewModelScope.launch { ... } (no bloquea la app).
 *   - Una función que devuelve Flow se observa: avisa sola cada vez que los datos cambian.
 *   - Si se cambian las tablas (entidades), hay que subir "version" o desinstalar la app del
 *     celular, de lo contrario la app se cierra al abrir la base de datos.
 */
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoriaDao(): CategoriaDao
    abstract fun productoDao(): ProductoDao
    abstract fun movimientoDao(): MovimientoDao
    abstract fun detalleMovimientoDao(): DetalleMovimientoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Cambien esta lista por sus categorías reales
        private val CATEGORIAS_INICIALES = listOf(
            "Alimentos", "Bebidas", "Limpieza", "Papelería", "Otros"
        )

        // Crea la base de datos la primera vez y después reutiliza la misma (singleton).
        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stockflow.db"
                )
                    .addCallback(object : Callback() {
                        // Se ejecuta solo la primera vez que se crea la base de datos
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CATEGORIAS_INICIALES.forEach { nombre ->
                                db.execSQL("INSERT INTO categoria (nombre) VALUES ('$nombre')")
                            }
                        }
                    })
                    .build()
                    .also { INSTANCE = it }
            }
    }
}