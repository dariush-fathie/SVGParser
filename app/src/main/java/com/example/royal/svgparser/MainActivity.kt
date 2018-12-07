package com.example.royal.svgparser

import android.graphics.*
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.devs.vectorchildfinder.VectorChildFinder
import com.devs.vectorchildfinder.VectorDrawableCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object Constants {
        // real svg map width in dp
        const val SVG_WIDTH = 970f

        // real svg map height in dp
        const val SVG_HEIGHT = 960f
    }

    private val vFullPathMap = HashMap<Int, VectorDrawableCompat.VFullPath>()
    lateinit var pathMap: HashMap<Int, Path>
    lateinit var regionMap: HashMap<Int, Region>

    private var selectedPosition = -1
    private var screenWidth = 0
    private var screenHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        screenHeight = Converter.getScreenHeightPx(this)
        screenWidth = Converter.getScreenWidthPx(this)

        iv.maximumScale = 4f
        iv.mediumScale = 2.5f
        iv.minimumScale = 1f

        Handler().postDelayed({
            runOnUiThread {
                progressbar.visibility = View.GONE
                calculateSVGScale()
                loadVector()
            }
        }, 1000)

    }

    private fun calculateSVGScale() {
        val screenWidthDp = Converter.getScreenWidthDp(this)
        val screenHeightDp = Converter.getScreenHeightDp(this)

        var divideFactor = 1

        while (true) {
            val tempWidth = SVG_WIDTH / divideFactor
            val tempHeight = SVG_HEIGHT / divideFactor

            if (tempWidth <= screenWidthDp && tempHeight <= screenHeightDp) {
                setImageViewSize(tempWidth, tempHeight)
                break
            }
            divideFactor++
        }
    }

    private fun setImageViewSize(widthDp: Float, heightDp: Float) {
        Log.e("final size", "$widthDp * $heightDp")
        iv.layoutParams.width = Converter.pxFromDp(this, widthDp).toInt()
        iv.layoutParams.height = Converter.pxFromDp(this, heightDp).toInt()

        cIV.layoutParams.width = Converter.pxFromDp(this, widthDp).toInt()
        cIV.layoutParams.height = Converter.pxFromDp(this, heightDp).toInt()
    }

    private fun loadVector() {
        val vector = VectorChildFinder(this, R.drawable.ir_vector, iv)
        pathMap = pathMap(vector)
        //regionMap = regionMap(pathMap)

        var displayRect = RectF(0f, 0f, 0f, 0f)

        iv.setOnPhotoTapListener { view, x, y ->
            //Log.e("x:y", "$x : $y")
            Log.e("displayRect", "${displayRect.left} : ${displayRect.right}")
            Log.e("displayRect", "${displayRect.right} : ${displayRect.bottom}")
            Log.e("displayRect", "${displayRect.width()} : ${displayRect.height()}")

            val xResult = x * displayRect.width() /*+ displayRect.left*/
            val yResult = y * displayRect.height() /*+ displayRect.top*/
            Log.e("TAP", "$xResult:$yResult - ${iv.scale}")

            val dx = displayRect.width() - displayRect.right
            val dy = displayRect.height() - displayRect.bottom

            Log.e("dx", "$dx")
            Log.e("dy", "$dy")

            onMapTaped(xResult, yResult, iv.scale, dx, dy)
        }

        iv.setOnScaleChangeListener { scaleFactor, focusX, focusY ->
            Log.e("Factor", "$scaleFactor - $focusX - $focusY")
        }

        iv.setOnMatrixChangeListener {
            //cIV.setRectF(RectF())
            //Log.e("matrix rect", it.toString())
            displayRect = it
        }
    }

    private fun onMapTaped(xResult: Float, yResult: Float, currentScale: Float, dx: Float, dy: Float) {
        /* regionMap.forEach {
             if (it.value.contains(point.x, point.y)) {
                 //Log.d("point", "Touch IN - position : ${it.key}")
                 vFullPathMap[it.key]?.fillColor = Color.GREEN

                 if (selectedPosition != -1) {
                     vFullPathMap[selectedPosition]?.fillColor =
                             ContextCompat.getColor(this@MainActivity, R.color.land)
                     selectedPosition = it.key
                     vFullPathMap[selectedPosition]?.fillColor = Color.GREEN
                 } else {
                     selectedPosition = it.key
                     vFullPathMap[selectedPosition]?.fillColor = Color.GREEN
                 }
                 iv.invalidate()
                 return@forEach
             }
         }*/



        for (j in 0 until pathMap.size) {
            val pBounds = RectF()
            val path = pathMap[j]!!

            val tempPath = Path()
            tempPath.addPath(path)

            tempPath.computeBounds(pBounds, true)
            Log.e("normal bound", pBounds.toString())


            val tempBounds = RectF()
            val tempPath2 = Path()

            if (currentScale > 1f) {
                val scaleMatrix = Matrix()
                scaleMatrix.setScale(currentScale, currentScale, 0.5f, 0.5f)
                tempPath.transform(scaleMatrix)
                tempPath.computeBounds(pBounds, true)
                Log.e("scaled bounds", pBounds.toString())
                tempPath.computeBounds(pBounds, true)

                scaleMatrix.reset()

                scaleMatrix.setTranslate(-dx, -dy)
                tempPath2.addPath(tempPath)
                tempPath2.transform(scaleMatrix)
                tempPath2.computeBounds(tempBounds, true)
            }

            if (pBounds.contains(xResult, yResult)) {
                Log.e("accepted bounds", pBounds.toString())
                //Log.d("point", "Touch IN - position : ${it.key}")
                vFullPathMap[j]?.fillColor = Color.GREEN


                cIV.setRectF(tempBounds)
                cIV.setPath(tempPath2)

                if (selectedPosition != -1) {
                    vFullPathMap[selectedPosition]?.fillColor =
                            ContextCompat.getColor(this@MainActivity, R.color.land)
                    selectedPosition = j
                    vFullPathMap[selectedPosition]?.fillColor = Color.GREEN
                } else {
                    selectedPosition = j
                    vFullPathMap[selectedPosition]?.fillColor = Color.GREEN
                }
                iv.invalidate()
                break
            }
        }

    }


    private fun pathMap(vector: VectorChildFinder): HashMap<Int, Path> {
        val map = HashMap<Int, Path>()
        for (i in 0..33) {
            val fullPath = vector.findPathByName("p$i")
            map[i] = toPath(fullPath)
            vFullPathMap[i] = fullPath
        }
        return map
    }

    private fun regionMap(pathMap: HashMap<Int, Path>): HashMap<Int, Region> {
        val regionMap = HashMap<Int, Region>()
        pathMap.forEach {
            val rectF = RectF()
            val region = Region()

            it.value.computeBounds(rectF, true)
            region.setPath(
                it.value,
                Region(rectF.left.toInt(), rectF.top.toInt(), rectF.right.toInt(), rectF.bottom.toInt())
            )

            regionMap[it.key] = region
        }

        return regionMap
    }

    private fun toPath(v: VectorDrawableCompat.VFullPath): Path {
        val path = Path()
        v.toPath(path)
        return path
    }

}
