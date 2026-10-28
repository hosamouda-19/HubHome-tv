package app.hubhome.launcher.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import app.hubhome.launcher.data.AppEntry

/** بطاقة تطبيق: لوح زجاجي شبه شفاف + أيقونة + اسم. التركيز = إطار أبيض وتكبير خفيف (transform فقط). */
class Tile(ctx: Context, sizeDp: Int, var animEnabled: Boolean) : LinearLayout(ctx) {
    val iconView = ImageView(ctx)
    var entry: AppEntry? = null
    var row: String = ""
    var focusKey: String = ""

    private val plate = FrameLayout(ctx)
    private val label = TextView(ctx)
    private val normalBg = GradientDrawable().apply { cornerRadius = ctx.dp(18f).toFloat(); setColor(0x33FFFFFF) }
    private val focusBg = GradientDrawable().apply {
        cornerRadius = ctx.dp(18f).toFloat(); setColor(0x59FFFFFF); setStroke(ctx.dp(3), Color.WHITE)
    }

    init {
        orientation = VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        isFocusable = true
        isClickable = true
        clipChildren = false
        val s = ctx.dp(sizeDp)
        val inner = (s * 0.6f).toInt()
        plate.background = normalBg
        iconView.scaleType = ImageView.ScaleType.FIT_CENTER
        plate.addView(iconView, FrameLayout.LayoutParams(inner, inner, Gravity.CENTER))
        addView(plate, LayoutParams(s, s))
        label.apply {
            setUiSize(13f); setTextColor(Color.WHITE); alpha = 0.65f
            maxLines = 1; ellipsize = TextUtils.TruncateAt.END; gravity = Gravity.CENTER
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        }
        addView(label, LayoutParams(s + ctx.dp(16), LayoutParams.WRAP_CONTENT).apply { topMargin = ctx.dp(8) })
    }

    fun setLabel(t: CharSequence) { label.text = t }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        plate.background = if (gainFocus) focusBg else normalBg
        label.alpha = if (gainFocus) 1f else 0.65f
        val s = if (gainFocus && animEnabled) 1.12f else 1f
        animate().scaleX(s).scaleY(s).setDuration(if (animEnabled) 110L else 0L).start()
    }
}
