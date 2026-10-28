package app.hubhome.launcher.ui

import android.graphics.*
import android.graphics.drawable.Drawable

/** أيقونات خطية بسيطة تُرسم بالكود (بدون ملفات صور). */
class GlyphDrawable(private val kind: Int) : Drawable() {
    companion object { const val HOME = 0; const val APPS = 1; const val CLOCK = 2; const val GEAR = 3; const val PLUS = 4; const val INPUT = 5 }

    private val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; color = Color.WHITE
        strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND; strokeWidth = 0.07f
    }
    private val path = Path()
    private val r = RectF()

    override fun draw(c: Canvas) {
        val b = bounds
        c.save()
        c.translate(b.left.toFloat(), b.top.toFloat())
        c.scale(b.width().toFloat(), b.height().toFloat())
        when (kind) {
            HOME -> {
                path.reset()
                path.moveTo(.15f, .5f); path.lineTo(.5f, .2f); path.lineTo(.85f, .5f)
                path.moveTo(.25f, .45f); path.lineTo(.25f, .8f); path.lineTo(.75f, .8f); path.lineTo(.75f, .45f)
                c.drawPath(path, p)
            }
            APPS -> {
                val s = floatArrayOf(.2f, .55f)
                for (x in s) for (y in s) { r.set(x, y, x + .25f, y + .25f); c.drawRoundRect(r, .05f, .05f, p) }
            }
            CLOCK -> {
                c.drawCircle(.5f, .5f, .32f, p)
                c.drawLine(.5f, .5f, .5f, .3f, p); c.drawLine(.5f, .5f, .65f, .58f, p)
            }
            GEAR -> {
                c.drawCircle(.5f, .5f, .15f, p); c.drawCircle(.5f, .5f, .28f, p)
                for (i in 0 until 8) {
                    val a = Math.toRadians(i * 45.0)
                    val cs = Math.cos(a).toFloat(); val sn = Math.sin(a).toFloat()
                    c.drawLine(.5f + cs * .28f, .5f + sn * .28f, .5f + cs * .38f, .5f + sn * .38f, p)
                }
            }
            PLUS -> { c.drawLine(.5f, .25f, .5f, .75f, p); c.drawLine(.25f, .5f, .75f, .5f, p) }
            INPUT -> {
                r.set(.15f, .25f, .85f, .65f); c.drawRoundRect(r, .05f, .05f, p)
                c.drawLine(.35f, .8f, .65f, .8f, p); c.drawLine(.5f, .65f, .5f, .8f, p)
            }
        }
        c.restore()
    }

    override fun setAlpha(a: Int) { p.alpha = a }
    override fun setColorFilter(cf: ColorFilter?) { p.colorFilter = cf }
    @Suppress("OVERRIDE_DEPRECATION")
    override fun getOpacity(): Int = PixelFormat.TRANSLUCENT
}
