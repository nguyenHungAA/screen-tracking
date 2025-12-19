var exec = require('cordova/exec');
var permissions = cordova.plugins.permissions;

exports.coolMethod = function (success, error) {
    exec(success, error, 'ScreenTracking', 'coolMethod', ['hello i am running']);
};

permissions.requestPermission(permissions.CAMERA, success, error);

function error() {
    console.warn('Camera permission is not turned on');
}

function success(status) {
    if (!status.hasPermission) error();
}
