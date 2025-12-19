var exec = require('cordova/exec');

exports.coolMethod = function (success, error) {
    exec(success, error, 'ScreenTracking', 'coolMethod', ['hello i am running']);
};

// Check if usage stats permission is granted
exports.hasUsageStatsPermission = function (success, error) {
    exec(success, error, 'ScreenTracking', 'hasUsageStatsPermission', []);
};

// Request usage stats permission (opens Settings)
exports.requestUsageStatsPermission = function (success, error) {
    exec(success, error, 'ScreenTracking', 'requestUsageStatsPermission', []);
};

// Get app usage statistics
exports.getUsageStats = function (success, error, timeInterval) {
    timeInterval = timeInterval || 'INTERVAL_DAILY';
    exec(success, error, 'ScreenTracking', 'getUsageStats', [timeInterval]);
};

