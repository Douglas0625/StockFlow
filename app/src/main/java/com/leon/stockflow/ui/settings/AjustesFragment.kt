package com.leon.stockflow.ui.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.leon.stockflow.R
import com.leon.stockflow.databinding.FragmentAjustesBinding

class AjustesFragment : Fragment(R.layout.fragment_ajustes) {

    private var _binding: FragmentAjustesBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAjustesBinding.bind(view)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
