package app.hubhome.launcher.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.util.LruCache
import android.widget.ImageView
import java.util.concurrent.Executors

/** تحميل الأيقونات بحجم ثابت في خيط واحد مع ذاكرة مؤقتة صغيرة (مناسب لـ 1GB RAM). */
class IconLoader(ctx: Context, private val sizePx: Int) {
    private val pm = ctx.packageManager
    private val cache = LruCache<String, Bitmap>(48)
    private val io = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())

    fun bind(iv: ImageView, e: AppEntry) {
        iv.tag = e.key
        val c = cache.get(e.key)
        if (c != null) { iv.setImageBitmap(c); return }
        iv.setImageDrawable(null)
        io.execute {
            val bmp = render(e)
            cache.put(e.key, bmp)
            main.post { if (iv.tag == e.key) iv.setImageBitmap(bmp) }
        }
    }

    private fun render(e: AppEntry): Bitmap {
        val d = try { pm.getActivityIcon(e.component) } catch (ex: Exception) { pm.defaultActivityIcon }
        val b = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        d.setBounds(0, 0, sizePx, sizePx)
        d.draw(Canvas(b))
        return b
    }

    fun evictAll() = cache.evictAll()
    fun shutdown() = io.shutdownNow()
}
