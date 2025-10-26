package com.example.drawit

import android.app.Application
import com.example.drawit.painting.effects.EffectManager

class DrawItApplication : Application() {
    val effectManager by lazy { EffectManager(this) }
}
