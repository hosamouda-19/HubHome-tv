package app.hubhome.launcher.ui

import android.graphics.Typeface
import android.view.View
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import app.hubhome.launcher.MainActivity
import app.hubhome.launcher.R

/** شبكة كل التطبيقات المثبتة. تُبنى عند أول فتح فقط لتوفير الذاكرة. */
class AppsPage(private val a: MainActivity) : ScrollView(a) {
    private val col = LinearLayout(a)
    var built = false
        private set

    init {
        clipChildren = false
        clipToPadding = false
        isVerticalScrollBarEnabled = false
        overScrollMode = OVER_SCROLL_NEVER
        setPadding(a.dp(110), a.dp(60), 0, a.dp(24))
        col.orientation = LinearLayout.VERTICAL
        col.clipChildren = false
        col.clipToPadding = false
        addView(col, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
    }

    fun clear() { col.removeAllViews(); built = false }

    fun rebuild() {
        col.removeAllViews()
        col.addView(TextView(a).apply {
            text = a.getString(R.string.apps_title); setUiSize(24f); setTextColor(0xFFFFFFFF.toInt())
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            setPadding(0, 0, 0, a.dp(12))
        })
        val apps = a.repo.apps
        var i = 0
        while (i < apps.size) {
            val row = LinearLayout(a).apply {
                orientation = LinearLayout.HORIZONTAL; clipChildren = false
                setPadding(0, a.dp(8), 0, a.dp(8))
            }
            for (j in i until minOf(i + COLS, apps.size)) {
                val t = a.makeTile(apps[j], "all", 92)
                row.addView(t, LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = a.dp(24) })
            }
            col.addView(row)
            i += COLS
        }
        built = true
    }

    companion object { const val COLS = 6 }
}
