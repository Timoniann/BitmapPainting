package com.timoniann.bitmappainting

import android.view.View
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import java.util.*


class PaintImageView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    val actions = LinkedList<Any>()

    fun addBackground(image: Bitmap) = actions.add(image)

    private var lastTouchPosition: PointF? = null

    init {
        isDrawingCacheEnabled = true
    }

    companion object {
        const val SENSITIVITY = 0.1

        fun Point.copy(): Point{
            return Point(x, y)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
             MotionEvent.ACTION_DOWN -> {
                 lastTouchPosition = PointF(event.x, event.y)

                 performClick()
            }
            MotionEvent.ACTION_MOVE -> {
                val curPoint = PointF(event.x, event.y)
                val last = actions.last()
                if(last is Path){
                    last.lineTo(curPoint.x, curPoint.y)
                } else {
                    val path = Path()
                    path.reset()
                    path.moveTo(curPoint.x, curPoint.y)
                    actions.add(path)
                }
            }
            MotionEvent.ACTION_UP -> {
                val lastPoint = lastTouchPosition ?: return false
                val curPoint = PointF(event.x, event.y)

                if (Math.abs(lastPoint.x - curPoint.x) < 5 && Math.abs(lastPoint.y - curPoint.y) < 5) {
                    actions.add(generatePoints(Point(curPoint.x.toInt(), curPoint.y.toInt())))
                    //actions.add(RectF(lastPoint.x - 20, lastPoint.y - 20, lastPoint.x + 20, lastPoint.y + 20))
                }
                else {
                    val last = actions.last()
                    if(last is Path){
                        last.lineTo(curPoint.x, curPoint.y)
                    } else {
                        val path = Path()
                        path.reset()
                        path.moveTo(lastPoint.x, lastPoint.y)
                        path.lineTo(curPoint.x, curPoint.y)
                        actions.add(path)
                    }
                }

                lastTouchPosition = null
            }
        //invalidate()
        }
        //invalidate()
        postInvalidate()

        return true
    }

    private fun checkColorSuitable(baseColor: Int, color: Int): Boolean{
        val baseRed = Color.red(baseColor)
        val baseGreen = Color.green(baseColor)
        val baseBlue = Color.blue(baseColor)

        val colorRed = Color.red(color)
        val colorGreen = Color.green(color)
        val colorBlue = Color.blue(color)

        return  colorRed in (baseRed * (1 - SENSITIVITY))..(baseRed * (SENSITIVITY + 1))
                && colorGreen in (baseGreen * (1 - SENSITIVITY))..(baseGreen * (SENSITIVITY + 1))
                && colorBlue in (baseBlue * (1 - SENSITIVITY))..(baseBlue * (SENSITIVITY + 1))
    }

    private fun generatePoints(point: Point): List<Point> {
        buildDrawingCache()

        val bitmap: Bitmap = getDrawingCache(true)
        val baseColor = bitmap.getPixel(point.x, point.y)
        val points = LinkedList<Point>()

        checkPoint(bitmap, point, baseColor, points, Direction.CENTER)

        return points
    }

    enum class Direction{
        TOP,
        BOTTOM,
        RIGHT,
        LEFT,
        CENTER,
        TOP_RIGHT,
        TOP_LEFT,
        BOTTOM_RIGHT,
        BOTTOM_LEFT
    }

    private fun checkPoint(bitmap: Bitmap, point: Point, baseColor: Int, points: LinkedList<Point>, direction: Direction){
        if (points.any { it.x == point.x && it.y == point.y }) return

        if (checkColorSuitable(baseColor, bitmap.getPixel(point.x, point.y))){
            points.add(point)
            Log.i("Adding point", "[${point.x} ${point.y}]")



            if (direction == Direction.RIGHT || direction == Direction.CENTER || direction == Direction.TOP_RIGHT || direction == Direction.BOTTOM_RIGHT)
                if (point.x + 1 < bitmap.width)
                    checkPoint(bitmap, Point(point.x + 1, point.y), baseColor, points, Direction.RIGHT)

            if (direction == Direction.LEFT || direction == Direction.CENTER || direction == Direction.TOP_LEFT || direction == Direction.BOTTOM_LEFT)
                if (point.x - 1 >= 0)
                    checkPoint(bitmap, Point(point.x - 1, point.y), baseColor, points, Direction.LEFT)

            if (direction == Direction.BOTTOM || direction == Direction.CENTER || direction == Direction.BOTTOM_LEFT || direction == Direction.TOP_LEFT)
                if (point.y + 1 < bitmap.height)
                    checkPoint(bitmap, Point(point.x, point.y + 1), baseColor, points, Direction.BOTTOM)

            if (direction == Direction.TOP || direction == Direction.CENTER || direction == Direction.TOP_LEFT || direction == Direction.TOP_RIGHT)
                if (point.y - 1 >= 0)
                    checkPoint(bitmap, Point(point.x, point.y - 1), baseColor, points, Direction.TOP)

//            if (direction == Direction.CENTER || direction == Direction.TOP_RIGHT){
//                if (point.y - 1 >= 0 && point.x + 1 < bitmap.width)
//                    checkPoint(bitmap, Point(point.x + 1, point.y - 1), baseColor, points, Direction.TOP_RIGHT)
//            }

//            if (direction == Direction.CENTER || direction == Direction.TOP_LEFT){
//                if (point.y - 1 >= 0 && point.x - 1 >= 0)
//                    checkPoint(bitmap, Point(point.x - 1, point.y - 1), baseColor, points, Direction.TOP_LEFT)
//            }

//            if (direction == Direction.CENTER || direction == Direction.BOTTOM_RIGHT){
//                if (point.y + 1 < bitmap.height && point.x + 1 < bitmap.width)
//                    checkPoint(bitmap, Point(point.x + 1, point.y + 1), baseColor, points, Direction.BOTTOM_RIGHT)
//            }
//
//            if (direction == Direction.CENTER || direction == Direction.BOTTOM_LEFT){
//                if (point.y + 1 < bitmap.height && point.x - 1 >= 0)
//                    checkPoint(bitmap, Point(point.x - 1, point.y + 1), baseColor, points, Direction.BOTTOM_LEFT)
//            }
        }
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    private var pictureRect = Rect(0, 0, width, height)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        pictureRect = Rect(0, 0, w, h)
    }



    override fun onDraw(canvas: Canvas) {
        //super.onDraw(canvas)

        actions.forEach {
            when (it){
                is Bitmap -> {
                    //canvas.setBitmap(it)

                    val paint = Paint()
                    paint.style = Paint.Style.FILL

                    //paint.colorFilter = LightingColorFilter(Color.RED, 0)

                    canvas.drawBitmap(it, null, pictureRect, paint)
                }
                is RectF -> {
                    val paint = Paint()
                    paint.color = Color.RED

                    canvas.drawRect(it, paint)
                }
                is Path -> {
                    val paint = Paint()
                    paint.color = Color.argb(50, 0, 0, 255)
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 50f

                    canvas.drawPath(it, paint)
                }
                is List<*> -> {
                    val paint = Paint()
                    paint.style = Paint.Style.STROKE
                    paint.strokeWidth = 3f

                    val pts = FloatArray(it.size * 2)
                    var counter = 0

                    it.forEach {pt ->
                        pt as Point
                        pts[counter * 2] = pt.x.toFloat()
                        pts[counter * 2 + 1] = pt.y.toFloat()
                        counter++
                    }

                    canvas.drawPoints(pts, paint)
                }
                else -> Log.e("PaintImageView", "OnDraw: unassigned type")
            }
        }


        //canvas.drawARGB(100, 255, 0, 0)
    }

}