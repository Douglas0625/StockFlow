package com.leon.stockflow.ui.common

import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.leon.stockflow.R

/**
 * Piezas compartidas de navegación. Para ir de una pantalla a otra se usan
 * SIEMPRE estas funciones (no se toca el NavController directamente), así
 * todo el equipo navega igual y si algo cambia se corrige en un solo lugar.
 */

/** Nombres de los datos que viajan entre pantallas. Deben coincidir con nav_graph.xml. */
object Args {
    const val ID_PRODUCTO = "idProducto"
    const val ID_MOVIMIENTO = "idMovimiento"
    const val SIN_ID = -1L
}

// ---------------------------------------------------------------------------
// Ir a otra pantalla
// ---------------------------------------------------------------------------

/** Sin id = agregar producto nuevo. Con id = editar ese producto. */
fun Fragment.irAFormularioProducto(idProducto: Long? = null) {
    findNavController().navigate(
        R.id.productoFormularioFragment,
        bundleOf(Args.ID_PRODUCTO to (idProducto ?: Args.SIN_ID))
    )
}

fun Fragment.irADetalleProducto(idProducto: Long) {
    findNavController().navigate(
        R.id.productoDetalleFragment,
        bundleOf(Args.ID_PRODUCTO to idProducto)
    )
}

/** Si se pasa un producto, ya viene agregado a la lista del movimiento. */
fun Fragment.irARegistrarMovimiento(idProductoInicial: Long? = null) {
    findNavController().navigate(
        R.id.registrarMovimientoFragment,
        bundleOf(Args.ID_PRODUCTO to (idProductoInicial ?: Args.SIN_ID))
    )
}

fun Fragment.irADetalleMovimiento(idMovimiento: Long) {
    findNavController().navigate(
        R.id.movimientoDetalleFragment,
        bundleOf(Args.ID_MOVIMIENTO to idMovimiento)
    )
}

/** Cambia de pestaña como si se tocara la barra inferior (ej. "VER TODOS"). */
fun Fragment.irAPestana(idPantalla: Int) {
    requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav).selectedItemId = idPantalla
}

/** Flecha "atrás" de las pantallas secundarias. */
fun Fragment.volver() {
    findNavController().navigateUp()
}

// ---------------------------------------------------------------------------
// Espacio para la barra de estado (lo usa MainActivity)
// ---------------------------------------------------------------------------

/**
 * Deja espacio para la barra de estado (arriba) y los lados.
 * Abajo NO se agrega nada porque la barra inferior ya lo hace sola.
 */
fun View.aplicarInsetsSistema() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { vista, insets ->
        val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        vista.setPadding(barras.left, barras.top, barras.right, 0)
        insets
    }
}
