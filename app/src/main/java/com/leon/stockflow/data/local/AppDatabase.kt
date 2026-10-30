package com.leon.stockflow.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.leon.stockflow.data.local.dao.CategoriaDao
import com.leon.stockflow.data.local.dao.DetalleMovimientoDao
import com.leon.stockflow.data.local.dao.MovimientoDao
import com.leon.stockflow.data.local.dao.ProductoDao
import com.leon.stockflow.data.local.entity.Categoria
import com.leon.stockflow.data.local.entity.DetalleMovimiento
import com.leon.stockflow.data.local.entity.Movimiento
import com.leon.stockflow.data.local.entity.Producto

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