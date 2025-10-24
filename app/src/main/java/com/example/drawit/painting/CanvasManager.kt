package com.example.drawit.painting

class CanvasManager {
    private val layers = mutableListOf<Layer>()

    init {
        layers.add(Layer(name = "Layer1"))
    }

    fun setActiveLayer(index: Int) {
        if (index == -1) {
            // disable all layers
            for (layer in layers) layer.isActive = false
        } else {
            for (i in layers.indices) {
                layers[i].isActive = (i == index)
            }
        }
    }

    fun getActiveLayerIndex(): Int {
        for (layerIdx in layers.indices) {
            if (layers[layerIdx].isActive) {
                return layerIdx
            }
        }
        return -1
    }

    fun getLayers(): List<Layer> = layers.toList()

    fun addLayer(layer: Layer) {
        layers.add(layer)
    }

    fun addLayer(name: String) {
        layers.add(Layer(name = name))
    }

    fun removeLayer(layer: Layer) {
        layers.remove(layer)
    }

    fun newLayerAction(layerName: String) {
        addLayer("Layer${getLayers().size + 1}")
        // set created layer as active (felt a bit more intuitive rather than clicking new and then selecting the layer)
        setActiveLayer(getLayers().size - 1)
    }

    fun getLayer(index: Int): Layer? = layers.getOrNull(index)
}