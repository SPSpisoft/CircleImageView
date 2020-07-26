package com.dingjianlun.circleimageview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round

@SuppressLint("AppCompatCustomView")
class CircleImageView(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int,
    defStyleRes: Int
) : ImageView(context, attrs, defStyleAttr, defStyleRes) {

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : this(context, attrs, defStyleAttr, 0)

    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : this(context, attrs, 0)

    constructor(
        context: Context
    ) : this(context, null)

    private var type = Type.Circle

    private var borderColor = 0
    private var borderWidth = 0

    private var leftTopRadius = 0
    private var leftBottomRadius = 0
    private var rightTopRadius = 0
    private var rightBottomRadius = 0

    private val rectF = RectF()

    private val roundPath = Path()
    private val bitmapPaint = Paint().apply { isAntiAlias = true }
    private val borderPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
    }

    private var ratio: Float = 0f

    init {
        val a = context.obtainStyledAttributes(
            attrs,
            R.styleable.CircleImageView,
            defStyleAttr,
            defStyleRes
        )

        type = a.getInt(R.styleable.CircleImageView_civ_type, type.i).let { Type.from(it) }

        borderColor = a.getColor(R.styleable.CircleImageView_civ_border_color, Color.BLACK)
        borderWidth = a.getDimensionPixelSize(R.styleable.CircleImageView_civ_border_width, 0)

        val radius = a.getDimensionPixelSize(R.styleable.CircleImageView_civ_corner_radius, 0)

        leftTopRadius =
            a.getDimensionPixelSize(R.styleable.CircleImageView_civ_left_top_radius, radius)
        leftBottomRadius =
            a.getDimensionPixelSize(R.styleable.CircleImageView_civ_left_bottom_radius, radius)
        rightTopRadius =
            a.getDimensionPixelSize(R.styleable.CircleImageView_civ_right_top_radius, radius)
        rightBottomRadius =
            a.getDimensionPixelSize(R.styleable.CircleImageView_civ_right_bottom_radius, radius)

        val ratioWidth = a.getInteger(R.styleable.CircleImageView_civ_ratio_width, 0)
        val ratioHeight = a.getInteger(R.styleable.CircleImageView_civ_ratio_height, 0)
        if (ratioWidth != 0 && ratioHeight != 0) ratio = 1f * ratioWidth / ratioHeight

        a.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (ratio != 0f) {
            val width = MeasureSpec.getSize(widthMeasureSpec)
            val height = MeasureSpec.getSize(heightMeasureSpec)
            val r = 1f * width / height
            val w: Int
            val h: Int
            if (r < ratio) {
                w = width
                h = (w / ratio).toInt()
            } else {
                h = height
                w = (h * ratio).toInt()
            }
            super.onMeasure(
                MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY)
            )
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)

        val drawable = drawable ?: background
        drawable ?: return

        val bitmap = drawableToBitmap(drawable)

        val w = width
        val h = height
        val bw = bitmap.width
        val bh = bitmap.height

        val matrix = getMatrix(w, h, bw, bh)
        setBitmapShader(bitmap, matrix, bitmapPaint)

        when (type) {
            Type.Circle -> drawCircle(canvas, w, h)
            Type.Round -> drawRound(canvas, w, h, matrix, bw, bh)
        }

    }

    private fun drawRound(
        canvas: Canvas,
        w: Int,
        h: Int,
        matrix: Matrix,
        bw: Int,
        bh: Int
    ) {
        val values = FloatArray(9) { 0f }
        matrix.getValues(values)
        val startX = values[Matrix.MTRANS_X].let { if (it < 0) 0f else it }
        val startY = values[Matrix.MTRANS_Y].let { if (it < 0) 0f else it }
        val endX = (values[Matrix.MSCALE_X] * bw + startX).let { if (it > w) w.toFloat() else it }
        val endY = (values[Matrix.MSCALE_Y] * bh + startY).let { if (it > h) h.toFloat() else it }

        rectF.set(
            startX + borderWidth / 2f,
            startY + borderWidth / 2f,
            endX - borderWidth / 2f,
            endY - borderWidth / 2f
        )
        roundPath.reset()
        roundPath.addRoundRect(
            rectF,
            floatArrayOf(
                leftTopRadius.toFloat(), leftTopRadius.toFloat(),
                rightTopRadius.toFloat(), rightTopRadius.toFloat(),
                rightBottomRadius.toFloat(), rightBottomRadius.toFloat(),
                leftBottomRadius.toFloat(), leftBottomRadius.toFloat()
            ),
            Path.Direction.CW
        )

        canvas.drawPath(roundPath, bitmapPaint)

        if (borderWidth > 0) {
            borderPaint.color = borderColor
            borderPaint.strokeWidth = borderWidth.toFloat()
            canvas.drawPath(roundPath, borderPaint)
        }
    }

    private fun drawCircle(canvas: Canvas, w: Int, h: Int) {
        val radius = (min(w, h) - borderWidth) / 2f
        canvas.drawCircle(w / 2f, h / 2f, radius, bitmapPaint)

        if (borderWidth > 0) {
            borderPaint.color = borderColor
            borderPaint.strokeWidth = borderWidth.toFloat()
            canvas.drawCircle(w / 2f, h / 2f, radius, borderPaint)
        }
    }

    private fun getMatrix(w: Int, h: Int, bw: Int, bh: Int): Matrix {
        val scaleType = scaleType
        val matrix = Matrix()
        when (scaleType) {
            ScaleType.CENTER -> {//
                matrix.setTranslate(round((w - bw) * 0.5f), round((h - bh) * 0.5f))
            }
            ScaleType.CENTER_CROP -> {
                val scale: Float
                val dx: Float
                val dy: Float
                if (bw * h > w * bh) {
                    scale = h / bh.toFloat()
                    dx = (w - bw * scale) * 0.5f
                    dy = 0f
                } else {
                    scale = w / bw.toFloat()
                    dx = 0f
                    dy = (h - bh * scale) * 0.5f
                }
                matrix.setScale(scale, scale)
                matrix.postTranslate(round(dx), round(dy))
            }
            ScaleType.CENTER_INSIDE -> {
                val scale: Float = if (bw <= w && bh <= h) 1.0f
                else min(w / bw.toFloat(), h / bh.toFloat())
                val dx: Float = round((w - bw * scale) * 0.5f)
                val dy: Float = round((h - bh * scale) * 0.5f)
                matrix.setScale(scale, scale)
                matrix.postTranslate(dx, dy)
            }
            else -> {
                val src = RectF(0f, 0f, bw.toFloat(), bh.toFloat())
                val dst = RectF(0f, 0f, w.toFloat(), h.toFloat())
                val stf = when (scaleType) {
                    ScaleType.FIT_XY -> Matrix.ScaleToFit.FILL
                    ScaleType.FIT_START -> Matrix.ScaleToFit.START
                    ScaleType.FIT_CENTER -> Matrix.ScaleToFit.CENTER
                    ScaleType.FIT_END -> Matrix.ScaleToFit.END
                    else -> Matrix.ScaleToFit.FILL
                }
                matrix.setRectToRect(src, dst, stf)
            }
        }
        return matrix
    }

    private fun setBitmapShader(bitmap: Bitmap, matrix: Matrix, paint: Paint) {
        val bitmapShader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        bitmapShader.setLocalMatrix(matrix)
        paint.shader = bitmapShader
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val intrinsicWidth = max(drawable.intrinsicWidth, 1)
        val intrinsicHeight = max(drawable.intrinsicHeight, 1)
        val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, intrinsicWidth, intrinsicHeight)
        drawable.draw(canvas)
        canvas.save()
        canvas.restore()

        val displayMetrics = resources.displayMetrics
        val scaledWidth = bitmap.getScaledWidth(displayMetrics)
        val scaledHeight = bitmap.getScaledHeight(displayMetrics)

        return Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888).apply {
            Canvas(this).apply {
                drawBitmap(
                    bitmap,
                    Rect(0, 0, bitmap.width, bitmap.height),
                    RectF(0f, 0f, scaledWidth.toFloat(), scaledHeight.toFloat()),
                    Paint().apply { isAntiAlias = true; isFilterBitmap = true }
                )
                save()
                restore()
            }
        }

    }

    enum class Type(val i: Int) {
        Circle(1), Round(2);

        companion object {
            fun from(i: Int) = values().first { it.i == i }
        }
    }

    fun setType(type: Type) = apply {
        if (this.type == type) return@apply
        this.type = type
        invalidate()
    }

    fun setCornerRadius(radius: Int) = setCornerRadius(radius, radius, radius, radius)

    fun setCornerRadius(
        leftTop: Int = this.leftTopRadius,
        leftBottom: Int = this.leftBottomRadius,
        rightTop: Int = this.rightTopRadius,
        rightBottom: Int = this.rightBottomRadius
    ) = apply {
        if (this.leftTopRadius == leftTop && this.leftBottomRadius == leftBottom &&
            this.rightTopRadius == rightTop && this.rightBottomRadius == rightBottom
        ) return@apply
        this.leftTopRadius = leftTop
        this.leftBottomRadius = leftBottom
        this.rightTopRadius = rightTop
        this.rightBottomRadius = rightBottom
        invalidate()
    }

    fun setBorderWidth(width: Int) = apply {
        if (this.borderWidth == width) return@apply
        this.borderWidth = width
        invalidate()
    }

    fun setBorderColor(color: Int) = apply {
        if (this.borderColor == color) return@apply
        this.borderColor = color
        invalidate()
    }

}