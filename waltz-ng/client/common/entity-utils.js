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


import _ from "lodash";
import {checkIsEntityRef} from "./checks";
import {CORE_API} from "./services/core-api-utils";

export function sameRef(r1, r2, options = { skipChecks: false }) {
    if (! options.skipChecks) {
        checkIsEntityRef(r1);
        checkIsEntityRef(r2);
    }
    return r1.kind === r2.kind && r1.id === r2.id;
}



export function isSameParentEntityRef(changes) {
    return sameRef(
        changes.parentEntityRef.previousValue,
        changes.parentEntityRef.currentValue,
        {skipChecks: true});
}


export function refToString(r) {
    checkIsEntityRef(r);
    return `${r.kind}/${r.id}`;
}


export function stringToRef(s) {
    const bits = s.split("/");
    return {
        kind: bits[0],
        id: bits[1]
    };
}


export function toEntityRef(obj, kind = obj.kind) {

    const ref = {
        id: obj.id,
        kind,
        name: obj.name,
        description: obj.description
    };

    checkIsEntityRef(ref);

    return ref;
}


export function mkRef(kind, id, name, description) {
    const ref = {
        kind,
        id,
        name,
        description
    };

    checkIsEntityRef(ref);
    return ref;
}


function determineLoadByIdCall(kind) {
    switch (kind) {
        case "APPLICATION":
            return CORE_API.ApplicationStore.getById;
        case "ACTOR":
            return CORE_API.ActorStore.getById;
        case "CHANGE_INITIATIVE":
            return CORE_API.ChangeInitiativeStore.getById;
        default:
            throw "Unsupported kind for loadById: " + kind;
    }
}


function determineLoadByExtIdCall(kind) {
    switch (kind) {
        case "APPLICATION":
            return CORE_API.ApplicationStore.findByAssetCode;
        case "DATA_TYPE":
            return CORE_API.DataTypeStore.getDataTypeByCode;
        case "MEASURABLE":
            return CORE_API.MeasurableStore.findByExternalId;
        case "PERSON":
            return CORE_API.PersonStore.getByEmployeeId;
        case "PHYSICAL_FLOW":
            return CORE_API.PhysicalFlowStore.findByExternalId;
        default:
            throw "Unsupported kind for loadByExtId: " + kind;
    }
}


export function loadEntity(serviceBroker, entityRef) {
    checkIsEntityRef(entityRef);

    const remoteCall = determineLoadByIdCall(entityRef.kind);
    return serviceBroker
        .loadViewData(remoteCall, [ entityRef.id ])
        .then(r => r.data);
}


export function loadByExtId(serviceBroker, kind, extId) {
    try {
        const remoteCall = determineLoadByExtIdCall(kind);
        return serviceBroker
            .loadViewData(remoteCall, [extId])
            .then(r => _.defaultTo(r.data, []))
            .then(d => _.isArray(d) ? d : [d])
    } catch (e) {
        return Promise.reject(e);
    }
}


export function isRemoved(entity) {
    return entity.isRemoved || entity.entityLifecycleStatus === "REMOVED";
}
