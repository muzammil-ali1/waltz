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

import angular from 'angular';
import AppGroupOverview from './components/overview/app-group-overview'
import RelatedAppGroupsSection from './components/related-app-groups-section/related-app-groups-section';
import SubscriptionButtons from './components/subscription-buttons/subscription-buttons';
import {registerComponents, registerStores} from "../common/module-utils";
import * as AppGroupStore from './services/app-group-store';
import Routes from './routes';
import AppGroupList from './components/app-group-list/app-group-list';
import AppGroupListSection from './components/app-group-list-section/app-group-list-section';
import AppGroupAppSelectionList from './directives/app-group-app-selection-list';
import FavouritesStore from "./services/favourites-store";
import FavouritesPanel from "./components/favourites-panel/favourites-panel";


export default () => {

    const module = angular.module('waltz.app.group', []);
    module
        .config(Routes);

    module
        .directive('waltzAppGroupAppSelectionList', AppGroupAppSelectionList);

    registerComponents(module, [
        AppGroupList,
        AppGroupListSection,
        AppGroupOverview,
        SubscriptionButtons,
        RelatedAppGroupsSection,
        FavouritesPanel ]);

    registerStores(module, [ AppGroupStore, FavouritesStore ]);

    return module.name;

}
