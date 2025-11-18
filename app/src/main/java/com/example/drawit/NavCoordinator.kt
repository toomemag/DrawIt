package com.example.drawit

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

sealed interface NavEvent {
    object ToMainView : NavEvent
    object ToNewPainting : NavEvent
    data class ToPaintingDetail(val paintingId: String) : NavEvent
    object Back : NavEvent
}

class NavCoordinator {
    private val _channel = Channel<NavEvent>(Channel.BUFFERED)
    val events = _channel.receiveAsFlow()

    fun toPaintingDetail(paintingId: String) {
        _channel.trySend(NavEvent.ToPaintingDetail(paintingId))
    }

    fun toNewPainting() {
        _channel.trySend(NavEvent.ToNewPainting)
    }

    fun back() {
        _channel.trySend(NavEvent.Back)
    }
}