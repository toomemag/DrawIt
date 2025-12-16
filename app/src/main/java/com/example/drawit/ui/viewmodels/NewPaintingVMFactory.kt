package com.example.drawit.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.drawit.data.local.room.repository.LocalPaintingsRepository
import com.example.drawit.domain.model.Painting
import com.example.drawit.painting.effects.EffectManager

class NewPaintingVMFactory(
    private val effectManager: EffectManager,
    private val paintingsRepository: LocalPaintingsRepository,
    private val initialPainting: Painting? = null,
    private val isDarkMode: Boolean?= null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NewPaintingViewModel::class.java)) {
            return NewPaintingViewModel(effectManager, paintingsRepository, initialPainting,isDarkMode) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}