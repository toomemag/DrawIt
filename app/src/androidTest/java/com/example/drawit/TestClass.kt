package com.example.drawit

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.drawit.painting.CanvasManager
import com.example.drawit.painting.PaintTool
import junit.framework.TestCase
import org.junit.Test
import org.junit.runner.RunWith

//@RunWith(AndroidJUnit4::class)
class TestClass {
    @Test
    fun differentBrushSizes(){
        val cm = CanvasManager()
        TestCase.assertEquals("Initial brush size", 1, cm.getBrushSize())
        cm.setBrushSize(5)
        TestCase.assertEquals("Changed brush size", 5, cm.getBrushSize())
    }

    @Test
    fun differentTools(){
        val cm = CanvasManager()
        TestCase.assertEquals("Initial Tool", PaintTool.PEN,cm.getTool())
        cm.setTool(PaintTool.FILL)
        TestCase.assertEquals("FILL Tool", PaintTool.FILL,cm.getTool())
        cm.setTool(PaintTool.BRUSH)
        TestCase.assertEquals("BRUSH Tool", PaintTool.BRUSH,cm.getTool())
        cm.setTool(PaintTool.ERASER)
        TestCase.assertEquals("ERASER Tool", PaintTool.ERASER,cm.getTool())
        cm.setTool(PaintTool.PEN)
        TestCase.assertEquals("PEN Tool", PaintTool.PEN,cm.getTool())






    }




}