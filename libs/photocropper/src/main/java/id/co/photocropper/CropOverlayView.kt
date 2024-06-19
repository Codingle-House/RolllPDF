package id.co.photocropper

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.Config.ARGB_8888
import android.graphics.Bitmap.createBitmap
import android.graphics.Canvas
import android.graphics.Color.WHITE
import android.graphics.Color.parseColor
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Paint.Style.FILL
import android.graphics.Path
import android.graphics.Point
import android.graphics.PorterDuff.Mode.SRC_IN
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Region.Op.DIFFERENCE
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_UP
import android.view.View
import id.co.photocropper.CropPosition.BOTTOM_LEFT
import id.co.photocropper.CropPosition.BOTTOM_RIGHT
import id.co.photocropper.CropPosition.TOP_LEFT
import id.co.photocropper.CropPosition.TOP_RIGHT
import id.co.rolllpdf.core.Constant.DOUBLE_TWO
import id.co.rolllpdf.core.Constant.FLOAT_ONE
import id.co.rolllpdf.core.Constant.FLOAT_THREE
import id.co.rolllpdf.core.Constant.FLOAT_TWO
import id.co.rolllpdf.core.Constant.FLOAT_ZERO
import id.co.rolllpdf.core.Constant.ONE
import id.co.rolllpdf.core.Constant.ONE_HUNDRED
import id.co.rolllpdf.core.Constant.THREE
import id.co.rolllpdf.core.Constant.TWO
import id.co.rolllpdf.core.Constant.ZERO
import id.co.rolllpdf.core.orZero
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Created by pertadima on 27,February,2021
 */

class CropOverlayView : View {
    private var defaultMargin = 100
    private var minDistance = 100
    private val vertexSize = 30
    private val gridSize = THREE
    private var bitmap: Bitmap? = null
    private var topLeft: Point? = null
    private var topRight: Point? = null
    private var bottomLeft: Point? = null
    private var bottomRight: Point? = null
    private var touchDownX = FLOAT_ZERO
    private var touchDownY = FLOAT_ZERO
    private var cropPosition: CropPosition? = null
    private var currentWidth = ZERO
    private var currentHeight = ZERO
    private var minX = ZERO
    private var maxX = ZERO
    private var minY = ZERO
    private var maxY = ZERO

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

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
        val scaleX = bitmap?.width.orZero() * FLOAT_ONE / width
        val scaleY = bitmap?.height.orZero() * FLOAT_ONE / height
        val maxScale = scaleX.coerceAtLeast(scaleY)

