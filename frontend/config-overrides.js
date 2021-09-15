const { paths } = require('react-app-rewired');
const path = require('path');

function ignoredFiles(appSrc) {
    return new RegExp(
        `^(?!${escape(
            path.normalize(appSrc + '/').replace(/[\\]+/g, '/')
        )}).+/node_modules/((?!@quarkusio).*)`,
        'g'
    );
}

module.exports = {
    //do stuff with the webpack config...
    devServer: function (configFunction) {
        return function (proxy, allowedHost) {
            const devServerConfig = configFunction(proxy, allowedHost);
            devServerConfig.watchOptions = {
                ignored: ignoredFiles(paths.appSrc),
            }
            return devServerConfig;
        };
    }
}