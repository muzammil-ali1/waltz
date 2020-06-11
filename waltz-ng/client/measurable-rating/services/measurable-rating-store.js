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
import {checkIsEntityRef, checkIsIdSelector} from "../../common/checks";


function store($http, baseApiUrl) {

    const baseUrl = `${baseApiUrl}/measurable-rating`;

    const findForEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${baseUrl}/entity/${ref.kind}/${ref.id}`)
            .then(d => d.data);
    };

    const findByCategory = (id) => {
        return $http
            .get(`${baseUrl}/category/${id}`)
            .then(d => d.data);
    };

    const findByMeasurableSelector = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${baseUrl}/measurable-selector`, options)
            .then(d => d.data);
    };

    const findByAppSelector = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${baseUrl}/app-selector`, options)
            .then(d => d.data);
    };

    const statsByAppSelector = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${baseUrl}/stats-by/app-selector`, options)
            .then(d => d.data);
    };

    const statsForRelatedMeasurables = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${baseUrl}/related-stats/measurable`, options)
            .then(d => d.data);
    };

    const countByMeasurableCategory = (id) => {
        return $http
            .get(`${baseUrl}/count-by/measurable/category/${id}`)
            .then(d => d.data);
    };

    const save = (ref, measurableId, rating = "Z", description = "") => {
        checkIsEntityRef(ref);
        return $http
            .post(`${baseUrl}/entity/${ref.kind}/${ref.id}/measurable/${measurableId}`, { rating, description })
            .then(d => d.data);
    };

    const remove = (ref, measurableId) => {
        checkIsEntityRef(ref);
        return $http
            .delete(`${baseUrl}/entity/${ref.kind}/${ref.id}/measurable/${measurableId}`)
            .then(d => d.data);
    };

    const removeByCategory = (ref, categoryId) => {
        checkIsEntityRef(ref);
        return $http
            .delete(`${baseUrl}/entity/${ref.kind}/${ref.id}/category/${categoryId}`)
            .then(d => d.data);
    };

    return {
        findByMeasurableSelector,
        findByAppSelector,
        findByCategory,
        findForEntityReference,
        countByMeasurableCategory,
        statsByAppSelector,
        statsForRelatedMeasurables,
        save,
        remove,
        removeByCategory
    };

}

store.$inject = ["$http", "BaseApiUrl"];


const serviceName = "MeasurableRatingStore";


export const MeasurableRatingStore_API = {
    findByMeasurableSelector: {
        serviceName,
        serviceFnName: "findByMeasurableSelector",
        description: "finds measurables by measurable selector"
    },
    findByAppSelector: {
        serviceName,
        serviceFnName: "findByAppSelector",
        description: "finds measurables by app selector"
    },
    findByCategory: {
        serviceName,
        serviceFnName: "findByCategory",
        description: "finds measurable ratings for a given category id"
    },
    findForEntityReference: {
        serviceName,
        serviceFnName: "findForEntityReference",
        description: "find measurables for an entity reference"
    },
    countByMeasurableCategory: {
        serviceName,
        serviceFnName: "countByMeasurableCategory",
        description: "return a count by measurable category [categoryId]"
    },
    statsByAppSelector: {
        serviceName,
        serviceFnName: "statsByAppSelector",
        description: "return measurable stats by app selector"
    },
    statsForRelatedMeasurables: {
        serviceName,
        serviceFnName: "statsForRelatedMeasurables",
        description: "return stats for related measurables"
    },
    save: {
        serviceName,
        serviceFnName: "save",
        description: "saves a measurable rating (either creating it or updating it as appropriate)"
    },
    remove: {
        serviceName,
        serviceFnName: "remove",
        description: "remove a measurable rating"
    },
    removeByCategory: {
        serviceName,
        serviceFnName: "removeByCategory",
        description: "remove all measurable ratings for an entity in a given category [entityRef, categoryId]"
    }
};


export default {
    serviceName,
    store
}
