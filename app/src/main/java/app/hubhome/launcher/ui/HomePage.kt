package app.hubhome.launcher.ui

import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import app.hubhome.launcher.MainActivity
import app.hubhome.launcher.R

/** الشاشة الرئيسية: صفوف أفقية (المفضلة، المفتوحة مؤخرًا، المصادر). */
class HomePage(private val a: MainActivity) : ScrollView(a) {
    private val col = LinearLayout(a)
    private val tiles = HashMap<String, Tile>()
    private var firstTile: Tile? = null

    init {
        clipChildren = false
        clipToPadding = false
        isVerticalScrollBarEnabled = false
        overScrollMode = OVER_SCROLL_NEVER
        setPadding(a.dp(110), a.dp(96), 0, a.dp(24))
        col.orientation = LinearLayout.VERTICAL
        col.clipChildren = false
        col.clipToPadding = false
        addView(col, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT))
    }

    fun findTile(key: String?): Tile? = if (key == null) null else tiles[key]
    fun firstTile(): Tile? = firstTile

    fun rebuild() {
        col.removeAllViews()
        tiles.clear()
        firstTile = null

        val favs = a.favoriteEntries()
        addRow(a.getString(R.string.row_favorites)) { row ->
            for (e in favs) row.addView(register(a.makeTile(e, "fav", 120)), lp())
            val add = Tile(a, 120, a.prefs.anim)
            add.iconView.setImageDrawable(GlyphDrawable(GlyphDrawable.PLUS))
            add.setLabel(a.getString(R.string.tile_add))
            add.focusKey = "fav:+"
            add.setOnClickListener { a.openAppsPage() }
            row.addView(register(add), lp())
        }
        if (a.prefs.showRecent) {
            val rec = a.recentEntries()
            if (rec.isNotEmpty()) addRow(a.getString(R.string.row_recent)) { row ->
                for (e in rec) row.addView(register(a.makeTile(e, "recent", 96)), lp())
            }
        }
        if (a.prefs.showInputs) {
            val ins = a.inputs()
            if (ins.isNotEmpty()) addRow(a.getString(R.string.row_inputs)) { row ->
                for (i in ins) {
                    val t = Tile(a, 96, a.prefs.anim)
                    t.iconView.setImageDrawable(GlyphDrawable(GlyphDrawable.INPUT))
                    t.setLabel(i.label)
                    t.focusKey = "input:${i.id}"
                    t.setOnClickListener { a.openInput(i) }
                    row.addView(register(t), lp())
                }
            }
        }
    }

    private fun register(t: Tile): Tile {
        tiles[t.focusKey] = t
        if (firstTile == null) firstTile = t
        return t
    }

    private fun lp() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply { marginEnd = a.dp(22) }

    private fun addRow(title: String, fill: (LinearLayout) -> Unit) {
        col.addView(TextView(a).apply {
            text = title; setUiSize(18f); setTextColor(0xE6FFFFFF.toInt())
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            setPadding(0, a.dp(18), 0, a.dp(6))
        })
        val inner = LinearLayout(a).apply {
            orientation = LinearLayout.HORIZONTAL
            clipChildren = false; clipToPadding = false
            setPadding(0, a.dp(10), a.dp(60), a.dp(14))
        }
        fill(inner)
        col.addView(HorizontalScrollView(a).apply {
            isHorizontalScrollBarEnabled = false
            overScrollMode = View.OVER_SCROLL_NEVER
            clipChildren = false; clipToPadding = false
            addView(inner)
        })
    }
}
