package app.hubhome.launcher

import android.app.Activity
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.tv.TvContract
import android.media.tv.TvInputManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextClock
import android.widget.TextView
import android.widget.Toast
import app.hubhome.launcher.data.AppEntry
import app.hubhome.launcher.data.AppRepository
import app.hubhome.launcher.data.IconLoader
import app.hubhome.launcher.data.Prefs
import app.hubhome.launcher.ui.*
import java.util.Collections

class InputEntry(val id: String, val label: String)

class MainActivity : Activity() {
    lateinit var prefs: Prefs
    lateinit var repo: AppRepository
    private lateinit var icons: IconLoader

    private lateinit var root: FrameLayout
    private lateinit var home: HomePage
    private lateinit var appsPage: AppsPage
    private lateinit var settingsPage: SettingsPage
    private lateinit var sidebar: LinearLayout
    private lateinit var navItems: List<NavItem>
    private lateinit var header: LinearLayout
    private lateinit var ambient: LinearLayout
    private lateinit var timeClock: TextClock
    private lateinit var dateClock: TextClock
    private lateinit var bigClock: TextClock
    private lateinit var bigDate: TextClock

    private var page = PAGE_HOME
    private var sidebarExpanded = false
    private var ambientOn = false
    private var started = false
    private var homeDirty = false
    private var appsDirty = true
    private val lastFocus = arrayOfNulls<View>(3)

