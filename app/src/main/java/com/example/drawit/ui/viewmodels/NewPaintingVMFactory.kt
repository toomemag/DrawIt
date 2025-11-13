package com.example.drawit.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.drawit.painting.effects.EffectManager

class NewPaintingVMFactory(
    private val effectManager: EffectManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewPaintingViewModel::class.java)) {
            return NewPaintingViewModel(effectManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}