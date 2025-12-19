package com.nguyenhung.screentracking;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

/**
 * This class echoes a string called from JavaScript.
 */
public class ScreenTracking extends CordovaPlugin {

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("coolMethod")) {
            String message = args.getString(0);
            this.coolMethod(message, callbackContext);
            return true;
        } else if (action.equals("hasUsageStatsPermission")) {
            this.hasUsageStatsPermission(callbackContext);
            return true;
        } else if (action.equals("requestUsageStatsPermission")) {
            this.requestUsageStatsPermission(callbackContext);
            return true;
        } else if (action.equals("getUsageStats")) {
            String timeInterval = args.getString(0);
            this.getUsageStats(timeInterval, callbackContext);
            return true;
        }
        return false;
    }

    private void coolMethod(String message, CallbackContext callbackContext) {
        if (message != null && message.length() > 0) {
            callbackContext.success(message);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    private void hasUsageStatsPermission(CallbackContext callbackContext) {
        boolean hasPermission = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                AppOpsManager appOpsManager = (AppOpsManager) cordova.getActivity()
                        .getSystemService(Context.APP_OPS_SERVICE);
                if (appOpsManager != null) {
                    int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                            android.os.Process.myUid(), cordova.getActivity().getPackageName());
                    hasPermission = mode == AppOpsManager.MODE_ALLOWED;
                }
            } catch (Exception e) {
                callbackContext.error("Error checking permission: " + e.getMessage());
                return;
            }
        }

        callbackContext.success(hasPermission ? 1 : 0);
    }

    private void requestUsageStatsPermission(CallbackContext callbackContext) {
        try {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            cordova.getActivity().startActivity(intent);
            callbackContext.success("Permission request opened");
        } catch (Exception e) {
            callbackContext.error("Failed to open usage access settings: " + e.getMessage());
        }
    }

    private void getUsageStats(String timeInterval, CallbackContext callbackContext) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            callbackContext.error("Usage stats require Android 5.0 or higher");
            return;
        }

        // For now, just return a placeholder
        callbackContext.success("Usage stats functionality placeholder - permission check first");
    }
}