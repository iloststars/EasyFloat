package com.lzf.easyfloat.foo

import android.view.WindowManager
import java.lang.Exception
import android.annotation.SuppressLint
import java.lang.Class
import java.lang.ClassNotFoundException
import java.lang.IllegalAccessException
import java.lang.NoSuchFieldException
import android.os.Build
import android.text.TextUtils
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.IOException

object F {
    const val KEY_VERSION_BLACKSHARK = "ro.blackshark.rom"
    const val KEY_VERSION_EMUI = "ro.build.version.emui"
    const val KEY_VERSION_MIUI = "ro.miui.ui.version.name"
    const val KEY_VERSION_NUBIA = "ro.build.nubia.rom.name"
    const val KEY_VERSION_ONEPLIS = "ro.build.ota.versionname"
    const val KEY_VERSION_OPPO = "ro.build.version.opporom"
    const val KEY_VERSION_ROG = "ro.build.fota.version"
    const val KEY_VERSION_SAMSUNG = "ro.channel.officehubrow"
    const val KEY_VERSION_SMARTISAN = "ro.smartisan.version"
    const val KEY_VERSION_VIVO = "ro.vivo.os.version"
    const val ROM_BLACKSHARK = "JOYUI"
    const val ROM_EMUI = "EMUI"
    const val ROM_FLYME = "FLYME"
    const val ROM_MIUI = "MIUI"
    const val ROM_NUBIAUI = "NUBIAUI"
    const val ROM_ONEPLUS = "HYDROGEN"
    const val ROM_OPPO = "OPPO"
    const val ROM_QIKU = "QIKU"
    const val ROM_ROG = "REPLIBLIC"
    const val ROM_SAMSUNG = "ONEUI"
    const val ROM_SMARTISAN = "SMARTISAN"
    const val ROM_VIVO = "VIVO"
    private var recordEnable = false
    private var sName: String? = null
    private var sVersion: String? = null

    //华为
    val isEmui: Boolean
        get() = check(ROM_EMUI)

    //小米
    val isMiui: Boolean
        get() = check(ROM_MIUI)

    //vivo
    val isVivo: Boolean
        get() = check(ROM_VIVO)

    //oppo
    val isOppo: Boolean
        get() = check(ROM_OPPO)

    //魅族
    val isFlyme: Boolean
        get() = check(ROM_FLYME)

    //360手机
    fun is360(): Boolean {
        return check(ROM_QIKU) || check("360")
    }

    val isSmartisan: Boolean
        get() = check(ROM_SMARTISAN)
    val isSOneplus: Boolean
        get() = check(ROM_ONEPLUS)
    val isRog: Boolean
        get() = check(ROM_ROG)
    val isNubia: Boolean
        get() = check(ROM_NUBIAUI)
    val name: String?
        get() {
            if (sName == null) {
                check("")
            }
            return sName
        }
    val version: String?
        get() {
            if (sVersion == null) {
                check("")
            }
            return sVersion
        }

    private fun setXiaomiParams(layoutParams: WindowManager.LayoutParams) {
        layoutParams.flags = layoutParams.flags or WindowManager.LayoutParams.FLAG_DITHER
    }

