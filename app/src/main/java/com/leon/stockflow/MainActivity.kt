package com.leon.stockflow

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.leon.stockflow.databinding.ActivityMainBinding
import com.leon.stockflow.ui.common.aplicarInsetsSistema

/**
 * Única Activity de la app. Solo hace tres cosas:
 *  1. Dibuja el contenedor de pantallas y la barra inferior (activity_main.xml).
 *  2. Deja espacio para la barra de estado.
 *  3. Conecta la barra inferior con el mapa de navegación (nav_graph.xml).
 *
 * Las pantallas viven en Fragments dentro de ui/home, ui/products, etc.
 * Aquí normalmente NO hay que tocar nada.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.main.aplicarInsetsSistema()

        // El NavHostFragment es quien cambia las pantallas; su NavController las controla.
        val navHost = supportFragmentManager.findFragmentById(R.id.navHost) as NavHostFragment
        binding.bottomNav.setupWithNavController(navHost.navController)
    }
}