        // 2. determine minX , maxX if maxScale = scaleY | minY, maxY if maxScale = scaleX
        var minX = ZERO
        var maxX = width
        var minY = ZERO
        var maxY = height
        if (maxScale == scaleY) { // image very tall
            val bitmapInCanvasWidth = (bitmap?.width.orZero() / maxScale).toInt()
            minX = (width - bitmapInCanvasWidth) / TWO
            maxX = width - minX
        } else { // image very wide
            val bitmapInCanvasHeight = (bitmap?.height.orZero() / maxScale).toInt()
            minY = (height - bitmapInCanvasHeight) / TWO
            maxY = height - minY
        }
        this.minX = minX
        this.minY = minY
        this.maxX = maxX
        this.maxY = maxY
        defaultMargin = if (maxX - minX < defaultMargin || maxY - minY < defaultMargin) ZERO
        else ONE_HUNDRED
        topLeft = Point(minX + defaultMargin, minY + defaultMargin)
        topRight = Point(maxX - defaultMargin, minY + defaultMargin)
        bottomLeft = Point(minX + defaultMargin, maxY - defaultMargin)
        bottomRight = Point(maxX - defaultMargin, maxY - defaultMargin)
    }

    private fun drawBackground(canvas: Canvas) {
        val path = Path().apply {
            moveTo(topLeft?.x.orZero().toFloat(), topLeft?.y.orZero().toFloat())
            lineTo(topRight?.x.orZero().toFloat(), topRight?.y.orZero().toFloat())
            lineTo(bottomRight?.x.orZero().toFloat(), bottomRight?.y.orZero().toFloat())
            lineTo(bottomLeft?.x.orZero().toFloat(), bottomLeft?.y.orZero().toFloat())
            close()
        }
        canvas.apply {
            save()
            clipPath(path, DIFFERENCE)
            drawColor(parseColor(CANVAS_COLOR))
            restore()
        }
    }

    private fun drawVertex(canvas: Canvas) {
        val paint = Paint().apply {
            color = WHITE
            style = FILL
        }
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
    }

    private fun drawEdge(canvas: Canvas) {
        val paint = Paint().apply {
            color = WHITE
            strokeWidth = FLOAT_THREE
            isAntiAlias = true
        }
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
        val paint = Paint().apply {
            color = WHITE
            strokeWidth = FLOAT_TWO
            isAntiAlias = true
        }
        for (i in ONE..gridSize) {
            val topDistanceX =
                abs(topLeft?.x.orZero() - topRight?.x.orZero()) / (gridSize.inc()) * i
            val topDistanceY =
                abs((topLeft?.y.orZero() - topRight?.y.orZero()) / (gridSize.inc()) * i)
            val top = Point(
                if (topLeft?.x.orZero() < topRight?.x.orZero()) topLeft?.x.orZero() + topDistanceX else topLeft?.x.orZero() - topDistanceX,
                if (topLeft?.y.orZero() < topRight?.y.orZero()) topLeft?.y.orZero() + topDistanceY else topLeft?.y.orZero() - topDistanceY
            )
            val bottomDistanceX =
                abs((bottomLeft?.x.orZero() - bottomRight?.x.orZero()) / (gridSize.inc()) * i)
            val bottomDistanceY =
                abs((bottomLeft?.y.orZero() - bottomRight?.y.orZero()) / (gridSize.inc()) * i)
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
                abs((topLeft?.x.orZero() - bottomLeft?.x.orZero()) / (gridSize.inc()) * i)
            val leftDistanceY =
                abs((topLeft?.y.orZero() - bottomLeft?.y.orZero()) / (gridSize.inc()) * i)
            val left = Point(
                if (topLeft?.x.orZero() < bottomLeft?.x.orZero()) topLeft?.x.orZero() + leftDistanceX else topLeft?.x.orZero() - leftDistanceX,
                if (topLeft?.y.orZero() < bottomLeft?.y.orZero()) topLeft?.y.orZero() + leftDistanceY else topLeft?.y.orZero() - leftDistanceY
            )
            val rightDistanceX =
                abs((topRight?.x.orZero() - bottomRight?.x.orZero()) / (gridSize.inc()) * i)
            val rightDistanceY =
                abs((topRight?.y.orZero() - bottomRight?.y.orZero()) / (gridSize.inc()) * i)
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

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            ACTION_UP -> parent.requestDisallowInterceptTouchEvent(false)
            ACTION_DOWN -> {
                parent.requestDisallowInterceptTouchEvent(false)
                onActionDown(event)
                return true
            }

            ACTION_MOVE -> {
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
        minDistance = distance(touchPoint, topLeft)
        cropPosition = TOP_LEFT
        if (minDistance > distance(touchPoint, topRight)) {
            minDistance = distance(touchPoint, topRight)
            cropPosition = TOP_RIGHT
        }
        if (minDistance > distance(touchPoint, bottomLeft)) {
            minDistance = distance(touchPoint, bottomLeft)
            cropPosition = BOTTOM_LEFT
        }
        if (minDistance > distance(touchPoint, bottomRight)) {
            minDistance = distance(touchPoint, bottomRight)
            cropPosition = BOTTOM_RIGHT
        }
    }

    private fun distance(src: Point, dst: Point?) = sqrt(
        (src.x - dst?.x.orZero()).toDouble().pow(2.0) + (src.y - dst?.y.orZero()).toDouble()
            .pow(DOUBLE_TWO)
    ).toInt()

    private fun onActionMove(event: MotionEvent) {
        val deltaX = (event.x - touchDownX).toInt()
        val deltaY = (event.y - touchDownY).toInt()
        when (cropPosition) {
            TOP_LEFT -> {
                adjustTopLeft(deltaX, deltaY)
                invalidate()
            }

            TOP_RIGHT -> {
                adjustTopRight(deltaX, deltaY)
                invalidate()
            }

            BOTTOM_LEFT -> {
                adjustBottomLeft(deltaX, deltaY)
                invalidate()
            }

            BOTTOM_RIGHT -> {
                adjustBottomRight(deltaX, deltaY)
                invalidate()
            }

            else -> Unit
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
        val output = createBitmap(
            bitmap?.width.orZero().inc(),
            bitmap?.height.orZero().inc(),
            ARGB_8888
        )
        val canvas = Canvas(output)
        val paint = Paint()
        // 1. draw path
        val path = Path().apply {
            moveTo(bitmapTopLeft.x.toFloat(), bitmapTopLeft.y.toFloat())
            lineTo(bitmapTopRight.x.toFloat(), bitmapTopRight.y.toFloat())
            lineTo(bitmapBottomRight.x.toFloat(), bitmapBottomRight.y.toFloat())
            lineTo(bitmapBottomLeft.x.toFloat(), bitmapBottomLeft.y.toFloat())
            close()
        }
        canvas.drawPath(path, paint)

        // 2. draw original bitmap
        paint.xfermode = PorterDuffXfermode(SRC_IN)
        bitmap?.let {
            val copyBitmap = it.copy(ARGB_8888, true)
            canvas.drawBitmap(copyBitmap, FLOAT_ZERO, FLOAT_ZERO, paint)
        }

        // 3. cut
        val cropRect = Rect(
            bitmapTopLeft.x.coerceAtMost(bitmapBottomLeft.x),
            bitmapTopLeft.y.coerceAtMost(bitmapTopRight.y),
            bitmapBottomRight.x.coerceAtLeast(bitmapTopRight.x),
            bitmapBottomRight.y.coerceAtLeast(bitmapBottomLeft.y)
        )
        val cut = createBitmap(
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
            val cutTopLeft = Point().apply {
                x = if (bitmapTopLeft.x > bitmapBottomLeft.x) bitmapTopLeft.x - bitmapBottomLeft.x
                else ZERO
                y = if (bitmapTopLeft.y > bitmapTopRight.y) bitmapTopLeft.y - bitmapTopRight.y
                else ZERO
            }
            val cutTopRight = Point().apply {
                x = if (bitmapTopRight.x > bitmapBottomRight.x) cropRect.width()
                else cropRect.width() - abs(bitmapBottomRight.x - bitmapTopRight.x)
                y = if (bitmapTopLeft.y > bitmapTopRight.y) ZERO
                else abs(bitmapTopLeft.y - bitmapTopRight.y)
            }
            val cutBottomLeft = Point().apply {
                x = if (bitmapTopLeft.x > bitmapBottomLeft.x) ZERO
                else abs(bitmapTopLeft.x - bitmapBottomLeft.x)
                y = if (bitmapBottomLeft.y > bitmapBottomRight.y) cropRect.height()
                else cropRect.height() - abs(bitmapBottomRight.y - bitmapBottomLeft.y)
            }
            val cutBottomRight = Point().apply {
                x = if (bitmapTopRight.x > bitmapBottomRight.x) cropRect.width() - abs(
                    bitmapBottomRight.x - bitmapTopRight.x
                ) else cropRect.width()
                y = if (bitmapBottomLeft.y > bitmapBottomRight.y) cropRect.height() - abs(
                    bitmapBottomRight.y - bitmapBottomLeft.y
                ) else cropRect.height()
            }
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
            val dst = floatArrayOf(
                FLOAT_ZERO,
                FLOAT_ZERO,
                width,
                FLOAT_ZERO,
                width,
                height,
                FLOAT_ZERO,
                height
            )
            val matrix = Matrix().apply {
                matrix.setPolyToPoly(src, ZERO, dst, ZERO, 4)
            }
            val stretch = createBitmap(cut.width, cut.height, ARGB_8888)
            Canvas(stretch).apply {
                concat(matrix)
                drawBitmapMesh(
                    cut,
                    WIDTH_BLOCK,
                    HEIGHT_BLOCK,
                    generateVertices(cut.width, cut.height),
                    0,
                    null,
                    0,
                    null
                )
            }
            cropListener.onFinish(stretch)
        }
    }

    private fun generateVertices(widthBitmap: Int, heightBitmap: Int): FloatArray {
        val vertices = FloatArray((WIDTH_BLOCK.inc()) * (HEIGHT_BLOCK.inc()) * TWO)
        val widthBlock = widthBitmap.toFloat() / WIDTH_BLOCK
        val heightBlock = heightBitmap.toFloat() / HEIGHT_BLOCK
        for (i in ZERO..HEIGHT_BLOCK) for (j in ZERO..WIDTH_BLOCK) {
            vertices[i * ((HEIGHT_BLOCK.inc()) * TWO) + (j * TWO)] = j * widthBlock
            vertices[(i * ((HEIGHT_BLOCK.inc()) * TWO)) + (j * TWO) + ONE] = i * heightBlock
        }
        return vertices
    }

    companion object {
        private const val WIDTH_BLOCK = 40
        private const val HEIGHT_BLOCK = 40
        private const val CANVAS_COLOR = "#66000000"
    }
}