package app.hubhome.launcher.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.provider.Settings
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import app.hubhome.launcher.BuildConfig
import app.hubhome.launcher.MainActivity
import app.hubhome.launcher.R

class SettingRow(ctx: Context, title: String) : LinearLayout(ctx) {
    private val valueView = TextView(ctx)
    private val focusBg = GradientDrawable().apply { cornerRadius = ctx.dp(14f).toFloat(); setColor(0x40FFFFFF) }
    private val normalBg = GradientDrawable().apply { cornerRadius = ctx.dp(14f).toFloat(); setColor(0x1AFFFFFF) }

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        isFocusable = true; isClickable = true
        background = normalBg
        setPadding(ctx.dp(20), 0, ctx.dp(20), 0)
        addView(TextView(ctx).apply { text = title; setUiSize(17f); setTextColor(Color.WHITE) },
            LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f))
        valueView.apply { setUiSize(16f); setTextColor(0xCCFFFFFF.toInt()) }
        addView(valueView)
    }

    fun setValue(v: String) { valueView.text = v }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
        background = if (gainFocus) focusBg else normalBg
    }
}

/** صفحة إعدادات الـ Launcher. كل صف يغيّر سلوكًا فعليًا ويُحفظ محليًا. */
class SettingsPage(private val a: MainActivity) : ScrollView(a) {
    private val col = LinearLayout(a)

    init {
        isVerticalScrollBarEnabled = false
        overScrollMode = OVER_SCROLL_NEVER
        setPadding(a.dp(110), a.dp(60), 0, a.dp(24))
        col.orientation = LinearLayout.VERTICAL
        addView(col, LayoutParams(a.dp(600), LayoutParams.WRAP_CONTENT))
        build()
    }

    private fun onOff(b: Boolean) = a.getString(if (b) R.string.on else R.string.off)

    private fun row(titleRes: Int, value: () -> String, action: () -> Unit) {
        val r = SettingRow(a, a.getString(titleRes))
        r.setValue(value())
        r.setOnClickListener { action(); r.setValue(value()) }
        col.addView(r, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, a.dp(56))
            .apply { bottomMargin = a.dp(8) })
    }

    private fun build() {
        val p = a.prefs
        col.addView(TextView(a).apply {
            text = a.getString(R.string.set_title); setUiSize(24f); setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            setPadding(0, 0, 0, a.dp(14))
        })
        val wpNames = a.resources.getStringArray(R.array.wallpaper_names)
        row(R.string.set_wallpaper, { wpNames[p.wallpaper % wpNames.size] }) {
            p.wallpaper = (p.wallpaper + 1) % Wallpapers.count; a.applyWallpaper()
        }
        row(R.string.set_clock, {
            a.getString(when (p.clockMode) { 1 -> R.string.set_clock_12; 2 -> R.string.set_clock_24; else -> R.string.set_clock_system })
        }) { p.clockMode = (p.clockMode + 1) % 3; a.applyClock() }
        row(R.string.set_recent, { onOff(p.showRecent) }) { p.showRecent = !p.showRecent; a.rebuildHome() }
        row(R.string.set_inputs, { onOff(p.showInputs) }) { p.showInputs = !p.showInputs; a.rebuildHome() }
        row(R.string.set_anim, { onOff(p.anim) }) { p.anim = !p.anim; a.rebuildHome(); a.markAppsDirty() }
        row(R.string.set_reset, { "" }) { p.favorites = emptyList(); p.favInit = false; a.rebuildHome() }
        row(R.string.set_default_home, { "" }) { a.openDefaultHomeSettings() }
        row(R.string.set_system, { "" }) {
            try { a.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
            catch (e: Exception) { Toast.makeText(a, R.string.toast_unavailable, Toast.LENGTH_SHORT).show() }
        }
        col.addView(TextView(a).apply {
            text = a.getString(R.string.set_version, BuildConfig.VERSION_NAME)
            setUiSize(13f); setTextColor(0x99FFFFFF.toInt()); setPadding(a.dp(4), a.dp(8), 0, 0)
        })
    }
}
