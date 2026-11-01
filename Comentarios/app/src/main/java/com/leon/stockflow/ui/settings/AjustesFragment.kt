package com.leon.stockflow.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.leon.stockflow.R
import com.leon.stockflow.databinding.FragmentAjustesBinding

/**
 * PANTALLA: Ajustes (pestaña de la barra inferior). Se mantiene simple.
 *
 * QUÉ DEBE TENER (según Figma):
 *  - Apariencia: dos opciones, CLARO y OSCURO.
 *  - Preferencias: Idioma (Español / Inglés).
 *  - Información: "Acerca de StockFlow" con la versión de la app.
 *  - Botón "Salir": cierra la app (requireActivity().finishAffinity()), no hay sesiones.
 *
 * CÓMO HACERLO (sin librerías nuevas):
 *  - Modo claro/oscuro: AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO / MODE_NIGHT_YES).
 *    Guardar la elección en SharedPreferences y volver a aplicarla al iniciar la app
 *    (en MainActivity.onCreate, antes de super.onCreate), de lo contrario se pierde al cerrar.
 *  - Idioma: AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en")).
 *    Requiere crear res/values-en/strings.xml con las traducciones. Dejarlo para el final.
 *  - Versión: leerla del sistema (packageManager.getPackageInfo(...).versionName); no escribir
 *    el número a mano.
 *
 * Esta pantalla no usa la base de datos ni ViewModel de datos.

 * ANTES DE EMPEZAR: leer InicioFragment.kt y MainViewModel.kt (ejemplo completo y comentado).
 * Si hay botones marcados "TEMPORAL", solo prueban la navegación: reemplazarlos por el diseño.
 * Pasos: (1) reemplazar el layout fragment_xxx.xml por el diseño de Figma, (2) crear un
 * ViewModel copiando el patrón de MainViewModel, (3) mostrar los datos en el Fragment,
 * (4) borrar los botones temporales y la línea "En construcción".
 */
class AjustesFragment : Fragment(R.layout.fragment_ajustes) {

    // Binding: acceso a las vistas del layout (ver el ejemplo comentado en InicioFragment).
    private var _binding: FragmentAjustesBinding? = null
    private val binding get() = _binding!!

    // Aquí se conecta todo: botones, listas y datos.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAjustesBinding.bind(view)
    }

    // Se suelta el binding cuando la vista se destruye.
    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
