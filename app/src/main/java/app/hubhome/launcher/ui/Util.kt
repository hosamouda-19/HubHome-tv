package app.hubhome.launcher.ui

import android.content.Context
import android.util.TypedValue
import android.widget.TextView

/**
 * التصميم المرجعي 960×540 وحدة. المقياس يُحسب من دقة الشاشة الفعلية بالبكسل
 * (وليس من الكثافة التي يبلّغ عنها النظام)، فتظهر الواجهة بنفس النسب على أي دقة.
 */
fun Context.uiScale(): Float {
    val dm = resources.displayMetrics
    return maxOf(dm.widthPixels, dm.heightPixels) / 960f
}

fun Context.dp(v: Float): Int = (v * uiScale() + 0.5f).toInt()
fun Context.dp(v: Int): Int = dp(v.toFloat())

/** حجم الخط بنفس المقياس (بالبكسل، بلا تأثر بـ density أو fontScale). */
fun TextView.setUiSize(v: Float) = setTextSize(TypedValue.COMPLEX_UNIT_PX, v * context.uiScale())
