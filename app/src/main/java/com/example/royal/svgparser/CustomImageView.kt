package com.example.royal.svgparser

import android.content.Context
import android.graphics.*
import androidx.appcompat.widget.AppCompatImageView
import android.util.AttributeSet


class CustomImageView : AppCompatImageView {

    private var rectanglePaint = Paint()

    private var rectangle = RectF()
    private var path = Path()

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrst: AttributeSet) : super(context, attrst) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    fun setRectF(rectF: RectF) {
        rectangle = rectF
        invalidate()
    }

    fun setPath(p: Path) {
        path = p
        invalidate()
    }

    private fun init() {
        rectanglePaint.color = Color.RED
        rectanglePaint.strokeWidth = 2f
        rectanglePaint.style = Paint.Style.STROKE
    }

    public override fun onDraw(canvas: Canvas) {
        canvas.drawRect(rectangle, rectanglePaint)
        canvas.drawPath(path, rectanglePaint)
    }

}