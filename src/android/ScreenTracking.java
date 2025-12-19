package com.nguyenhung.screentracking;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import java.util.Calendar;
import java.util.List;

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
            AppOpsManager appOpsManager = (AppOpsManager) cordova.getActivity().getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), cordova.getActivity().getPackageName());
            hasPermission = mode == AppOpsManager.MODE_ALLOWED;
        }
        
        callbackContext.success(hasPermission ? 1 : 0);
    }

    private void requestUsageStatsPermission(CallbackContext callbackContext) {
        try {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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

        // Check permission first
        AppOpsManager appOpsManager = (AppOpsManager) cordova.getActivity().getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), cordova.getActivity().getPackageName());
        
        if (mode != AppOpsManager.MODE_ALLOWED) {
            callbackContext.error("Usage stats permission not granted");
            return;
        }

        try {
            UsageStatsManager usageStatsManager = (UsageStatsManager) cordova.getActivity().getSystemService(Context.USAGE_STATS_SERVICE);
            
            // Set time range (last 24 hours)
            Calendar calendar = Calendar.getInstance();
            long endTime = calendar.getTimeInMillis();
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            long startTime = calendar.getTimeInMillis();

            // Get usage stats
            int intervalType = UsageStatsManager.INTERVAL_DAILY;
            if ("INTERVAL_WEEKLY".equals(timeInterval)) {
                intervalType = UsageStatsManager.INTERVAL_WEEKLY;
            } else if ("INTERVAL_MONTHLY".equals(timeInterval)) {
                intervalType = UsageStatsManager.INTERVAL_MONTHLY;
            }

            List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(intervalType, startTime, endTime);
            
            JSONArray result = new JSONArray();
            PackageManager packageManager = cordova.getActivity().getPackageManager();
            
            for (UsageStats usageStats : usageStatsList) {
                if (usageStats.getTotalTimeInForeground() > 0) {
                    JSONObject appInfo = new JSONObject();
                    appInfo.put("packageName", usageStats.getPackageName());
                    appInfo.put("totalTimeInForeground", usageStats.getTotalTimeInForeground());
                    appInfo.put("firstTimeStamp", usageStats.getFirstTimeStamp());
                    appInfo.put("lastTimeStamp", usageStats.getLastTimeStamp());
                    
                    // Get app name
                    try {
                        ApplicationInfo applicationInfo = packageManager.getApplicationInfo(usageStats.getPackageName(), 0);
                        String appName = packageManager.getApplicationLabel(applicationInfo).toString();
                        appInfo.put("appName", appName);
                    } catch (PackageManager.NameNotFoundException e) {
                        appInfo.put("appName", usageStats.getPackageName());
                    }
                    
                    result.put(appInfo);
                }
            }
            
            callbackContext.success(result);
            
        } catch (Exception e) {
            callbackContext.error("Failed to get usage stats: " + e.getMessage());
        }
    }
}
