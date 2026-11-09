package com.leon.stockflow.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.leon.stockflow.R
import com.leon.stockflow.data.AppDatabase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Notificación inmediata de stock bajo (una sola por producto mientras siga bajo).
 *
 * Se llama después de cualquier operación que cambie existencias o stock mínimo:
 *     StockBajoNotifier.verificar(context, listOf(idProducto))
 *
 * - Stock <= mínimo y sin aviso previo -> notifica y recuerda el id en SharedPreferences.
 * - Stock > mínimo -> olvida el id (se permite una nueva notificación futura).
 * - Sin permiso POST_NOTIFICATIONS no se marca como avisado y no pasa nada más.
 * - Nunca lanza excepciones: un fallo aquí no afecta a la operación de inventario.
 */
object StockBajoNotifier {

    private const val CANAL_ID = "stock_bajo"
    private const val PREFS = "stock_bajo_notificado"
    private const val CLAVE_IDS = "ids"

    private val mutex = Mutex()

    suspend fun verificar(context: Context, idsProducto: Collection<Long>) {
        if (idsProducto.isEmpty()) return
        val app = context.applicationContext
        try {
            withContext(Dispatchers.IO) {
                mutex.withLock {
                    val prefs = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    val avisados = prefs.getStringSet(CLAVE_IDS, emptySet()).orEmpty().toMutableSet()
                    var cambio = false
                    val dao = AppDatabase.getInstance(app).productoDao()

                    for (id in idsProducto.distinct()) {
                        val producto = dao.obtenerPorId(id)
                        val clave = id.toString()
                        if (producto == null || producto.cantidadStock > producto.stockMinimo) {
                            if (avisados.remove(clave)) cambio = true
                        } else if (clave !in avisados && mostrar(app, id, producto.nombre, producto.cantidadStock)) {
                            avisados.add(clave)
                            cambio = true
                        }
                    }
                    // Se copia el set: getStringSet no debe modificarse en sitio.
                    if (cambio) prefs.edit().putStringSet(CLAVE_IDS, HashSet(avisados)).apply()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // Las notificaciones nunca deben romper el flujo de inventario.
        }
    }

    /** Devuelve true si la notificación se publicó. */
    private fun mostrar(context: Context, id: Long, nombre: String, cantidad: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) return false

        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return false

        crearCanal(context)

        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }
            ?: return false
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificacion = NotificationCompat.Builder(context, CANAL_ID)
            .setSmallIcon(R.drawable.ic_info_circle)
            .setContentTitle(context.getString(R.string.notif_stock_bajo_titulo))
            .setContentText(context.getString(R.string.notif_stock_bajo_texto, nombre, cantidad))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.notif_stock_bajo_texto, nombre, cantidad))
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        return try {
            manager.notify(id.toInt(), notificacion)
            true
        } catch (e: SecurityException) {
            false
        }
    }

    private fun crearCanal(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val canal = NotificationChannel(
            CANAL_ID,
            context.getString(R.string.notif_canal_nombre),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply { description = context.getString(R.string.notif_canal_desc) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }
}
