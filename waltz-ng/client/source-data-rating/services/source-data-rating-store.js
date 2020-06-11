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

let loaderPromise = null;

export function store($http, baseUrl) {

    const BASE = `${baseUrl}/source-data-rating`;

    const findAll = (force = false) => {
        if (loaderPromise && ! force) return loaderPromise;

        loaderPromise = $http
            .get(`${BASE}`)
            .then(result => result.data);

        return loaderPromise;
    };

    return {
        findAll
    };
}

store.$inject = ['$http', 'BaseApiUrl'];


export const serviceName = 'SourceDataRatingStore';


export const SourceDataRatingStore_API = {
    findAll: {
        serviceName,
        serviceFnName: 'findAll',
        description: 'retrieve all source data ratings'
    },
};