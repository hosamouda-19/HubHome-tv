package app.hubhome.launcher.ui

import android.graphics.drawable.GradientDrawable

/** خلفيات متدرجة تُرسم بالكود: لا صور ولا ضبابية، أي لا تكلفة ذاكرة تُذكر. */
object Wallpapers {
    private val SETS = arrayOf(
        intArrayOf(0xFF0B1020.toInt(), 0xFF111B3A.toInt(), 0xFF1B2A5E.toInt()),
        intArrayOf(0xFF06151A.toInt(), 0xFF0B3A3F.toInt(), 0xFF1E6F63.toInt()),
        intArrayOf(0xFF150B1F.toInt(), 0xFF3B1B4F.toInt(), 0xFF7A3E6B.toInt()),
        intArrayOf(0xFF140A08.toInt(), 0xFF3A1A12.toInt(), 0xFF7C3A1D.toInt()),
        intArrayOf(0xFF08090B.toInt(), 0xFF16181D.toInt(), 0xFF2A2D35.toInt()),
        intArrayOf(0xFF04121F.toInt(), 0xFF0A3350.toInt(), 0xFF1478A6.toInt())
    )
    val count: Int get() = SETS.size

    fun make(i: Int): GradientDrawable =
        GradientDrawable(GradientDrawable.Orientation.TL_BR, SETS[((i % SETS.size) + SETS.size) % SETS.size])
            .apply { setDither(true) }
}
