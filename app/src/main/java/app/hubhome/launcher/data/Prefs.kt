package app.hubhome.launcher.data

import android.content.Context

/** إعدادات محلية خفيفة (SharedPreferences) تبقى بعد إعادة تشغيل التلفزيون. */
class Prefs(ctx: Context) {
    private val sp = ctx.getSharedPreferences("hub", Context.MODE_PRIVATE)

    var favorites: List<String>
        get() = (sp.getString("fav", "") ?: "").split('\n').filter { it.isNotEmpty() }
        set(v) { sp.edit().putString("fav", v.joinToString("\n")).apply() }

    var favInit: Boolean
        get() = sp.getBoolean("favInit", false)
        set(v) { sp.edit().putBoolean("favInit", v).apply() }

    var wallpaper: Int
        get() = sp.getInt("wp", 0)
        set(v) { sp.edit().putInt("wp", v).apply() }

    /** 0 = النظام، 1 = 12 ساعة، 2 = 24 ساعة */
    var clockMode: Int
        get() = sp.getInt("clock", 0)
        set(v) { sp.edit().putInt("clock", v).apply() }

    var showRecent: Boolean
        get() = sp.getBoolean("recent", true)
        set(v) { sp.edit().putBoolean("recent", v).apply() }

    var showInputs: Boolean
        get() = sp.getBoolean("inputs", true)
        set(v) { sp.edit().putBoolean("inputs", v).apply() }

    var anim: Boolean
        get() = sp.getBoolean("anim", true)
        set(v) { sp.edit().putBoolean("anim", v).apply() }

    var recents: List<String>
        get() = (sp.getString("recents", "") ?: "").split('\n').filter { it.isNotEmpty() }
        set(v) { sp.edit().putString("recents", v.joinToString("\n")).apply() }

    fun pushRecent(key: String) {
        val l = recents.toMutableList()
        l.remove(key)
        l.add(0, key)
        recents = l.take(12)
    }
}
