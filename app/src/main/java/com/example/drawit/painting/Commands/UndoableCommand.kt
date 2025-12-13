package com.example.drawit.painting.Commands

interface UndoableCommand {
    fun execute()
    fun undo()
    fun redo()
}