/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

function uiSelectSetup(uiSelectConfig) {
    uiSelectConfig.theme = 'bootstrap';
    uiSelectConfig.resetSearchInput = true;
    uiSelectConfig.appendToBody = true;
}

uiSelectSetup.$inject = ['uiSelectConfig'];


function authProviderSetup($authProvider, BaseUrl) {
    $authProvider.baseUrl = BaseUrl;
    $authProvider.withCredentials = false;

    $authProvider.google({
        clientId: 'Google account'
    });

    $authProvider.github({
        clientId: 'GitHub Client ID'
    });

    $authProvider.linkedin({
        clientId: 'LinkedIn Client ID'
    });
}

authProviderSetup.$inject = [
    '$authProvider',
    'BaseUrl',
];


function showdownSetup($showdownProvider) {
    const customExtensions = () => {
        const tableStyle = {
            type: 'output',
            regex: /<table>/g,
            replace: '<table class="table table-condensed table-striped table-bordered">'
        };

        return [tableStyle];
    };

    $showdownProvider.loadExtension(customExtensions);

    $showdownProvider.setOption('excludeTrailingPunctuationFromURLs', true);
    $showdownProvider.setOption('sanitize', true);
    $showdownProvider.setOption('simplifiedAutoLink', true);
    $showdownProvider.setOption('simpleLineBreaks', true);
    $showdownProvider.setOption('strikethrough', true);
    $showdownProvider.setOption('tables', true);
    $showdownProvider.setOption('tasklists', true);
}

showdownSetup.$inject  = [
    '$showdownProvider'
];


function configureNotification(notificationProvider) {
    notificationProvider.setOptions({
        positionX: 'right',
        positionY: 'bottom'
    });
}

configureNotification.$inject = [
    'NotificationProvider'
];


function setup(module) {
    module
        .config(uiSelectSetup)
        .config(authProviderSetup)
        .config(showdownSetup)
        .config(configureNotification);

    // for formly setup see: `formly/index.js`
}


export default (module) => setup(module);