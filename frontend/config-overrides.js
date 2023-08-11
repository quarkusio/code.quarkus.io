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
    watchOptions: function (config) {
        config.ignored = ignoredFiles(paths.appSrc);
        return config;
    }
}