    private val pkgReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) { refreshApps() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = Prefs(this)
        repo = AppRepository(this)
        icons = IconLoader(this, dp(72))
        buildUi()
        window.decorView.viewTreeObserver.addOnGlobalFocusChangeListener(
            ViewTreeObserver.OnGlobalFocusChangeListener { _, n -> onFocusMoved(n) })
        refreshApps()
    }

    // ---------------------------------------------------------------- UI

    private fun buildUi() {
        root = FrameLayout(this)
        applyWallpaper()

        home = HomePage(this)
        appsPage = AppsPage(this)
        settingsPage = SettingsPage(this)
        appsPage.visibility = View.GONE
        settingsPage.visibility = View.GONE
        root.addView(home, match())
        root.addView(appsPage, match())
        root.addView(settingsPage, match())

        // الساعة والتاريخ أعلى اليمين
        timeClock = TextClock(this).apply {
            setUiSize(40f); setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        }
        dateClock = TextClock(this).apply {
            setUiSize(15f); setTextColor(0xCCFFFFFF.toInt())
            format12Hour = "EEEE, d MMMM"; format24Hour = "EEEE, d MMMM"
        }
        header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.RIGHT
            addView(timeClock); addView(dateClock)
        }
        root.addView(header, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.RIGHT
        ).apply { topMargin = dp(28); rightMargin = dp(48) })

        // الشريط الجانبي
        navItems = listOf(
            NavItem(this, GlyphDrawable.HOME, getString(R.string.nav_home)),
            NavItem(this, GlyphDrawable.APPS, getString(R.string.nav_apps)),
            NavItem(this, GlyphDrawable.CLOCK, getString(R.string.nav_ambient)),
            NavItem(this, GlyphDrawable.GEAR, getString(R.string.nav_settings))
        )
        navItems[0].onFocused = { showPage(PAGE_HOME) }
        navItems[1].onFocused = { showPage(PAGE_APPS) }
        navItems[3].onFocused = { showPage(PAGE_SETTINGS) }
        navItems[2].setOnClickListener { showAmbient() }
        sidebar = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(12), 0, dp(40), 0)
            navItems.forEach { addView(it, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(10) }) }
        }
        root.addView(sidebar, FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT))

        // وضع الساعة (Ambient) بسيط وخفيف
        bigClock = TextClock(this).apply {
            setUiSize(110f); setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif-thin", Typeface.NORMAL)
        }
        bigDate = TextClock(this).apply {
            setUiSize(24f); setTextColor(0xCCFFFFFF.toInt())
            format12Hour = "EEEE, d MMMM"; format24Hour = "EEEE, d MMMM"
        }
        ambient = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; visibility = View.GONE
            addView(bigClock); addView(bigDate)
        }
        root.addView(ambient, match())

        applyClock()
        setContentView(root)
        navItems[0].active = true
    }

    private fun match() = FrameLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

    fun applyWallpaper() { root.background = Wallpapers.make(prefs.wallpaper) }

    fun applyClock() {
        for (c in listOf(timeClock, bigClock)) when (prefs.clockMode) {
            1 -> { c.format12Hour = "h:mm"; c.format24Hour = "h:mm" }
            2 -> { c.format12Hour = "HH:mm"; c.format24Hour = "HH:mm" }
            else -> { c.format12Hour = null; c.format24Hour = null }
        }
    }

    private fun showPage(p: Int) {
        if (page == p) return
        page = p
        home.visibility = if (p == PAGE_HOME) View.VISIBLE else View.GONE
        appsPage.visibility = if (p == PAGE_APPS) View.VISIBLE else View.GONE
        settingsPage.visibility = if (p == PAGE_SETTINGS) View.VISIBLE else View.GONE
        header.visibility = if (p == PAGE_HOME) View.VISIBLE else View.GONE
        navItems[0].active = p == PAGE_HOME
        navItems[1].active = p == PAGE_APPS
        navItems[3].active = p == PAGE_SETTINGS
        if (p == PAGE_APPS && (appsDirty || !appsPage.built) && repo.apps.isNotEmpty()) {
            appsPage.rebuild(); appsDirty = false
        }
    }

    fun openAppsPage() { navItems[1].requestFocus() }

    fun markAppsDirty() {
        appsDirty = true
        if (page == PAGE_APPS) { appsPage.rebuild(); appsDirty = false }
    }

    private fun navFor(p: Int): NavItem = navItems[when (p) { PAGE_APPS -> 1; PAGE_SETTINGS -> 3; else -> 0 }]

    private fun pageView(p: Int): View = when (p) { PAGE_APPS -> appsPage; PAGE_SETTINGS -> settingsPage; else -> home }

    private fun inSidebar(v: View): Boolean {
        var p: View? = v
        while (p != null) { if (p === sidebar) return true; p = p.parent as? View }
        return false
    }

    private fun onFocusMoved(n: View?) {
        val side = n != null && inSidebar(n)
        setSidebarExpanded(side)
        if (n != null && !side) lastFocus[page] = n
    }

    private fun setSidebarExpanded(b: Boolean) {
        if (sidebarExpanded == b) return
        sidebarExpanded = b
        navItems.forEach { it.setExpanded(b) }
        sidebar.background = if (b) GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
            intArrayOf(0xE605070D.toInt(), 0x0005070D)) else null
    }

    private fun focusPage() {
        val last = lastFocus[page]
        if (last != null && last.isAttachedToWindow && last.isShown && last.requestFocus()) return
        if (page == PAGE_HOME) home.firstTile()?.requestFocus()?.let { if (it) return }
        val l = ArrayList<View>()
        pageView(page).addFocusables(l, View.FOCUS_DOWN)
        l.firstOrNull()?.requestFocus()
    }

    private fun showAmbient() {
        ambientOn = true
        sidebar.visibility = View.GONE; header.visibility = View.GONE
        pageView(page).visibility = View.GONE
        ambient.visibility = View.VISIBLE
    }

    private fun hideAmbient() {
        ambientOn = false
        ambient.visibility = View.GONE
        sidebar.visibility = View.VISIBLE
        pageView(page).visibility = View.VISIBLE
        header.visibility = if (page == PAGE_HOME) View.VISIBLE else View.GONE
        navFor(page).requestFocus()
    }

    // ------------------------------------------------------------ Remote keys

    override fun dispatchKeyEvent(e: KeyEvent): Boolean {
        if (ambientOn) { if (e.action == KeyEvent.ACTION_UP) hideAmbient(); return true }
        if (e.action == KeyEvent.ACTION_DOWN) {
            val f = currentFocus
            when (e.keyCode) {
                KeyEvent.KEYCODE_DPAD_LEFT -> if (f != null && !inSidebar(f)) {
                    val n = f.focusSearch(View.FOCUS_LEFT)
                    if (n == null || inSidebar(n)) { navFor(page).requestFocus(); return true }
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> if (f != null && inSidebar(f)) { focusPage(); return true }
                KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_INFO -> if (f is Tile) {
                    val en = f.entry
                    if (en != null) { showMenu(en, f.row); return true }
                }
            }
        }
        return super.dispatchKeyEvent(e)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // الـ Launcher لا يُغلق. الرجوع: من المحتوى إلى الشريط، ومن الشريط إلى الرئيسية.
        val f = currentFocus
        when {
            ambientOn -> hideAmbient()
            f != null && !inSidebar(f) -> navFor(page).requestFocus()
            page != PAGE_HOME -> navItems[0].requestFocus()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // زر Home: إن كانت الواجهة ظاهرة أصلًا نعود للبداية، وإلا نحتفظ بآخر موضع.
        if (intent.hasCategory(Intent.CATEGORY_HOME) && started) {
            if (ambientOn) hideAmbient()
            navItems[0].requestFocus()
            home.scrollTo(0, 0)
            lastFocus[PAGE_HOME] = null
            focusPage()
        }
    }

    // --------------------------------------------------------------- Lifecycle

    override fun onStart() {
        super.onStart()
        val f = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED); addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED); addAction(Intent.ACTION_PACKAGE_CHANGED)
            addDataScheme("package")
        }
        registerReceiver(pkgReceiver, f)
        refreshApps()
        if (homeDirty) rebuildHome()
        started = true
    }

    override fun onStop() {
        started = false
        try { unregisterReceiver(pkgReceiver) } catch (_: Exception) {}
        super.onStop()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_UI_HIDDEN) {
            icons.evictAll()
            if (page != PAGE_APPS) { appsPage.clear(); appsDirty = true }
        }
    }

    override fun onDestroy() {
        icons.shutdown(); repo.shutdown()
        super.onDestroy()
    }

    // ------------------------------------------------------------------- Data

    private fun refreshApps() {
        repo.refreshAsync { changed ->
            if (changed) {
                rebuildHome()
                if (page == PAGE_APPS) { appsPage.rebuild(); appsDirty = false } else appsDirty = true
            }
        }
    }

    fun rebuildHome(focusKey: String? = null) {
        val prev = focusKey ?: (currentFocus as? Tile)?.focusKey
        home.rebuild()
        homeDirty = false
        val t = home.findTile(prev)
        if (t != null && page == PAGE_HOME) t.requestFocus()
        else if (currentFocus == null && page == PAGE_HOME) home.firstTile()?.requestFocus()
    }

    fun favoriteEntries(): List<AppEntry> {
        if (!prefs.favInit && repo.apps.isNotEmpty()) {
            prefs.favorites = defaultFavorites(); prefs.favInit = true
        }
        return prefs.favorites.mapNotNull { repo.find(it) }
    }

    private fun defaultFavorites(): List<String> {
        val out = ArrayList<String>()
        for (pkg in KNOWN) repo.apps.firstOrNull { it.pkg == pkg }?.let { out.add(it.key) }
        for (e in repo.apps) { if (out.size >= 8) break; if (e.key !in out) out.add(e.key) }
        return out
    }

    fun recentEntries(): List<AppEntry> = prefs.recents.mapNotNull { repo.find(it) }.take(8)

    fun inputs(): List<InputEntry> = try {
        val tvm = getSystemService(Context.TV_INPUT_SERVICE) as? TvInputManager
        tvm?.tvInputList.orEmpty().filter { it.isPassthroughInput }.map {
            InputEntry(it.id, (it.loadCustomLabel(this) ?: it.loadLabel(this)).toString())
        }
    } catch (e: Exception) { emptyList() }

    fun openInput(i: InputEntry) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, TvContract.buildChannelUriForPassthroughInput(i.id))
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) {
            Toast.makeText(this, R.string.toast_cant_open_input, Toast.LENGTH_SHORT).show()
        }
    }

    fun makeTile(e: AppEntry, row: String, sizeDp: Int): Tile = Tile(this, sizeDp, prefs.anim).also { t ->
        t.entry = e; t.row = row; t.focusKey = "$row:${e.key}"
        t.setLabel(e.label)
        icons.bind(t.iconView, e)
        t.setOnClickListener { launch(e) }
        t.setOnLongClickListener { showMenu(e, row); true }
    }

    fun launch(e: AppEntry) {
        val i = Intent(Intent.ACTION_MAIN).setComponent(e.component)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        try {
            startActivity(i)
            prefs.pushRecent(e.key)
            homeDirty = true
        } catch (ex: Exception) {
            Toast.makeText(this, R.string.toast_cant_open, Toast.LENGTH_SHORT).show()
        }
    }

    fun openDefaultHomeSettings() {
        try { startActivity(Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        catch (e: Exception) {
            try { startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) } catch (_: Exception) {}
            Toast.makeText(this, R.string.toast_default_home_hint, Toast.LENGTH_LONG).show()
        }
    }

    // ------------------------------------------------------------ App menu

    private fun moveFav(key: String, d: Int) {
        val l = prefs.favorites.toMutableList()
        val i = l.indexOf(key)
        if (i < 0) return
        var j = i + d
        while (j in l.indices && repo.find(l[j]) == null) j += d
        if (j !in l.indices) return
        Collections.swap(l, i, j)
        prefs.favorites = l
        rebuildHome("fav:$key")
    }

    fun showMenu(e: AppEntry, row: String) {
        val isFav = e.key in prefs.favorites
        val items = ArrayList<Pair<String, () -> Unit>>()
        items.add(getString(R.string.menu_open) to { launch(e) })
        if (isFav) {
            items.add(getString(R.string.menu_remove_fav) to { prefs.favorites = prefs.favorites - e.key; rebuildHome() })
            if (row == "fav") {
                items.add(getString(R.string.menu_move_left) to { moveFav(e.key, -1) })
                items.add(getString(R.string.menu_move_right) to { moveFav(e.key, 1) })
            }
        } else {
            items.add(getString(R.string.menu_add_fav) to { prefs.favorites = prefs.favorites + e.key; rebuildHome() })
        }
        items.add(getString(R.string.menu_app_info) to {
            try {
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${e.pkg}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (ex: Exception) { toastUnavailable() }
        })
        items.add(getString(R.string.menu_uninstall) to {
            try {
                startActivity(Intent(Intent.ACTION_DELETE, Uri.parse("package:${e.pkg}"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (ex: Exception) { toastUnavailable() }
        })

        val dialog = Dialog(this, R.style.HubDialog)
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            background = GradientDrawable().apply { cornerRadius = dp(20f).toFloat(); setColor(0xF2151A26.toInt()) }
        }
        box.addView(TextView(this).apply {
            text = e.label; setUiSize(18f); setTextColor(Color.WHITE)
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            setPadding(dp(12), dp(4), dp(12), dp(10))
        })
        for ((label, action) in items) {
            box.addView(TextView(this).apply {
                text = label; setUiSize(16f); setTextColor(Color.WHITE)
                setPadding(dp(12), dp(12), dp(12), dp(12))
                isFocusable = true; isClickable = true
                setOnClickListener { dialog.dismiss(); action() }
                setOnFocusChangeListener { v, f ->
                    v.background = if (f) GradientDrawable().apply {
                        cornerRadius = dp(12f).toFloat(); setColor(0x40FFFFFF)
                    } else null
                }
            })
        }
        dialog.setContentView(box)
        dialog.window?.setLayout(dp(340), ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.show()
        box.getChildAt(1)?.requestFocus()
    }

    private fun toastUnavailable() = Toast.makeText(this, R.string.toast_unavailable, Toast.LENGTH_SHORT).show()

    companion object {
        const val PAGE_HOME = 0
        const val PAGE_APPS = 1
        const val PAGE_SETTINGS = 2
        private val KNOWN = listOf(
            "com.google.android.youtube.tv", "com.netflix.ninja", "com.amazon.amazonvideo.livingroom",
            "com.disney.disneyplus", "com.google.android.youtube.tvmusic", "com.spotify.tv.android"
        )
    }
}
