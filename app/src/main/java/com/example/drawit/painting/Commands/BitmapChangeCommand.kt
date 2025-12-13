package com.example.drawit.painting.Commands

import android.graphics.Bitmap

class BitmapChangeCommand(private val bitmap: Bitmap, private val onExecute: () -> Unit) : UndoableCommand {
    private val bitmapBefore = bitmap.copy(bitmap.config?: Bitmap.Config.ARGB_8888, true)

    override fun execute() {
        onExecute()
    }

    override fun undo() {
        bitmap.eraseColor(0)
        for (x in 0 until bitmapBefore.width) {
            for (y in 0 until bitmapBefore.height) {
                bitmap.setPixel(x, y, bitmapBefore.getPixel(x, y))
            }
        }
    }

    override fun redo() {
        execute()
    }
}