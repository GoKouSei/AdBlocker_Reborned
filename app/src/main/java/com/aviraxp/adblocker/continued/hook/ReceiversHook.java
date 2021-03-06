package com.aviraxp.adblocker.continued.hook;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XModuleResources;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;
import com.aviraxp.adblocker.continued.util.LogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class ReceiversHook implements IXposedHookLoadPackage, IXposedHookZygoteInit {

    private Set<String> receiversList;

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!PreferencesHelper.isReceiversHookEnabled() || lpparam.packageName.equals("android")) {
            return;
        }

        ArrayList<String> arrayReceivers = new ArrayList<>();
        Object activityThread = XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread");
        Context systemContext = (Context) XposedHelpers.callMethod(activityThread, "getSystemContext");
        ActivityInfo[] receiverInfo = systemContext.getPackageManager().getPackageInfo(systemContext.getPackageName(), PackageManager.GET_RECEIVERS).receivers;

        if (receiverInfo != null) {
            for (ActivityInfo info : receiverInfo) {
                arrayReceivers.add(info.name);
            }
        }

        for (String checkReceiver : receiversList) {
            if (arrayReceivers.contains(checkReceiver)) {
                XposedHelpers.findAndHookMethod(checkReceiver, lpparam.classLoader, "onReceive", Context.class, Intent.class, XC_MethodReplacement.DO_NOTHING);
                LogUtils.logRecord("Receiver Block Success: " + lpparam.packageName + "/" + checkReceiver, true);
            }
        }
    }

    public void initZygote(StartupParam startupParam) throws Throwable {
        String MODULE_PATH = startupParam.modulePath;
        Resources res = XModuleResources.createInstance(MODULE_PATH, null);
        byte[] array = XposedHelpers.assetAsByteArray(res, "blocklist/receivers");
        String decoded = new String(array, "UTF-8");
        String[] sUrls = decoded.split("\n");
        receiversList = new HashSet<>();
        Collections.addAll(receiversList, sUrls);
    }
}