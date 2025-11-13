package com.example.drawit.painting


import java.util.LinkedList

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import java.util.Queue

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
class CanvasManager{
    private val layers = mutableListOf<Layer>()
    private var paintColor: Int = 0xFF000000.toInt()
    private var brushSize: Int = 1
    private var currentTool: PaintTool = PaintTool.PEN

    init {
        // initialize with one layer
        layers.add(Layer(name = "Layer1"))
    }

    fun getTool( ): PaintTool { return currentTool }
    fun getBrushSize( ): Int { return brushSize }
    fun setTool( tool: PaintTool ) { currentTool = tool }
    fun setBrushSize( size: Int ) { brushSize = size }

    /**
     * Sets the active layer by index. If index is null, all layers are deactivated.
     * @param index The index of the layer to activate, or null to deactivate all layers
     */
    fun setActiveLayer(index: Int?) {
        if (index == null) {
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
    fun getActiveLayerIndex(): Int? {
        for (layerIdx in layers.indices) {
            if (layers[layerIdx].isActive) {
                return layerIdx
            }
        }
        return null
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
        android.util.Log.d( "CanvasManager", "added new layer, total=${getLayers().size}" )
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

    /**
     * Sets the current tool.
     * @param tool, The tool to set
     */
    fun setCurrentTool(tool : PaintTool) {
        currentTool = tool
    }

    /**
     * Gets the current tool.
     * @return The current tool
     */
    fun getCurrentTool(): PaintTool {
        return currentTool
    }

    /**
     * Method to fill the active layer
     * @param x,y, the coordinates to fill from
     */
    fun fill(x: Int, y : Int) {
        // 1. Get the active layer to draw on.
        val activeLayerIndex = getActiveLayerIndex()
        if (activeLayerIndex == -1) return // Exit if no layer is active
        val layer = getLayer(activeLayerIndex) ?: return
        val bitmap = layer.bitmap

        // 2. Boundary Check: Make sure the starting point is inside the bitmap.
        if (x < 0 || x >= bitmap.width || y < 0 || y >= bitmap.height) {
            return
        }

        // 3. Get Target Color: This is the color of the pixel the user tapped.
        val targetColor = bitmap.getPixel(x, y)

        // 4. Get Fill Color: This is the currently selected paint color.
        val fillColor = getColor() // Or however you get the current color, e.g., 'paintColor'

        // 5. Early Exit: If the target area is already the correct color, do nothing.
        if (targetColor == fillColor) {
            return
        }

        // 6. Flood Fill Algorithm (using a Queue for Breadth-First Search)
        val queue: Queue<Pair<Int, Int>> = LinkedList()
        queue.add(Pair(x, y))

        while (queue.isNotEmpty()) {
            val (px, py) = queue.poll() ?: continue

            // Check if the current pixel is within bounds
            if (px < 0 || px >= bitmap.width || py < 0 || py >= bitmap.height) {
                continue
            }

            // If the pixel's color is the one we want to replace...
            if (bitmap.getPixel(px, py) == targetColor) {
                // ...change its color to the new fill color...
                bitmap.setPixel(px, py, fillColor)

                // ...and add its four neighbors to the queue to be processed.
                queue.add(Pair(px + 1, py)) // Right
                queue.add(Pair(px - 1, py)) // Left
                queue.add(Pair(px, py + 1)) // Bottom
                queue.add(Pair(px, py - 1)) // Top
            }
        }
    }

    fun serializeForFirebase(timeTaken: Int): Map<String, Any> {
        return mapOf(
            "userId" to Firebase.auth.currentUser?.uid.toString(),
            "timeTaken" to timeTaken,
            "createdAt" to FieldValue.serverTimestamp(),
            "size" to 128,
            "theme" to "Theme<TODO>",
            "mode" to "free_mode<TODO>",
            "layers" to layers.map { layer -> layer.serializeForFirebase() }
        )
    }



}