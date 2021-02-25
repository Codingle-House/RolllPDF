package id.co.rolllpdf.scan

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import id.co.rolllpdf.core.getColorCompat
import id.co.rolllpdf.core.orZero
import java.util.*
import kotlin.math.abs

/**
 * Created by jhansi on 28/03/15.
 */
class PolygonView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {
    private var paint: Paint? = null
    private var pointer1: ImageView? = null
    private var pointer2: ImageView? = null
    private var pointer3: ImageView? = null
    private var pointer4: ImageView? = null
    private var midPointer13: ImageView? = null
    private var midPointer12: ImageView? = null
    private var midPointer34: ImageView? = null
    private var midPointer24: ImageView? = null
    private var polygonView: PolygonView? = null

    init {
        init()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun init() {
        polygonView = this
        pointer1 = getImageView(0, 0)
        pointer2 = getImageView(width, 0)
        pointer3 = getImageView(0, height)
        pointer4 = getImageView(width, height)
        midPointer13 = getImageView(0, height / 2)
        midPointer13?.setOnTouchListener(MidPointTouchListenerImpl(pointer1, pointer3))
        midPointer12 = getImageView(0, width / 2)
        midPointer12?.setOnTouchListener(MidPointTouchListenerImpl(pointer1, pointer2))
        midPointer34 = getImageView(0, height / 2)
        midPointer34?.setOnTouchListener(MidPointTouchListenerImpl(pointer3, pointer4))
        midPointer24 = getImageView(0, height / 2)
        midPointer24?.setOnTouchListener(MidPointTouchListenerImpl(pointer2, pointer4))
        addView(pointer1)
        addView(pointer2)
        addView(midPointer13)
        addView(midPointer12)
        addView(midPointer34)
        addView(midPointer24)
        addView(pointer3)
        addView(pointer4)
        initPaint()
    }

    override fun attachViewToParent(child: View, index: Int, params: ViewGroup.LayoutParams) {
        super.attachViewToParent(child, index, params)
    }

    private fun initPaint() {
        paint = Paint().apply {
            color = context.getColorCompat(R.color.blue)
            strokeWidth = 2f
            isAntiAlias = true
        }
    }

    var points: Map<Int, PointF>
        get() {
            val points: MutableList<PointF> = ArrayList()
            points.add(PointF(pointer1?.x.orZero(), pointer1?.y.orZero()))
            points.add(PointF(pointer2?.x.orZero(), pointer2?.y.orZero()))
            points.add(PointF(pointer3?.x.orZero(), pointer3?.y.orZero()))
            points.add(PointF(pointer4?.x.orZero(), pointer4?.y.orZero()))
            return getOrderedPoints(points)
        }
        set(pointFMap) {
            if (pointFMap.size == 4) {
                setPointsCoordinates(pointFMap)
            }
        }

    fun getOrderedPoints(points: List<PointF>): Map<Int, PointF> {
        val centerPoint = PointF()
        val size = points.size
        for (pointF in points) {
            centerPoint.x += pointF.x / size
            centerPoint.y += pointF.y / size
        }
        val orderedPoints: MutableMap<Int, PointF> = HashMap()
        for (pointF in points) {
            var index = -1
            if (pointF.x < centerPoint.x && pointF.y < centerPoint.y) {
                index = 0
            } else if (pointF.x > centerPoint.x && pointF.y < centerPoint.y) {
                index = 1
            } else if (pointF.x < centerPoint.x && pointF.y > centerPoint.y) {
                index = 2
            } else if (pointF.x > centerPoint.x && pointF.y > centerPoint.y) {
                index = 3
            }
            orderedPoints[index] = pointF
        }
        return orderedPoints
    }

    private fun setPointsCoordinates(pointFMap: Map<Int, PointF>) {
        pointer1?.x = pointFMap[0]?.x.orZero()
        pointer1?.y = pointFMap[0]?.y.orZero()
        pointer2?.x = pointFMap[1]?.x.orZero()
        pointer2?.y = pointFMap[1]?.y.orZero()
        pointer3?.x = pointFMap[2]?.x.orZero()
        pointer3?.y = pointFMap[2]?.y.orZero()
        pointer4?.x = pointFMap[3]?.x.orZero()
        pointer4?.y = pointFMap[3]?.y.orZero()
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        paint?.let {
            canvas.drawLine(
                pointer1?.x.orZero() + pointer1?.width.orZero() / 2,
                pointer1?.y.orZero() + pointer1?.height.orZero() / 2,
                pointer3?.x.orZero() + pointer3?.width.orZero() / 2,
                pointer3?.y.orZero() + pointer3?.height.orZero() / 2,
                it
            )

            canvas.drawLine(
                pointer1?.x.orZero() + pointer1?.width.orZero() / 2,
                pointer1?.y.orZero() + pointer1?.height.orZero() / 2,
                pointer2?.x.orZero() + pointer2?.width.orZero() / 2,
                pointer2?.y.orZero() + pointer2?.height.orZero() / 2,
                it
            )

            canvas.drawLine(
                pointer2?.x.orZero() + pointer2?.width.orZero() / 2,
                pointer2?.y.orZero() + pointer2?.height.orZero() / 2,
                pointer4?.x.orZero() + pointer4?.width.orZero() / 2,
                pointer4?.y.orZero() + pointer4?.height.orZero() / 2,
                it
            )
            canvas.drawLine(
                pointer3?.x.orZero() + pointer3?.width.orZero() / 2,
                pointer3?.y.orZero() + pointer3?.height.orZero() / 2,
                pointer4?.x.orZero() + pointer4?.width.orZero() / 2,
                pointer4?.y.orZero() + pointer4?.height.orZero() / 2,
                it
            )
        }
        midPointer13?.x = pointer3?.x.orZero() - (pointer3?.x.orZero() - pointer1?.x.orZero()) / 2
        midPointer13?.y = pointer3?.y.orZero() - (pointer3?.y.orZero() - pointer1?.y.orZero()) / 2
        midPointer24?.x = pointer4?.x.orZero() - (pointer4?.x.orZero() - pointer2?.x.orZero()) / 2
        midPointer24?.y = pointer4?.y.orZero() - (pointer4?.y.orZero() - pointer2?.y.orZero()) / 2
        midPointer34?.x = pointer4?.x.orZero() - (pointer4?.x.orZero() - pointer3?.x.orZero()) / 2
        midPointer34?.y = pointer4?.y.orZero() - (pointer4?.y.orZero() - pointer3?.y.orZero()) / 2
        midPointer12?.x = pointer2?.x.orZero() - (pointer2?.x.orZero() - pointer1?.x.orZero()) / 2
        midPointer12?.y = pointer2?.y.orZero() - (pointer2?.y.orZero() - pointer1?.y.orZero()) / 2
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun getImageView(posX: Int, posY: Int): ImageView {
        val imageView = ImageView(context)
        val lParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        imageView.apply {
            layoutParams = lParams
            setImageResource(R.drawable.circle)
            x = posX.toFloat()
            y = posY.toFloat()
            setOnTouchListener(TouchListenerImpl())
        }
        return imageView
    }

    private inner class MidPointTouchListenerImpl(
        private val mainPointer1: ImageView?,
        private val mainPointer2: ImageView?
    ) : OnTouchListener {
        var downPT = PointF() // Record Mouse Position When Pressed Down
        var startPT = PointF() // Record Start Position of 'img'

        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val mv = PointF(event.x - downPT.x, event.y - downPT.y)
                    if (abs(mainPointer1?.x.orZero() - mainPointer2?.x.orZero()) > abs(
                            mainPointer1?.y.orZero() - mainPointer2?.y.orZero()
                        )
                    ) {
                        if (mainPointer2?.y.orZero() + mv.y + v.height < polygonView?.height.orZero() && mainPointer2?.y.orZero() + mv.y > 0) {
                            v.x = (startPT.y + mv.y)
                            startPT = PointF(v.x, v.y)
                            mainPointer2?.y = (mainPointer2?.y.orZero() + mv.y)
                        }
                        if (mainPointer1?.y.orZero() + mv.y + v.height < polygonView!!.height && mainPointer1?.y.orZero() + mv.y > 0) {
                            v.x = (startPT.y + mv.y)
                            startPT = PointF(v.x, v.y)
                            mainPointer1?.y = (mainPointer1?.y.orZero() + mv.y)
                        }
                    } else {
                        if (mainPointer2?.x.orZero() + mv.x + v.width < polygonView!!.width && mainPointer2?.x.orZero() + mv.x > 0) {
                            v.x = (startPT.x + mv.x)
                            startPT = PointF(v.x, v.y)
                            mainPointer2?.x = (mainPointer2?.x.orZero() + mv.x)
                        }
                        if (mainPointer1?.x.orZero() + mv.x + v.width < polygonView!!.width && mainPointer1?.x.orZero() + mv.x > 0) {
                            v.x = (startPT.x + mv.x)
                            startPT = PointF(v.x, v.y)
                            mainPointer1?.x = (mainPointer1?.x.orZero() + mv.x)
                        }
                    }
                }
                MotionEvent.ACTION_DOWN -> {
                    downPT.x = event.x
                    downPT.y = event.y
                    startPT = PointF(v.x, v.y)
                }
                MotionEvent.ACTION_UP -> {
                    var color = 0
                    color = if (isValidShape(points)) {
                        resources.getColor(R.color.blue)
                    } else {
                        resources.getColor(R.color.orange)
                    }
                    paint?.color = color
                }
                else -> {
                }
            }
            polygonView?.invalidate()
            return true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return super.onTouchEvent(event)
    }

