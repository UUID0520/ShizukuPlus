package af.shizuku.manager.utils

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.os.Bundle
import af.shizuku.manager.ShizukuSettings
import timber.log.Timber

/** Keeps the app's task visibility in sync with the saved setting, including after a cold start. */
class RecentTasksController(private val application: Application) :
    Application.ActivityLifecycleCallbacks,
    SharedPreferences.OnSharedPreferenceChangeListener {

    init {
        // Lifecycle callbacks retain this controller; SharedPreferences holds its listener weakly.
        application.registerActivityLifecycleCallbacks(this)
        ShizukuSettings.getPreferences().registerOnSharedPreferenceChangeListener(this)
        applyVisibility()
    }

    private fun applyVisibility() {
        val activityManager = application.getSystemService(ActivityManager::class.java) ?: return
        val tasks = try {
            activityManager.appTasks
        } catch (e: Exception) {
            Timber.w(e, "Unable to read app tasks")
            return
        }
        val hidden = ShizukuSettings.isHideFromRecentsEnabled()
        tasks.forEach { task ->
            try {
                val info = task.taskInfo
                val component = info.baseActivity ?: info.baseIntent.component ?: return@forEach
                if (component.packageName != application.packageName) return@forEach

                val activityInfo = application.packageManager.getActivityInfo(component, 0)
                // Permission/pairing dialogs are always excluded by their manifest, even when
                // the user turns this setting off for the main app task.
                val alwaysHidden = activityInfo.flags and ActivityInfo.FLAG_EXCLUDE_FROM_RECENTS != 0
                task.setExcludeFromRecents(hidden || alwaysHidden)
            } catch (e: Exception) {
                // A task can disappear between enumeration and update; keep processing the rest.
                Timber.w(e, "Unable to update app task visibility")
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {
        if (key == null || key == ShizukuSettings.Keys.KEY_HIDE_FROM_RECENTS) applyVisibility()
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = applyVisibility()

    override fun onActivityStarted(activity: Activity) = applyVisibility()

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}
}
