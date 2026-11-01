package com.leon.stockflow.ui.common

import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.leon.stockflow.MainActivity
import com.leon.stockflow.R
import com.leon.stockflow.ui.movements.MovimientosActivity
import com.leon.stockflow.ui.products.ProductosActivity
import com.leon.stockflow.ui.settings.AjustesActivity

/**
 * Piezas compartidas por TODAS las pantallas. Si algo de la barra inferior
 * cambia, se cambia aquí y no en 8 archivos.
 */

/**
 * Deja espacio para la barra de estado (arriba) y los lados.
 * Abajo NO se agrega nada porque la barra inferior ya lo hace sola.
 * Uso: binding.root.aplicarInsetsSistema()  (después de setContentView)
 */
fun View.aplicarInsetsSistema() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { vista, insets ->
        val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        vista.setPadding(barras.left, barras.top, barras.right, 0)
        insets
    }
}

/**
 * Conecta la barra inferior.
 *
 * @param pestanaActual  pestaña que se ve resaltada (R.id.nav_inicio, nav_productos, ...).
 * @param esPantallaRaiz true en las 4 pantallas principales. false en las secundarias
 *                       (detalle, formulario...): ahí tocar su propia pestaña regresa
 *                       a la lista principal.
 */
fun AppCompatActivity.configurarBarraInferior(
    barra: BottomNavigationView,
    pestanaActual: Int,
    esPantallaRaiz: Boolean = true
) {
    barra.selectedItemId = pestanaActual // antes del listener, para que no se dispare
    barra.setOnItemSelectedListener { item ->
        val yaEstoyAhi = esPantallaRaiz && item.itemId == pestanaActual
        if (!yaEstoyAhi) irAPestana(item.itemId)
        true
    }
}

/**
 * Abre una de las 4 pantallas principales y cierra las demás, así el botón
 * "atrás" siempre sale de la app y las pantallas no se acumulan.
 */
fun AppCompatActivity.irAPestana(idItem: Int) {
    val destino = when (idItem) {
        R.id.nav_inicio -> MainActivity::class.java
        R.id.nav_productos -> ProductosActivity::class.java
        R.id.nav_movimientos -> MovimientosActivity::class.java
        R.id.nav_ajustes -> AjustesActivity::class.java
        else -> return
    }
    startActivity(
        Intent(this, destino)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    )
}
