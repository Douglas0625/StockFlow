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
 * NAVEGACIÓN: todo lo que se necesita para ir de una pantalla a otra.
 *
 * REGLA DEL EQUIPO: para abrir otra pantalla se usan SIEMPRE las funciones de este archivo
 * (irADetalleProducto(...), volver()...). No se toca el NavController directamente. Así todos
 * navegamos igual y, si algo cambia, se corrige en un solo lugar.
 *
 * ¿Cómo se usa? Desde cualquier Fragment, por ejemplo al tocar un producto de la lista:
 *
 *     irADetalleProducto(producto.id)
 *
 * ¿Cómo recibe el dato la otra pantalla? Lo lee de los argumentos del Fragment. Ejemplo
 * (ya viene hecho en ProductoDetalleFragment):
 *
 *     private val idProducto: Long by lazy { arguments?.getLong(Args.ID_PRODUCTO, Args.SIN_ID) ?: Args.SIN_ID }
 *
 * Solo se manda el ID, nunca el objeto completo: la pantalla que lo recibe vuelve a leer
 * el producto desde la base de datos, así siempre ve la información actualizada.
 *
 * ¿Y si hace falta agregar una pantalla nueva? (poco probable, pero por si acaso)
 *  1. Crear res/layout/fragment_xxx.xml y la clase XxxFragment : Fragment(R.layout.fragment_xxx).
 *  2. Declararla en res/navigation/nav_graph.xml con su id y sus argumentos.
 *  3. Agregar aquí una función irAXxx(...) siguiendo el patrón de las de abajo.
 */

/**
 * Nombres de los datos que viajan entre pantallas (como etiquetas en una caja).
 * Deben ser IGUALES a los android:name de los <argument> de res/navigation/nav_graph.xml.
 */
object Args {
    const val ID_PRODUCTO = "idProducto"
    const val ID_MOVIMIENTO = "idMovimiento"
    const val SIN_ID = -1L // "no hay id": se usa cuando se agrega algo nuevo en vez de editarlo
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

/**
 * Cambia de pestaña como si el usuario tocara la barra inferior (ej. el botón "VER TODOS"
 * de Inicio). Recibe el id de la pantalla: R.id.inicioFragment, R.id.productosFragment,
 * R.id.movimientosFragment o R.id.ajustesFragment.
 */
fun Fragment.irAPestana(idPantalla: Int) {
    requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav).selectedItemId = idPantalla
}

/**
 * Flecha "atrás" de las pantallas secundarias (formulario, detalles...). Regresa a la
 * pantalla anterior. El botón "atrás" del celular hace lo mismo sin necesidad de código.
 */
fun Fragment.volver() {
    findNavController().navigateUp()
}

// ---------------------------------------------------------------------------
// Espacio para la barra de estado (lo usa MainActivity)
// ---------------------------------------------------------------------------

/**
 * Deja espacio para la barra de estado (arriba) y los lados, para que el contenido no
 * quede tapado por la hora y la batería. Abajo NO se agrega nada porque la barra
 * inferior ya lo hace sola. Solo lo usa MainActivity: las pantallas no necesitan llamarlo.
 */
fun View.aplicarInsetsSistema() {
    ViewCompat.setOnApplyWindowInsetsListener(this) { vista, insets ->
        val barras = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        vista.setPadding(barras.left, barras.top, barras.right, 0)
        insets
    }
}
