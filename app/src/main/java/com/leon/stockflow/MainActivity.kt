package com.leon.stockflow

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.leon.stockflow.databinding.ActivityMainBinding
import com.leon.stockflow.ui.common.aplicarInsetsSistema

/**
 * ÚNICA Activity de la app: StockFlow usa "una Activity + muchos Fragments".
 *
 * Vocabulario básico:
 *  - Activity      = la ventana de la app. Aquí solo hay una y casi nunca se toca.
 *  - Fragment      = una "pantalla" que se dibuja DENTRO de la Activity
 *                    (Inicio, Productos, Movimientos...). Cada pantalla es un Fragment.
 *  - NavHost       = el contenedor de activity_main.xml donde se muestra el Fragment actual.
 *  - NavController = el "conductor" que cambia de Fragment siguiendo el mapa
 *                    res/navigation/nav_graph.xml.
 *
 * Por eso la barra inferior no parpadea al cambiar de pantalla: vive aquí, y lo único
 * que cambia es lo que hay dentro del contenedor.
 *
 * IMPORTANTE: si vas a programar una pantalla, NO edites este archivo.
 * Trabaja en el Fragment de tu pantalla (ui/products, ui/movements, ui/settings...).
 */
class MainActivity : AppCompatActivity() {

    // ViewBinding: Android genera la clase ActivityMainBinding a partir de activity_main.xml.
    // Con ella se accede a las vistas como binding.bottomNav, sin findViewById.
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // La app se dibuja "de borde a borde", también detrás de la barra de estado (hora y
        // batería, arriba) y de los botones del sistema (abajo). Para no tapar contenido hay
        // que dejar espacio: de eso se encarga aplicarInsetsSistema() más abajo.
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Deja espacio arriba para la barra de estado (ver ui/common/Navegacion.kt).
        binding.main.aplicarInsetsSistema()

        // 1) Buscamos el NavHostFragment (el contenedor del XML) y de él sacamos su NavController.
        //    El NavController conoce el mapa nav_graph.xml y es quien cambia de pantalla.
        val navHost = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        // 2) Conectamos la barra inferior con el NavController. Esta línea hace que al tocar
        //    una pestaña se abra su pantalla, que la pestaña activa se resalte sola y que
        //    cada pestaña recuerde lo que tenía abierto. Funciona porque el id de cada item de
        //    res/menu/bottom_nav_menu.xml es IGUAL al id de su pantalla en nav_graph.xml.
        binding.bottomNav.setupWithNavController(navHost.navController)
    }
}
