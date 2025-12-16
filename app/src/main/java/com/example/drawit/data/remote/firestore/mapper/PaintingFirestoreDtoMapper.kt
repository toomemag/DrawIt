package com.example.drawit.data.remote.firestore.mapper

import android.util.Base64
import com.example.drawit.data.local.room.entity.LayerBindingEntity
import com.example.drawit.data.local.room.entity.PaintingEntity
import com.example.drawit.data.local.room.entity.PaintingLayerEntity
import com.example.drawit.data.local.room.mapper.toDomain
import com.example.drawit.data.remote.firestore.model.LayerBindingFirestoreDto
import com.example.drawit.data.remote.firestore.model.LayerFirestoreDto
import com.example.drawit.data.remote.firestore.model.PaintingFirestoreDto
import com.example.drawit.domain.model.Layer
import com.example.drawit.domain.model.LayerEffectBinding
import com.example.drawit.domain.model.LayerTransformInput
import com.example.drawit.domain.model.Painting
import com.google.firebase.Timestamp


/**
 * Maps a PaintingFirestoreDto to a PaintingEntity for local storage.
 * @return PaintingEntity representing the painting for local storage.
 */
fun PaintingFirestoreDto.toEntity(): PaintingEntity = PaintingEntity(
    id = this.id,
    timeTaken = this.timeTaken,
    size = this.size,
    theme = this.theme,
    mode = this.mode,
)

fun PaintingFirestoreDto.toDomain(): Painting {
    val p = Painting(
        id = this.id,
        timeTaken = this.timeTaken,
        size = this.size,
        theme = this.theme,
        mode = this.mode,
        layers = mutableListOf()
    )

    val mappedLayers = this.layers.map { layerDto ->
        val layerEnt = layerDto.toEntity(p)
        val bindingsEntList = layerDto.bindings.flatMap { (effectType, list) ->
            list.map { bindingDto ->
                bindingDto.toEntity(layerId = layerEnt.id, effectType = effectType)
            }
        }
        layerEnt.toDomain(bindingsEntList)
    }

    p.layers.addAll(mappedLayers)

    return p
}

/**
 * Maps a LayerFirestoreDto to a PaintingLayerEntity associated with the given painting.
 * @param painting The Painting entity to which this layer belongs.
 * @return PaintingLayerEntity representing the layer for local storage.
 */
fun LayerFirestoreDto.toEntity(painting: Painting): PaintingLayerEntity = PaintingLayerEntity(
    paintingId = painting.id,
    bitmap = this.bitmap
)

/**
 * Maps a PaintingEntity along with its layers and their bindings to a PaintingFirestoreDto.
 * @param layers List of PaintingLayerEntity associated with the painting.
 * @param bindings Map where the key is the painting ID and the value is a list of LayerBindingEntity.
 * @return PaintingFirestoreDto representing the painting for Firestore storage.
 */
fun PaintingEntity.toFirestoreDto(layers: List<PaintingLayerEntity>, bindings: Map<String, List<LayerBindingEntity>>): PaintingFirestoreDto = PaintingFirestoreDto(
    id = this.id,
    createdAt = Timestamp.now(),
    layers = layers.map { it.toFirestoreDto( bindings.getOrDefault(this.id, listOf())) },
    mode = this.mode,
    size = this.size,
    theme = this.theme,
    timeTaken = this.timeTaken,
    userId = ""
)

/**
 * Maps a PaintingLayerEntity and its associated LayerBindingEntities to a LayerFirestoreDto.
 * @param bindings List of LayerBindingEntity associated with the layer.
 * @return LayerFirestoreDto representing the layer for Firestore storage.
 */
fun PaintingLayerEntity.toFirestoreDto(bindings: List<LayerBindingEntity>): LayerFirestoreDto = LayerFirestoreDto(
    id = this.id,
    bitmap = this.bitmap,
    bindings = bindings
        .groupBy { it.effectType }
        .mapValues { entry -> entry.value.map { it.toFirestoreDto() } }
)

/**
 * Maps a LayerBindingEntity to a LayerBindingFirestoreDto.
 * @return LayerBindingFirestoreDto representing the layer binding for Firestore storage.
 */
fun LayerBindingEntity.toFirestoreDto(): LayerBindingFirestoreDto = LayerBindingFirestoreDto(
    id = this.id,
    effectInputIndex = this.effectOutputIndex,
    layerTransformInput = this.layerTransformInput
)

fun LayerBindingFirestoreDto.toEntity(layerId: String, effectType: Int): LayerBindingEntity = LayerBindingEntity(
    id = this.id,
    layerId = layerId,
    effectOutputIndex = this.effectInputIndex,
    layerTransformInput = this.layerTransformInput,
    effectType = effectType
)