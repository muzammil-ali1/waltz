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

import {checkIsCreatePhysicalFlowCommand, checkIsEntityRef, checkIsIdSelector} from "../../common/checks";


export function store($http, baseApiUrl) {

    const base = `${baseApiUrl}/physical-flow`;


    const findByEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${base}/entity/${ref.kind}/${ref.id}`)
            .then(r => r.data);
    };


    const findByProducerEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${base}/entity/${ref.kind}/${ref.id}/produces`)
            .then(r => r.data);
    };


    const findByConsumerEntityReference = (ref) => {
        checkIsEntityRef(ref);
        return $http
            .get(`${base}/entity/${ref.kind}/${ref.id}/consumes`)
            .then(r => r.data);
    };


    const findBySpecificationId = (id) => {
        return $http
            .get(`${base}/specification/${id}`)
            .then(r => r.data);
    };


    const findByExternalId = (extId) => {
        return $http
            .get(`${base}/external-id/${extId}`)
            .then(r => r.data);
    };


    const getById = (id) => {
        return $http
            .get(`${base}/id/${id}`)
            .then(r => r.data);
    };


    const findBySelector = (options) => {
        checkIsIdSelector(options);
        return $http
            .post(`${base}/selector`, options)
            .then(r => r.data);
    };

    const merge = (fromId, toId) => {
        return $http
            .post(`${base}/merge/from/${fromId}/to/${toId}`)
            .then(r => r.data);
    };

    const create = (cmd) => {
        checkIsCreatePhysicalFlowCommand(cmd);
        return $http
            .post(base, cmd)
            .then(r => r.data);
    };


    const searchReports = (query) => {
        return $http
            .get(`${base}/search-reports/${query}`)
            .then(r => r.data);
    };


    const deleteById = (id) => $http
            .delete(`${base}/${id}`)
            .then(r => r.data);


    const updateSpecDefinitionId = (flowId, command) => {
        return $http
            .post(`${base}/id/${flowId}/spec-definition`, command)
            .then(r => r.data);
    };


    const updateAttribute = (flowId, command) => {
        return $http
            .post(`${base}/id/${flowId}/attribute`, command)
            .then(r => r.data);
    };


    const validateUpload = (commands) => {
        return $http
            .post(`${base}/upload/validate`, commands)
            .then(r => r.data);
    };


    const upload = (commands) => {
        return $http
            .post(`${base}/upload`, commands)
            .then(r => r.data);
    };


    const cleanupOrphans = () => $http
        .get(`${base}/cleanup-orphans`)
        .then(r => r.data);


    return {
        findBySpecificationId,
        findByExternalId,
        findByEntityReference,
        findByProducerEntityReference,
        findByConsumerEntityReference,
        findBySelector,
        merge,
        getById,
        searchReports,
        create,
        deleteById,
        updateSpecDefinitionId,
        updateAttribute,
        validateUpload,
        upload,
        cleanupOrphans
    };
}


store.$inject = [
    "$http",
    "BaseApiUrl"
];


export const serviceName = "PhysicalFlowStore";



export const PhysicalFlowStore_API = {
    findBySpecificationId: {
        serviceName,
        serviceFnName: "findBySpecificationId",
        description: "executes findBySpecificationId"
    },
    findByEntityReference: {
        serviceName,
        serviceFnName: "findByEntityReference",
        description: "executes findByEntityReference"
    },
    findByProducerEntityReference: {
        serviceName,
        serviceFnName: "findByProducerEntityReference",
        description: "executes findByProducerEntityReference"
    },
    findByConsumerEntityReference: {
        serviceName,
        serviceFnName: "findByConsumerEntityReference",
        description: "executes findByConsumerEntityReference"
    },
    findByExternalId: {
        serviceName,
        serviceFnName: "findByExternalId",
        description: "executes findByExternalId"
    },
    findBySelector: {
        serviceName,
        serviceFnName: "findBySelector",
        description: "executes findBySelector"
    },
    merge: {
        serviceName,
        serviceFnName: "merge",
        description: "merge [from, to]"
    },
    getById: {
        serviceName,
        serviceFnName: "getById",
        description: "executes getById"
    },
    searchReports: {
        serviceName,
        serviceFnName: "searchReports",
        description: "executes searchReports"
    },
    create: {
        serviceName,
        serviceFnName: "create",
        description: "executes create"
    },
    deleteById: {
        serviceName,
        serviceFnName: "deleteById",
        description: "executes deleteById"
    },
    updateSpecDefinitionId: {
        serviceName,
        serviceFnName: "updateSpecDefinitionId",
        description: "executes updateSpecDefinitionId"
    },
    updateAttribute: {
        serviceName,
        serviceFnName: "updateAttribute",
        description: "executes updateAttribute"
    },
    validateUpload: {
        serviceName,
        serviceFnName: "validateUpload",
        description: "executes validateUpload"
    },
    upload: {
        serviceName,
        serviceFnName: "upload",
        description: "executes upload"
    },
    cleanupOrphans: {
        serviceName,
        serviceFnName: "cleanupOrphans",
        description: "cleans up orphaned physical flows"
    }
};