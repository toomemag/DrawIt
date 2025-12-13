package com.example.drawit.painting.Commands

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode

class StrokeCommand(
    private val targetBitmap: Bitmap,
    private val bitmapBefore: Bitmap,
    private val bitmapAfter: Bitmap
) : UndoableCommand {

    override fun execute() {
        // The stroke is already on the bitmap when this is created, so this is empty.
    }

    override fun undo() {
        copyBitmap(bitmapBefore, targetBitmap)
    }

    override fun redo() {
        copyBitmap(bitmapAfter, targetBitmap)
    }

    private fun copyBitmap(source: Bitmap, destination: Bitmap) {
        val canvas = Canvas(destination)
        val paint = Paint()
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC)
        destination.eraseColor(0) // Clear destination bitmap
        canvas.drawBitmap(source, 0f, 0f, paint)
    }
}
