package id.co.photocropper

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.Nullable
import id.co.rolllpdf.core.orZero
import kotlin.math.abs
import kotlin.math.pow

/**
 * Created by pertadima on 27,February,2021
 */

class CropOverlayView : View {
    private var defaultMargin = 100
    private val minDistance = 100
    private val vertexSize = 30
    private val gridSize = 3
    private var bitmap: Bitmap? = null
    private var topLeft: Point? = null
    private var topRight: Point? = null
    private var bottomLeft: Point? = null
    private var bottomRight: Point? = null
    private var touchDownX = 0f
    private var touchDownY = 0f
    private var cropPosition: CropPosition? = null
    private var currentWidth = 0
    private var currentHeight = 0
    private var minX = 0
    private var maxX = 0
    private var minY = 0
    private var maxY = 0

    constructor(context: Context?) : super(context)
    constructor(context: Context?, @Nullable attrs: AttributeSet?) : super(context, attrs)

    fun setBitmap(bitmap: Bitmap?) {
        this.bitmap = bitmap
        resetPoints()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (width != currentWidth || height != currentHeight) {
            currentWidth = width
            currentHeight = height
            resetPoints()
        }
        if (bitmap == null) return
        drawBackground(canvas)
        drawVertex(canvas)
        drawEdge(canvas)
        drawGrid(canvas)
    }

