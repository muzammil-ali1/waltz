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
import _ from 'lodash';
import {CORE_API} from "../../common/services/core-api-utils";


function service($q, serviceBroker) {

    const findBySelector = (id, kind, scope = 'CHILDREN') => {
        const options = _.isObject(id)
            ? id
            : {scope, entityReference: {id, kind}};

        const promises = [
            serviceBroker.loadViewData(CORE_API.ServerInfoStore.findStatsForSelector, [options]).then(r => r.data),
            serviceBroker.loadViewData(CORE_API.DatabaseStore.findStatsForSelector, [options]).then(r => r.data),
            serviceBroker.loadViewData(CORE_API.SoftwareCatalogStore.findStatsForSelector, [options]).then(r => r.data)
        ];

        return $q
            .all(promises)
            .then(([
                serverStats,
                databaseStats,
                softwareStats
            ]) => ({
                serverStats,
                databaseStats,
                softwareStats
            }));
    };


    return {
        findBySelector
    };
}


service.$inject = [
    "$q",
    "ServiceBroker"
];


const serviceName = 'TechnologyStatisticsService';


export default {
    service,
    serviceName
};


export const TechnologyStatisticsService_API = {
    findBySelector: {
        serviceName,
        serviceFnName: 'findBySelector',
        description: 'find stats for the given app selector'
    },
};