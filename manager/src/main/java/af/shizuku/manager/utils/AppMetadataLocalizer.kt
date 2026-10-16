package af.shizuku.manager.utils

import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import af.shizuku.manager.R
import af.shizuku.manager.database.AppContextManager
import java.util.Locale

/** Localizes built-in catalog text without changing package IDs, enhancement keys, or remote data. */
object AppMetadataLocalizer {
    private val textIds = intArrayOf(
        R.string.metadata_enh_shell_interceptor_title,
        R.string.metadata_enh_shell_interceptor_description,
        R.string.metadata_enh_storage_proxy_title,
        R.string.metadata_enh_storage_proxy_description,
        R.string.metadata_enh_dpm_plus_title,
        R.string.metadata_enh_dpm_plus_description,
        R.string.metadata_enh_npu_plus_title,
        R.string.metadata_enh_npu_plus_description,
        R.string.metadata_enh_vm_plus_title,
        R.string.metadata_enh_vm_plus_description,
        R.string.metadata_enh_win_plus_title,
        R.string.metadata_enh_win_plus_description,
        R.string.metadata_enh_overlay_manager_plus_title,
        R.string.metadata_enh_overlay_manager_plus_description,
        R.string.metadata_enh_network_governor_plus_title,
        R.string.metadata_enh_network_governor_plus_description,
        R.string.metadata_app_com_topjohnwu_magisk,
        R.string.metadata_app_eu_chainfire_supersu,
        R.string.metadata_app_org_lsposed_manager,
        R.string.metadata_app_com_vipercn_viper4android_v2,
        R.string.metadata_app_org_adaway,
        R.string.metadata_app_dev_ukanth_ufirewall,
        R.string.metadata_nav_22,
        R.string.metadata_app_com_ramdaas_ramexe,
        R.string.metadata_app_me_piebridge_prevent,
        R.string.metadata_app_com_samsung_android_hexinstall,
        R.string.metadata_app_com_samsung_android_themepark,
        R.string.metadata_app_com_keramidas_titaniumbackup,
        R.string.metadata_nav_28,
        R.string.metadata_app_eu_darken_sdm,
        R.string.metadata_nav_30,
        R.string.metadata_app_com_speedsoftware_rootexplorer,
        R.string.metadata_nav_32,
        R.string.metadata_app_com_speedsoftware_explorer,
        R.string.metadata_app_com_jrummy_root_browserfree,
        R.string.metadata_nav_35,
        R.string.metadata_app_com_estrongs_android_pop,
        R.string.metadata_nav_37,
        R.string.metadata_app_com_github_machiav3lli_backup,
        R.string.metadata_nav_39,
        R.string.metadata_app_com_jrummy_apps_build_prop_editor,
        R.string.metadata_app_com_machiav3lli_neo_backup,
        R.string.metadata_app_projekt_substratum_lite,
        R.string.metadata_app_com_oasisfeng_greenify,
        R.string.metadata_app_com_franco_doze,
        R.string.metadata_app_com_uzumapps_wakelockdetector,
        R.string.metadata_app_com_asksven_betterbatterystats,
        R.string.metadata_app_org_swiftapps_swiftbackup,
        R.string.metadata_nav_48,
        R.string.metadata_app_thejaustin_hexodus,
        R.string.metadata_app_thejaustin_afdroid,
        R.string.metadata_app_thejaustin_pearity,
        R.string.metadata_app_thejaustin_termux_ai,
        R.string.metadata_app_thejaustin_appmanager,
        R.string.metadata_app_thejaustin_simweather,
        R.string.metadata_app_thejaustin_contactsplus,
        R.string.metadata_app_thejaustin_obtainiumplus,
        R.string.metadata_app_thejaustin_snapback,
        R.string.metadata_app_com_aistra_hail,
        R.string.metadata_app_samolego_canta,
        R.string.metadata_app_rikka_appops,
        R.string.metadata_app_com_catchingnow_icebox,
        R.string.metadata_app_com_zacharee_installwithoptions,
        R.string.metadata_app_cf_playhi_freezeyou,
        R.string.metadata_app_com_oasisfeng_island,
        R.string.metadata_app_com_oasisfeng_island_fdroid,
        R.string.metadata_app_bin_mt_plus,
        R.string.metadata_app_pl_solidexplorer2,
        R.string.metadata_nav_68,
        R.string.metadata_app_com_ghisler_android_totalcommander,
        R.string.metadata_nav_70,
        R.string.metadata_app_com_lonelycatgames_xplore,
        R.string.metadata_app_ru_zdevs_zarchiver,
        R.string.metadata_app_com_alphainventor_filemanager,
        R.string.metadata_app_net_dinglisch_android_taskerm,
        R.string.metadata_app_com_arlosoft_macrodroid,
        R.string.metadata_app_henrichg_phoneprofilesplus,
        R.string.metadata_app_eu_toneiv_ubktouch,
        R.string.metadata_app_com_kieronquinn_ambientmusicmod,
        R.string.metadata_app_com_kieronquinn_darq,
        R.string.metadata_app_com_zacharee_tweaker,
        R.string.metadata_app_dev_lexip_hecate,
        R.string.metadata_app_mahmud0808_colorblendr,
        R.string.metadata_app_com_kieronquinn_smartspacer,
        R.string.metadata_app_com_ysy_app_firewall,
        R.string.metadata_app_com_deltazefiro_amarokhider,
        R.string.metadata_app_ahmetcanarslan_shizuwall,
        R.string.metadata_app_tk_zwander_wifilist,
        R.string.metadata_app_p_shashank_ashellyou,
        R.string.metadata_app_rohitkushvaha01_reterminal,
        R.string.metadata_app_com_imranr98_obtainium,
        R.string.metadata_app_com_aurora_store,
        R.string.metadata_app_com_looker_droidify,
        R.string.metadata_app_eu_darken_sdmse,
        R.string.metadata_app_com_paget96_chargemonitor,
        R.string.metadata_app_com_mihonapp_mihon,
        R.string.metadata_category_96,
        R.string.metadata_category_97,
        R.string.metadata_category_98,
        R.string.metadata_category_99,
        R.string.metadata_category_100,
        R.string.metadata_category_101
    )

    @Volatile private var sourceIds: Map<String, Int>? = null

    fun text(context: Context, source: String): String {
        val ids = sourceIds ?: run {
            val config = Configuration(context.resources.configuration).apply {
                setLocales(LocaleList(Locale.ENGLISH))
            }
            val english = context.createConfigurationContext(config).resources
            textIds.associateBy { english.getString(it) }.also { sourceIds = it }
        }
        return ids[source]?.let { context.getString(it) } ?: source
    }

    fun metadata(context: Context, source: AppContextManager.AppMetadata?): AppContextManager.AppMetadata? =
        source?.copy(
            description = text(context, source.description),
            suPathSettingNav = source.suPathSettingNav?.let { text(context, it) },
            potentialEnhancements = source.potentialEnhancements.map { item ->
                item.copy(title = text(context, item.title), description = text(context, item.description))
            }
        )
}
