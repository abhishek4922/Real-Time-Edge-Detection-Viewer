package com.example.edgevision

import android.content.Context
import android.graphics.Bitmap
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicYuvToRGB
import androidx.camera.core.ImageProxy

class YuvToRgbConverter(context: Context) {
    private val rs: RenderScript = RenderScript.create(context)
    private val scriptYuvToRgb: ScriptIntrinsicYuvToRGB =
        ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs))

    fun yuvToRgb(image: ImageProxy, output: Bitmap) {
        val yuvBytes = yuv420ToNv21(image)
        val yuvType = Allocation.createSized(rs, Element.U8(rs), yuvBytes.size)
        val rgbaType = Allocation.createFromBitmap(rs, output)

        yuvType.copyFrom(yuvBytes)
        scriptYuvToRgb.setInput(yuvType)
        scriptYuvToRgb.forEach(rgbaType)
        rgbaType.copyTo(output)
    }

    private fun yuv420ToNv21(image: ImageProxy): ByteArray {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        return nv21
    }
}