    fun isValidShape(pointFMap: Map<Int, PointF>): Boolean {
        return pointFMap.size == 4
    }

    private inner class TouchListenerImpl : OnTouchListener {
        var downPT = PointF() // Record Mouse Position When Pressed Down
        var startPT = PointF() // Record Start Position of 'img'
        override fun onTouch(v: View, event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    val mv = PointF(event.x - downPT.x, event.y - downPT.y)
                    if (startPT.x + mv.x + v.width < polygonView?.width.orZero() && startPT.y + mv.y + v.height < polygonView?.height.orZero() && startPT.x + mv.x > 0 && startPT.y + mv.y > 0) {
                        v.x = (startPT.x + mv.x)
                        v.y = (startPT.y + mv.y)
                        startPT = PointF(v.x, v.y)
                    }
                }
                MotionEvent.ACTION_DOWN -> {
                    downPT.x = event.x
                    downPT.y = event.y
                    startPT = PointF(v.x, v.y)
                }
                MotionEvent.ACTION_UP -> {
                    var color = 0
                    color = if (isValidShape(points)) {
                        context.getColorCompat(R.color.blue)
                    } else {
                        context.getColorCompat(R.color.orange)
                    }
                    paint?.color = color
                }
                else -> {
                }
            }
            polygonView?.invalidate()
            return true
        }
    }
}