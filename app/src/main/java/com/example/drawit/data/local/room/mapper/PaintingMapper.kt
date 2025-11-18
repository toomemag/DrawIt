package com.example.drawit.data.local.room.mapper

import com.example.drawit.data.local.room.entity.LayerWithBindings
import com.example.drawit.data.local.room.entity.PaintingEntity
import com.example.drawit.data.local.room.entity.PaintingWithLayers
import com.example.drawit.domain.model.Painting
import java.util.UUID

fun Painting.toEntityWithRelations(): PaintingWithLayers {
    val paintingEntity = PaintingEntity(
        id = this.id.ifEmpty { UUID.randomUUID().toString() },
        timeTaken = this.timeTaken,
        size = this.size,
        theme = this.theme,
        mode = this.mode
    )

    val layersWithBindings = this.layers.map { layer ->
        val (layerEntity, bindingEntities) = layer.toEntities(paintingEntity.id)

        LayerWithBindings(
            layer = layerEntity,
            bindings = bindingEntities
        )
    }

    return PaintingWithLayers(
        painting = paintingEntity,
        layers = layersWithBindings
    )
}

fun PaintingWithLayers.toDomain(): Painting {
    val domainLayers = this.layers.map { layerWithBindings ->
        layerWithBindings.layer.toDomain(layerWithBindings.bindings)
    }.toMutableList()

    android.util.Log.d( "PaintingWithLayers", "toDomain - domain painting <" +
            "size=${this.painting.size}, " +
            "#layers=${domainLayers.size}, " +
            "timeTaken=${this.painting.timeTaken}, " +
            "theme=${this.painting.theme}, " +
            "mode=${this.painting.mode}>" )

    return Painting(
        id = this.painting.id,
        size = this.painting.size,
        layers = domainLayers,
        timeTaken = this.painting.timeTaken,
        theme = this.painting.theme,
        mode = this.painting.mode
    )
}