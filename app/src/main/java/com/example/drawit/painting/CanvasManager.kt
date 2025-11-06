package com.example.drawit.painting

enum class PaintTool {
    PEN, BRUSH, FILL, ERASER
}

/**
 * Manages the canvas state, including layers, paint color, brush size, and current tool.
 *
 * @property layers A list of layers on the canvas
 * @property paintColor The current paint color
 * @property brushSize The current brush size
 * @property currentTool The currently selected paint tool
 */
class CanvasManager {
    private val layers = mutableListOf<Layer>()
    private var paintColor: Int = 0xFFFFFFFF.toInt()
    private var brushSize: Int = 1
    private var currentTool: PaintTool = PaintTool.PEN

    init {
        // initialize with one layer
        layers.add(Layer(name = "Layer1"))
    }

    /**
     * Sets the active layer by index. If index is -1, all layers are deactivated.
     * @param index The index of the layer to activate, or -1 to deactivate all layers
     */
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

    /**
     * Gets the index of the currently active layer.
     * @return The index of the active layer, or -1 if no layer is active
     */
    fun getActiveLayerIndex(): Int {
        for (layerIdx in layers.indices) {
            if (layers[layerIdx].isActive) {
                return layerIdx
            }
        }
        return -1
    }

    /**
     * Gets a copy of the list of layers.
     * @return A list of layers
     */
    fun getLayers(): List<Layer> = layers.toList()

    /**
     * Adds a new layer to the canvas.
     * @param layer The layer to add
     */
    fun addLayer(layer: Layer) {
        layers.add(layer)
    }

    /**
     * Adds a new layer with the specified name to the canvas.
     * @param name The name of the new layer
     */
    fun addLayer(name: String) {
        layers.add(Layer(name = name))
    }

    /**
     * Removes a layer from the canvas.
     * @param layer The layer to remove
     */
    fun removeLayer(layer: Layer) {
        layers.remove(layer)
    }

    /**
     * Creates a new layer with a default name and sets it as the active layer.
     * @param layerName The name of the new layer
     */
    fun newLayerAction(layerName: String) {
        addLayer("Layer${getLayers().size + 1}")
        // set created layer as active (felt a bit more intuitive rather than clicking new and then selecting the layer)
        setActiveLayer(getLayers().size - 1)
    }

    /**
     * Gets the layer at the specified index.
     * @param index The index of the layer to get
     * @return The layer at the specified index, or null if index is out of bounds
     */
    fun getLayer(index: Int): Layer? = layers.getOrNull(index)

    /**
     * Sets the current paint color.
     * @param color The color to set
     */
    fun setColor(color: Int) {
        paintColor = color
    }

    /**
     * Gets the current paint color.
     * @return The current paint color
     */
    fun getColor(): Int = paintColor
}