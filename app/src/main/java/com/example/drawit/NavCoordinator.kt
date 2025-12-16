package com.example.drawit

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

sealed interface NavEvent {
    object ToAuthView : NavEvent
    object ToMainView : NavEvent
    object ToNewPainting : NavEvent
    data class ToPaintingDetail(val paintingId: String) : NavEvent
    object Back : NavEvent
}

class NavCoordinator {
    private val _channel = Channel<NavEvent>(Channel.BUFFERED)
    val events = _channel.receiveAsFlow()

    fun toMainScreen() {
        _channel.trySend(NavEvent.ToMainView)
    }

    fun toPaintingDetail(paintingId: String) {
        _channel.trySend(NavEvent.ToPaintingDetail(paintingId))
    }

    fun toNewPainting() {
        _channel.trySend(NavEvent.ToNewPainting)
    }

    fun toAuthScreen() {
        _channel.trySend(NavEvent.ToAuthView)
    }

    fun back() {
        _channel.trySend(NavEvent.Back)
    }
}