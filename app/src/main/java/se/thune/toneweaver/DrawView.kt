package se.thune.toneweaver

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.support.annotation.RequiresApi
import android.util.AttributeSet
import android.util.Log
import android.view.View

class DrawView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    val paint : Paint = Paint()
    var arr = floatArrayOf()
  // Todo fix this
    val nothing = paint.setARGB(255,255,64,129) // XD Lol gahaha. FUck off this is my shit


    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        canvas!!.drawLines(arr, paint)
    }

    fun setDrawPoints(arr : FloatArray) {
        this.arr = arr
        invalidate()
    }
}

