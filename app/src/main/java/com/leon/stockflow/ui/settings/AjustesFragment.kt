package com.leon.stockflow.ui.settings

import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.leon.stockflow.R
import com.leon.stockflow.databinding.FragmentAjustesBinding

/**
 * PANTALLA: Configuración / Ajustes.
 *
 * Funcionalidades según el diseño de Figma:
 *  - Apariencia: Selector dual (Modo Claro / Modo Oscuro) con persistencia en SharedPreferences.
 *  - Preferencias: Selector de Idioma (Español / Inglés) mediante AppCompatDelegate.
 *  - Información: Ficha acerca de StockFlow y lectura dinámica de la versión de la app.
 *  - Salir: Cierre seguro de la aplicación con confirmación.
 */
class AjustesFragment : Fragment(R.layout.fragment_ajustes) {

    private var _binding: FragmentAjustesBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAjustesBinding.bind(view)

        configurarModoInterfaz()
        configurarIdioma()
        configurarVersionApp()
        configurarBotonSalir()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    /**
     * Configura los botones de interfaz Claro / Oscuro y sincroniza el estado guardado.
     */
    private fun configurarModoInterfaz() {
        val esOscuro = PreferenciasAjustes.esModoOscuro(requireContext())
        actualizarEstadoModoVisual(esOscuro)

        binding.btnModoClaro.setOnClickListener {
            if (PreferenciasAjustes.esModoOscuro(requireContext())) {
                PreferenciasAjustes.guardarModoOscuro(requireContext(), false)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        binding.btnModoOscuro.setOnClickListener {
            if (!PreferenciasAjustes.esModoOscuro(requireContext())) {
                PreferenciasAjustes.guardarModoOscuro(requireContext(), true)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
    }

    /**
     * Actualiza el diseño de los botones para reflejar visualmente cuál está activo.
     */
    private fun actualizarEstadoModoVisual(esOscuro: Boolean) {
        val colorPrimary = com.google.android.material.color.MaterialColors.getColor(
            requireContext(),
            androidx.appcompat.R.attr.colorPrimary,
            android.graphics.Color.BLACK
        )
        val colorOutline = com.google.android.material.color.MaterialColors.getColor(
            requireContext(),
            com.google.android.material.R.attr.colorOutline,
            android.graphics.Color.GRAY
        )

        if (esOscuro) {
            binding.btnModoClaro.setBackgroundResource(R.drawable.bg_toggle_inactive)
            binding.ivIconoClaro.imageTintList = ColorStateList.valueOf(colorOutline)
            binding.tvTextoClaro.setTextColor(colorOutline)

            binding.btnModoOscuro.setBackgroundResource(R.drawable.bg_toggle_active)
            binding.ivIconoOscuro.imageTintList = ColorStateList.valueOf(colorPrimary)
            binding.tvTextoOscuro.setTextColor(colorPrimary)
        } else {
            binding.btnModoClaro.setBackgroundResource(R.drawable.bg_toggle_active)
            binding.ivIconoClaro.imageTintList = ColorStateList.valueOf(colorPrimary)
            binding.tvTextoClaro.setTextColor(colorPrimary)

            binding.btnModoOscuro.setBackgroundResource(R.drawable.bg_toggle_inactive)
            binding.ivIconoOscuro.imageTintList = ColorStateList.valueOf(colorOutline)
            binding.tvTextoOscuro.setTextColor(colorOutline)
        }
    }

    /**
     * Configura el selector de idioma (Español / English).
     */
    private fun configurarIdioma() {
        val localesActuales = AppCompatDelegate.getApplicationLocales()
        val esIngles = localesActuales.toLanguageTags().startsWith("en")

        binding.tvIdiomaSeleccionado.text = if (esIngles) {
            getString(R.string.ajustes_idioma_en)
        } else {
            getString(R.string.ajustes_idioma_es)
        }

        binding.cardIdioma.setOnClickListener {
            val opciones = arrayOf(
                getString(R.string.ajustes_idioma_es),
                getString(R.string.ajustes_idioma_en)
            )
            val seleccionInicial = if (esIngles) 1 else 0

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.ajustes_seleccionar_idioma)
                .setSingleChoiceItems(opciones, seleccionInicial) { dialog, indice ->
                    dialog.dismiss()
                    val tagIdioma = if (indice == 1) "en" else "es"
                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tagIdioma))
                }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
        }
    }

    /**
     * Obtiene dinámicamente la versión declarada en Gradle y la formatea en el badge.
     */
    private fun configurarVersionApp() {
        val nombreVersion = try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requireContext().packageManager.getPackageInfo(
                    requireContext().packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            }
            packageInfo.versionName ?: "1.0"
        } catch (_: Exception) {
            "1.0"
        }

        binding.tvVersion.text = getString(R.string.ajustes_version_formato, nombreVersion.uppercase())
    }

    /**
     * Configura el botón para salir de la aplicación con diálogo de confirmación.
     */
    private fun configurarBotonSalir() {
        binding.btnSalir.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.ajustes_confirmar_salir_titulo)
                .setMessage(R.string.ajustes_confirmar_salir_mensaje)
                .setPositiveButton(R.string.ajustes_salir) { _, _ ->
                    requireActivity().finishAffinity()
                }
                .setNegativeButton(R.string.action_cancel, null)
                .show()
        }
    }
}
