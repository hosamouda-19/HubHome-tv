package app.hubhome.launcher.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executors

data class AppEntry(val key: String, val label: String, val component: ComponentName) {
    val pkg: String get() = component.packageName
}

/** اكتشاف التطبيقات المثبتة عبر PackageManager (بدون QUERY_ALL_PACKAGES). */
class AppRepository(private val ctx: Context) {
    private val pm = ctx.packageManager
    private val io = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val byKey = HashMap<String, AppEntry>()

    var apps: List<AppEntry> = emptyList()
        private set
    private var loaded = false

    fun find(key: String): AppEntry? = byKey[key]

    /** يحمّل القائمة في خيط خلفي، ثم يستدعي onDone على الخيط الرئيسي (changed = تغيّرت القائمة). */
    fun refreshAsync(onDone: (changed: Boolean) -> Unit) {
        io.execute {
            val list = load()
            main.post {
                val changed = !loaded || list.map { it.key } != apps.map { it.key }
                loaded = true
                if (changed) {
                    apps = list
                    byKey.clear()
                    list.forEach { byKey[it.key] = it }
                }
                onDone(changed)
            }
        }
    }

    private fun load(): List<AppEntry> {
        val seen = HashSet<String>()
        val out = ArrayList<AppEntry>()
        // تطبيقات التلفزيون أولًا، ثم التطبيقات العادية التي لا تعلن LEANBACK
        for (cat in arrayOf(Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER)) {
            val q = Intent(Intent.ACTION_MAIN).addCategory(cat)
            val list = try { pm.queryIntentActivities(q, 0) } catch (e: Exception) { emptyList() }
            for (ri in list) {
                val ai = ri.activityInfo ?: continue
                if (ai.packageName == ctx.packageName) continue
                if (!seen.add(ai.packageName)) continue
                val cn = ComponentName(ai.packageName, ai.name)
                val label = try { ri.loadLabel(pm).toString() } catch (e: Exception) { ai.packageName }
                out.add(AppEntry(cn.flattenToString(), label, cn))
            }
        }
        out.sortBy { it.label.lowercase() }
        return out
    }

    fun shutdown() = io.shutdownNow()
}