    private fun setSamsungFlags(layoutParams: WindowManager.LayoutParams) {
        try {
            val method = layoutParams.javaClass.getMethod("semAddExtensionFlags", Integer.TYPE)
            val method2 = layoutParams.javaClass.getMethod("semAddPrivateFlags", Integer.TYPE)
            method.invoke(layoutParams, -2147352576)
            method2.invoke(layoutParams, layoutParams.flags)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setMeizuParams(layoutParams: WindowManager.LayoutParams, i: Int): Boolean {
        return try {
            @SuppressLint("PrivateApi") val cls = Class.forName("android.view.MeizuLayoutParams")
            val declaredField = cls.getDeclaredField("flags")
            declaredField.isAccessible = true
            val newInstance = cls.newInstance()
            declaredField.setInt(newInstance, i)
            layoutParams.javaClass.getField("meizuParams")[layoutParams] = newInstance
            true
        } catch (e: ClassNotFoundException) {
            false
        } catch (e: IllegalAccessException) {
            false
        } catch (e: InstantiationException) {
            false
        } catch (e: NoSuchFieldException) {
            false
        }
    }

    private fun setMeizuParams_new(layoutParams: WindowManager.LayoutParams, i: Int) {
        try {
            val declaredField = layoutParams.javaClass.getDeclaredField("meizuFlags")
            declaredField.isAccessible = true
            declaredField.setInt(layoutParams, i)
        } catch (ignored: Exception) {
        }
    }

    private fun setOnePlusParams(layoutParams: WindowManager.LayoutParams, i: Int) {
        try {
            @SuppressLint("DiscouragedPrivateApi") val declaredField =
                layoutParams.javaClass.getDeclaredField("privateFlags")
            declaredField.isAccessible = true
            declaredField[layoutParams] = i
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setRecordEnable(recordEnable:Boolean){
        this.recordEnable = recordEnable
    }

    fun getFakeRecorderWindowLayoutParams(layoutParams:WindowManager.LayoutParams): WindowManager.LayoutParams {
//        val layoutParams = WindowManager.LayoutParams()
        if (recordEnable)
            return layoutParams
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            layoutParams.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        when (name) {
            ROM_MIUI, ROM_BLACKSHARK -> setXiaomiParams(layoutParams)
            ROM_ROG -> layoutParams.memoryType = layoutParams.memoryType or 268435456
            ROM_SAMSUNG -> setSamsungFlags(layoutParams)
            ROM_ONEPLUS -> if (Build.VERSION.SDK_INT == 30) {
                try {
                    @SuppressLint("SoonBlockedPrivateApi") val declaredField =
                        layoutParams.javaClass.getDeclaredField("PRIVATE_FLAG_IS_ROUNDED_CORNERS_OVERLAY")
                    declaredField.isAccessible = true
                    setOnePlusParams(layoutParams, declaredField[layoutParams.javaClass] as Int)
                } catch (ignore: Exception) {
                }
            }
            ROM_FLYME -> if (!setMeizuParams(layoutParams, 8192)) {
                setMeizuParams_new(layoutParams, 1024)
            }
        }
        layoutParams.title = fakeRecordWindowTitle
        return layoutParams
    }

    private val fakeRecordWindowTitle: String
        get() {
            if (sName == null) {
                check("")
            }
            if (sName == null) {
                return ""
            }
            when (sName) {
                ROM_MIUI -> return "com.miui.screenrecorder"
                ROM_EMUI -> return "ScreenRecoderTimer"
                ROM_OPPO -> return "com.coloros.screenrecorder.FloatView"
                ROM_VIVO -> return "screen_record_menu"
                ROM_ONEPLUS -> return "op_screenrecord"
                ROM_FLYME -> return "SysScreenRecorder"
                ROM_NUBIAUI -> return "NubiaScreenDecorOverlay"
                ROM_BLACKSHARK -> return "com.blackshark.screenrecorder"
                ROM_ROG -> return "com.asus.force.layer.transparent.SR.floatingpanel"
            }
            return ""
        }

    private fun check(rom: String): Boolean {
        if (sName != null) {
            return sName == rom
        }
        if (!TextUtils.isEmpty(getProp(KEY_VERSION_MIUI).also { sVersion = it })) {
            sName = ROM_MIUI
        } else if (!TextUtils.isEmpty(getProp(KEY_VERSION_BLACKSHARK).also { sVersion = it })) {
            sName = ROM_BLACKSHARK
        } else if (!TextUtils.isEmpty(getProp(KEY_VERSION_EMUI).also { sVersion = it })) {
            sName = ROM_EMUI
        } else if (!TextUtils.isEmpty(getProp(KEY_VERSION_OPPO).also { sVersion = it })) {
            sName = ROM_OPPO
        } else if (!TextUtils.isEmpty(getProp(KEY_VERSION_VIVO).also { sVersion = it })) {
            sName = ROM_VIVO
        } else if (!TextUtils.isEmpty(getProp(KEY_VERSION_SMARTISAN).also { sVersion = it })) {
            sName = ROM_SMARTISAN
        } else if (!TextUtils.isEmpty(getProp(KEY_VERSION_NUBIA).also { sVersion = it })) {
            sName = ROM_NUBIAUI
        } else {
            val ota = "ro.build.ota.versionname"
            if (TextUtils.isEmpty(getProp(ota)) || !getProp(ota)!!
                    .toLowerCase().contains("hydrogen")
            ) {
                val fota = "ro.build.fota.version"
                if (!TextUtils.isEmpty(getProp(fota)) && getProp(fota)!!
                        .toLowerCase().contains("CN_Phone")
                ) {
                    sName = ROM_ROG
                } else if (!TextUtils.isEmpty(getProp("ro.channel.officehubrow"))) {
                    sName = ROM_SAMSUNG
                } else {
                    sVersion = Build.DISPLAY
                    if (sVersion!!.uppercase().contains(ROM_FLYME)) {
                        sName = ROM_FLYME
                    } else {
                        sVersion = Build.UNKNOWN
                        sName = Build.MANUFACTURER.toUpperCase()
                    }
                }
            } else {
                sName = ROM_ONEPLUS
            }
        }
        return sName == rom
    }

    private fun getProp(name: String): String? {
        var line = ""
        var input: BufferedReader? = null
        try {
            val p = Runtime.getRuntime().exec("getprop $name")
            input = BufferedReader(InputStreamReader(p.inputStream), 1024)
            line = input.readLine()
            input.close()
        } catch (ex: IOException) {
            return null
        } finally {
            if (input != null) {
                try {
                    input.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return line
    }
}