package com.aviraxp.adblocker.continued.hook;

import com.aviraxp.adblocker.continued.helper.PreferencesHelper;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class BackPressHook implements IXposedHookLoadPackage {

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!PreferencesHelper.isBackPressHookEnabled()) {
            return;
        }

        XC_MethodHook backPressHook = new XC_MethodHook() {
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                param.args[0] = true;
            }
        };

        XposedBridge.hookAllMethods(android.app.Dialog.class, "setCancelable", backPressHook);
        XposedBridge.hookAllMethods(android.app.AlertDialog.Builder.class, "setCancelable", backPressHook);
        XposedBridge.hookAllMethods(android.app.ProgressDialog.class, "setCancelable", backPressHook);
        XposedBridge.hookAllMethods(android.app.Activity.class, "setFinishOnTouchOutside", backPressHook);
    }
}