    private fun resetPoints() {
        if (bitmap == null) return

        // 1. calculate bitmap size in new canvas
        val scaleX = bitmap?.width.orZero() * 1.0f / width
        val scaleY = bitmap?.height.orZero() * 1.0f / height
        val maxScale = scaleX.coerceAtLeast(scaleY)

        // 2. determine minX , maxX if maxScale = scaleY | minY, maxY if maxScale = scaleX
        var minX = 0
        var maxX = width
        var minY = 0
        var maxY = height
        if (maxScale == scaleY) { // image very tall
            val bitmapInCanvasWidth = (bitmap?.width.orZero() / maxScale).toInt()
            minX = (width - bitmapInCanvasWidth) / 2
            maxX = width - minX
        } else { // image very wide
            val bitmapInCanvasHeight = (bitmap?.height.orZero() / maxScale).toInt()
            minY = (height - bitmapInCanvasHeight) / 2
            maxY = height - minY
        }
        this.minX = minX
        this.minY = minY
        this.maxX = maxX
        this.maxY = maxY
        defaultMargin =
            if (maxX - minX < defaultMargin || maxY - minY < defaultMargin) 0 // remove min
            else 100
        Log.e("stk", "maxX - minX=" + (maxX - minX))
        Log.e("stk", "maxY - minY=" + (maxY - minY))
        topLeft = Point(minX + defaultMargin, minY + defaultMargin)
        topRight = Point(maxX - defaultMargin, minY + defaultMargin)
        bottomLeft = Point(minX + defaultMargin, maxY - defaultMargin)
        bottomRight = Point(maxX - defaultMargin, maxY - defaultMargin)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun drawBackground(canvas: Canvas) {
        val paint = Paint()
        paint.color = Color.parseColor("#66000000")
        paint.style = Paint.Style.FILL
        val path = Path()
        path.moveTo(topLeft?.x.orZero().toFloat(), topLeft?.y.orZero().toFloat())
        path.lineTo(topRight?.x.orZero().toFloat(), topRight?.y.orZero().toFloat())
        path.lineTo(bottomRight?.x.orZero().toFloat(), bottomRight?.y.orZero().toFloat())
        path.lineTo(bottomLeft?.x.orZero().toFloat(), bottomLeft?.y.orZero().toFloat())
        path.close()
        canvas.save()
        canvas.clipPath(path, Region.Op.DIFFERENCE)
        canvas.drawColor(Color.parseColor("#66000000"))
        canvas.restore()
    }

    private fun drawVertex(canvas: Canvas) {
        val paint = Paint()
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawCircle(
            topLeft?.x.orZero().toFloat(),
            topLeft?.y.orZero().toFloat(),
            vertexSize.toFloat(),
            paint
        )
        canvas.drawCircle(
            topRight?.x.orZero().toFloat(),
            topRight?.y.orZero().toFloat(),
            vertexSize.toFloat(),
            paint
        )
        canvas.drawCircle(
            bottomLeft?.x.orZero().toFloat(),
            bottomLeft?.y.orZero().toFloat(),
            vertexSize.toFloat(),
            paint
        )
        canvas.drawCircle(
            bottomRight?.x.orZero().toFloat(),
            bottomRight?.y.orZero().toFloat(),
            vertexSize.toFloat(),
            paint
        )
        Log.e(
            "stk",
            "vertextPoints=" +
                    topLeft.toString() + " " + topRight.toString() + " " + bottomRight.toString() + " " + bottomLeft.toString()
        )
    }

    private fun drawEdge(canvas: Canvas) {
        val paint = Paint()
        paint.color = Color.WHITE
        paint.strokeWidth = 3f
        paint.isAntiAlias = true
        canvas.drawLine(
            topLeft?.x.orZero().toFloat(),
            topLeft?.y.orZero().toFloat(),
            topRight?.x.orZero().toFloat(),
            topRight?.y.orZero().toFloat(),
            paint
        )
        canvas.drawLine(
            topLeft?.x.orZero().toFloat(),
            topLeft?.y.orZero().toFloat(),
            bottomLeft?.x.orZero().toFloat(),
            bottomLeft?.y.orZero().toFloat(),
            paint
        )
        canvas.drawLine(
            bottomRight?.x.orZero().toFloat(),
            bottomRight?.y.orZero().toFloat(),
            topRight?.x.orZero().toFloat(),
            topRight?.y.orZero().toFloat(),
            paint
        )
        canvas.drawLine(
            bottomRight?.x.orZero().toFloat(),
            bottomRight?.y.orZero().toFloat(),
            bottomLeft?.x.orZero().toFloat(),
            bottomLeft?.y.orZero().toFloat(),
            paint
        )
    }

    private fun drawGrid(canvas: Canvas) {
        val paint = Paint()
        paint.color = Color.WHITE
        paint.strokeWidth = 2f
        paint.isAntiAlias = true
        for (i in 1..gridSize) {
            val topDistanceX = abs(topLeft?.x.orZero() - topRight?.x.orZero()) / (gridSize + 1) * i
            val topDistanceY =
                abs((topLeft?.y.orZero() - topRight?.y.orZero()) / (gridSize + 1) * i)
            val top = Point(
                if (topLeft?.x.orZero() < topRight?.x.orZero()) topLeft?.x.orZero() + topDistanceX else topLeft?.x.orZero() - topDistanceX,
                if (topLeft?.y.orZero() < topRight?.y.orZero()) topLeft?.y.orZero() + topDistanceY else topLeft?.y.orZero() - topDistanceY
            )
            val bottomDistanceX =
                abs((bottomLeft?.x.orZero() - bottomRight?.x.orZero()) / (gridSize + 1) * i)
            val bottomDistanceY =
                abs((bottomLeft?.y.orZero() - bottomRight?.y.orZero()) / (gridSize + 1) * i)
            val bottom = Point(
                if (bottomLeft?.x.orZero() < bottomRight?.x.orZero()) bottomLeft?.x.orZero() + bottomDistanceX else bottomLeft?.x.orZero() - bottomDistanceX,
                if (bottomLeft?.y.orZero() < bottomRight?.y.orZero()) bottomLeft?.y.orZero() + bottomDistanceY else bottomLeft?.y.orZero() - bottomDistanceY
            )
            canvas.drawLine(
                top.x.toFloat(),
                top.y.toFloat(),
                bottom.x.toFloat(),
                bottom.y.toFloat(),
                paint
            )
            val leftDistanceX =
                abs((topLeft?.x.orZero() - bottomLeft?.x.orZero()) / (gridSize + 1) * i)
            val leftDistanceY =
                abs((topLeft?.y.orZero() - bottomLeft?.y.orZero()) / (gridSize + 1) * i)
            val left = Point(
                if (topLeft?.x.orZero() < bottomLeft?.x.orZero()) topLeft?.x.orZero() + leftDistanceX else topLeft?.x.orZero() - leftDistanceX,
                if (topLeft?.y.orZero() < bottomLeft?.y.orZero()) topLeft?.y.orZero() + leftDistanceY else topLeft?.y.orZero() - leftDistanceY
            )
            val rightDistanceX =
                abs((topRight?.x.orZero() - bottomRight?.x.orZero()) / (gridSize + 1) * i)
            val rightDistanceY =
                abs((topRight?.y.orZero() - bottomRight?.y.orZero()) / (gridSize + 1) * i)
            val right = Point(
                if (topRight?.x.orZero() < bottomRight?.x.orZero()) topRight?.x.orZero() + rightDistanceX else topRight?.x.orZero() - rightDistanceX,
                if (topRight?.y.orZero() < bottomRight?.y.orZero()) topRight?.y.orZero() + rightDistanceY else topRight?.y.orZero() - rightDistanceY
            )
            canvas.drawLine(
                left.x.toFloat(),
                left.y.toFloat(),
                right.x.toFloat(),
                right.y.toFloat(),
                paint
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> parent.requestDisallowInterceptTouchEvent(false)
            MotionEvent.ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(false)
                onActionDown(event)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                parent.requestDisallowInterceptTouchEvent(true)
                onActionMove(event)
                return true
            }
        }
        return false
    }

    private fun onActionDown(event: MotionEvent) {
        touchDownX = event.x
        touchDownY = event.y
        val touchPoint = Point(
            event.x.toInt(),
            event.y.toInt()
        )
        var minDistance = distance(touchPoint, topLeft)
        cropPosition = CropPosition.TOP_LEFT
        if (minDistance > distance(touchPoint, topRight)) {
            minDistance = distance(touchPoint, topRight)
            cropPosition = CropPosition.TOP_RIGHT
        }
        if (minDistance > distance(touchPoint, bottomLeft)) {
            minDistance = distance(touchPoint, bottomLeft)
            cropPosition = CropPosition.BOTTOM_LEFT
        }
        if (minDistance > distance(touchPoint, bottomRight)) {
            minDistance = distance(touchPoint, bottomRight)
            cropPosition = CropPosition.BOTTOM_RIGHT
        }
    }

    private fun distance(src: Point, dst: Point?): Int {
        return Math.sqrt(
            Math.pow(
                (src.x - dst?.x.orZero()).toDouble(),
                2.0
            ) + (src.y - dst?.y.orZero()).toDouble().pow(2.0)
        )
            .toInt()
    }

    private fun onActionMove(event: MotionEvent) {
        val deltaX = (event.x - touchDownX).toInt()
        val deltaY = (event.y - touchDownY).toInt()
        when (cropPosition) {
            CropPosition.TOP_LEFT -> {
                adjustTopLeft(deltaX, deltaY)
                invalidate()
            }
            CropPosition.TOP_RIGHT -> {
                adjustTopRight(deltaX, deltaY)
                invalidate()
            }
            CropPosition.BOTTOM_LEFT -> {
                adjustBottomLeft(deltaX, deltaY)
                invalidate()
            }
            CropPosition.BOTTOM_RIGHT -> {
                adjustBottomRight(deltaX, deltaY)
                invalidate()
            }
        }
        touchDownX = event.x
        touchDownY = event.y
    }

    private fun adjustTopLeft(deltaX: Int, deltaY: Int) {
        var newX = topLeft?.x.orZero() + deltaX
        if (newX < minX) newX = minX
        if (newX > maxX) newX = maxX
        var newY = topLeft?.y.orZero() + deltaY
        if (newY < minY) newY = minY
        if (newY > maxY) newY = maxY
        topLeft?.set(newX, newY)
    }

    private fun adjustTopRight(deltaX: Int, deltaY: Int) {
        var newX = topRight?.x.orZero() + deltaX
        if (newX > maxX) newX = maxX
        if (newX < minX) newX = minX
        var newY = topRight?.y.orZero() + deltaY
        if (newY < minY) newY = minY
        if (newY > maxY) newY = maxY
        topRight?.set(newX, newY)
    }

    private fun adjustBottomLeft(deltaX: Int, deltaY: Int) {
        var newX = bottomLeft?.x.orZero() + deltaX
        if (newX < minX) newX = minX
        if (newX > maxX) newX = maxX
        var newY = bottomLeft?.y.orZero() + deltaY
        if (newY > maxY) newY = maxY
        if (newY < minY) newY = minY
        bottomLeft?.set(newX, newY)
    }

    private fun adjustBottomRight(deltaX: Int, deltaY: Int) {
        var newX = bottomRight?.x.orZero() + deltaX
        if (newX > maxX) newX = maxX
        if (newX < minX) newX = minX
        var newY = bottomRight?.y.orZero() + deltaY
        if (newY > maxY) newY = maxY
        if (newY < minY) newY = minY
        bottomRight?.set(newX, newY)
    }

    fun crop(cropListener: CropListener, needStretch: Boolean) {
        if (topLeft == null) return

        // calculate bitmap size in new canvas
        val scaleX = bitmap?.width.orZero() * 1.0f / width
        val scaleY = bitmap?.height.orZero() * 1.0f / height
        val maxScale = scaleX.coerceAtLeast(scaleY)

        // re-calculate coordinate in original bitmap
        Log.e("stk", "maxScale=$maxScale")
        val bitmapTopLeft = Point(
            ((topLeft?.x.orZero() - minX) * maxScale).toInt(),
            ((topLeft?.y.orZero() - minY) * maxScale).toInt()
        )
        val bitmapTopRight = Point(
            ((topRight?.x.orZero() - minX) * maxScale).toInt(),
            ((topRight?.y.orZero() - minY) * maxScale).toInt()
        )
        val bitmapBottomLeft = Point(
            ((bottomLeft?.x.orZero() - minX) * maxScale).toInt(),
            ((bottomLeft?.y.orZero() - minY) * maxScale).toInt()
        )
        val bitmapBottomRight = Point(
            ((bottomRight?.x.orZero() - minX) * maxScale).toInt(),
            ((bottomRight?.y.orZero() - minY) * maxScale).toInt()
        )
        Log.e(
            "stk", ("bitmapPoints="
                    + bitmapTopLeft.toString() + " "
                    + bitmapTopRight.toString() + " "
                    + bitmapBottomRight.toString() + " "
                    + bitmapBottomLeft.toString() + " ")
        )
        val output =
            Bitmap.createBitmap(
                bitmap?.width.orZero() + 1,
                bitmap?.height.orZero() + 1,
                Bitmap.Config.ARGB_8888
            )
        val canvas = Canvas(output)
        val paint = Paint()
        // 1. draw path
        val path = Path()
        path.moveTo(bitmapTopLeft.x.toFloat(), bitmapTopLeft.y.toFloat())
        path.lineTo(bitmapTopRight.x.toFloat(), bitmapTopRight.y.toFloat())
        path.lineTo(bitmapBottomRight.x.toFloat(), bitmapBottomRight.y.toFloat())
        path.lineTo(bitmapBottomLeft.x.toFloat(), bitmapBottomLeft.y.toFloat())
        path.close()
        canvas.drawPath(path, paint)

        // 2. draw original bitmap
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        bitmap?.let {
            val copyBitmap = it.copy(Bitmap.Config.ARGB_8888, true)
            canvas.drawBitmap(copyBitmap, 0f, 0f, paint)
        }

        // 3. cut
        val cropRect = Rect(
            bitmapTopLeft.x.coerceAtMost(bitmapBottomLeft.x),
            bitmapTopLeft.y.coerceAtMost(bitmapTopRight.y),
            bitmapBottomRight.x.coerceAtLeast(bitmapTopRight.x),
            bitmapBottomRight.y.coerceAtLeast(bitmapBottomLeft.y)
        )
        val cut = Bitmap.createBitmap(
            output,
            cropRect.left,
            cropRect.top,
            cropRect.width(),
            cropRect.height()
        )
        if (!needStretch) {
            cropListener.onFinish(cut)
        } else {
            // 4. re-calculate coordinate in cropRect
            val cutTopLeft = Point()
            val cutTopRight = Point()
            val cutBottomLeft = Point()
            val cutBottomRight = Point()
            cutTopLeft.x =
                if (bitmapTopLeft.x > bitmapBottomLeft.x) bitmapTopLeft.x - bitmapBottomLeft.x else 0
            cutTopLeft.y =
                if (bitmapTopLeft.y > bitmapTopRight.y) bitmapTopLeft.y - bitmapTopRight.y else 0
            cutTopRight.x =
                if (bitmapTopRight.x > bitmapBottomRight.x) cropRect.width() else cropRect.width() - Math.abs(
                    bitmapBottomRight.x - bitmapTopRight.x
                )
            cutTopRight.y =
                if (bitmapTopLeft.y > bitmapTopRight.y) 0 else Math.abs(bitmapTopLeft.y - bitmapTopRight.y)
            cutBottomLeft.x =
                if (bitmapTopLeft.x > bitmapBottomLeft.x) 0 else Math.abs(bitmapTopLeft.x - bitmapBottomLeft.x)
            cutBottomLeft.y =
                if (bitmapBottomLeft.y > bitmapBottomRight.y) cropRect.height() else cropRect.height() - Math.abs(
                    bitmapBottomRight.y - bitmapBottomLeft.y
                )
            cutBottomRight.x =
                if (bitmapTopRight.x > bitmapBottomRight.x) cropRect.width() - Math.abs(
                    bitmapBottomRight.x - bitmapTopRight.x
                ) else cropRect.width()
            cutBottomRight.y =
                if (bitmapBottomLeft.y > bitmapBottomRight.y) cropRect.height() - Math.abs(
                    bitmapBottomRight.y - bitmapBottomLeft.y
                ) else cropRect.height()
            Log.e("stk", cut.width.toString() + "x" + cut.height)
            Log.e(
                "stk", ("cutPoints="
                        + cutTopLeft.toString() + " "
                        + cutTopRight.toString() + " "
                        + cutBottomRight.toString() + " "
                        + cutBottomLeft.toString() + " ")
            )
            val width = cut.width.toFloat()
            val height = cut.height.toFloat()
            val src = floatArrayOf(
                cutTopLeft.x.toFloat(),
                cutTopLeft.y.toFloat(),
                cutTopRight.x.toFloat(),
                cutTopRight.y.toFloat(),
                cutBottomRight.x.toFloat(),
                cutBottomRight.y.toFloat(),
                cutBottomLeft.x.toFloat(),
                cutBottomLeft.y.toFloat()
            )
            val dst = floatArrayOf(0f, 0f, width, 0f, width, height, 0f, height)
            val matrix = Matrix()
            matrix.setPolyToPoly(src, 0, dst, 0, 4)
            val stretch = Bitmap.createBitmap(cut.width, cut.height, Bitmap.Config.ARGB_8888)
            val stretchCanvas = Canvas(stretch)
            //            stretchCanvas.drawBitmap(cut, matrix, null);
            stretchCanvas.concat(matrix)
            stretchCanvas.drawBitmapMesh(
                cut,
                WIDTH_BLOCK,
                HEIGHT_BLOCK,
                generateVertices(cut.width, cut.height),
                0,
                null,
                0,
                null
            )
            cropListener.onFinish(stretch)
        }
    }

    private val WIDTH_BLOCK = 40
    private val HEIGHT_BLOCK = 40
    private fun generateVertices(widthBitmap: Int, heightBitmap: Int): FloatArray {
        val vertices = FloatArray((WIDTH_BLOCK + 1) * (HEIGHT_BLOCK + 1) * 2)
        val widthBlock = widthBitmap.toFloat() / WIDTH_BLOCK
        val heightBlock = heightBitmap.toFloat() / HEIGHT_BLOCK
        for (i in 0..HEIGHT_BLOCK) for (j in 0..WIDTH_BLOCK) {
            vertices[i * ((HEIGHT_BLOCK + 1) * 2) + (j * 2)] = j * widthBlock
            vertices[(i * ((HEIGHT_BLOCK + 1) * 2)) + (j * 2) + 1] = i * heightBlock
        }
        return vertices
    }
}