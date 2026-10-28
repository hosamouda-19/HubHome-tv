package app.hubhome.launcher.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

/** عنصر الشريط الجانبي: أيقونة، والاسم يظهر عند توسّع الشريط. */
class NavItem(ctx: Context, glyph: Int, text: String) : LinearLayout(ctx) {
    private val icon = ImageView(ctx)
    private val label = TextView(ctx)
    private val focusBg = GradientDrawable().apply { cornerRadius = ctx.dp(24f).toFloat(); setColor(0x40FFFFFF) }
    var onFocused: (() -> Unit)? = null
    var active: Boolean = false
        set(v) { field = v; refresh() }

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        isFocusable = true
        isClickable = true
        setPadding(ctx.dp(14), ctx.dp(10), ctx.dp(18), ctx.dp(10))
        icon.setImageDrawable(GlyphDrawable(glyph))
        addView(icon, LayoutParams(ctx.dp(28), ctx.dp(28)))
        label.apply {
            this.text = text; setUiSize(16f); setTextColor(Color.WHITE)
            visibility = GONE; setPadding(ctx.dp(14), 0, ctx.dp(8), 0)
        }
        addView(label)
        refresh()
    }

    fun setExpanded(b: Boolean) { label.visibility = if (b) VISIBLE else GONE }

    private fun refresh() { icon.alpha = if (active || isFocused) 1f else 0.55f }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        background = if (gainFocus) focusBg else null
        refresh()
        if (gainFocus) onFocused?.invoke()
    }
}
