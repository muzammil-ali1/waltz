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


import {checkIsEntityRef} from "./checks";

export function determineDownwardsScopeForKind(kind) {
    switch (kind) {
        case "ACTOR":
        case "APPLICATION":
        case "APP_GROUP":
        case "CHANGE_INITIATIVE":
        case "FLOW_DIAGRAM":
        case "LICENCE":
        case "LOGICAL_DATA_ELEMENT":
        case "LOGICAL_FLOW":
        case "PHYSICAL_FLOW":
        case "PHYSICAL_SPECIFICATION":
        case "SCENARIO":
        case "SERVER":
        case "SOFTWARE":
        case "SOFTWARE_VERSION":
            return "EXACT";
        default:
            return "CHILDREN";
    }
}


export function determineUpwardsScopeForKind(kind) {
    switch (kind) {
        case "ORG_UNIT":
        case "MEASURABLE":
        case "DATA_TYPE":
        case "CHANGE_INITIATIVE":
            return "PARENTS";
        default:
            return "EXACT";
    }
}


/**
 * Helper method to construct valid IdSelectionOption instances.
 * @param entityReference
 * @param scope
 * @param entityLifecycleStatuses
 * @param filters
 * @param linkingEntityKind
 * @returns {{entityLifecycleStatuses: string[], entityReference: {kind: *, id: *}, scope: (*|string), filters}}
 */
export function mkSelectionOptions(entityReference, scope, entityLifecycleStatuses = ["ACTIVE"], filters = {}) {
    checkIsEntityRef(entityReference);

    return {
        entityReference: { id: entityReference.id, kind: entityReference.kind }, // use minimal ref to increase cache hits in broker
        scope: scope || determineDownwardsScopeForKind(entityReference.kind),
        entityLifecycleStatuses,
        filters
    };
}


export function mkSelectionOptionsWithJoiningEntity(entityReference, scope, entityLifecycleStatuses = ["ACTIVE"], filters = {}, joiningEntityKind = null) {
    checkIsEntityRef(entityReference);

    return {
        entityReference: { id: entityReference.id, kind: entityReference.kind }, // use minimal ref to increase cache hits in broker
        scope: scope || determineDownwardsScopeForKind(entityReference.kind),
        entityLifecycleStatuses,
        filters,
        joiningEntityKind: joiningEntityKind
    };
}


/**
 * @deprecated use mkSelectionOptions instead, this method now  just calls that one.
 * TODO: Remove in 1.23
 *
 * @param entityReference
 * @param scope
 * @param entityLifecycleStatuses
 * @param filters
 * @returns {{entityLifecycleStatuses: string[], entityReference: {kind: *, id: *}, scope: (*|string), filters}}
 */
export function mkApplicationSelectionOptions(entityReference,
                                              scope,
                                              entityLifecycleStatuses = ["ACTIVE"],
                                              filters = {}) {

    console.log("Calls to mkApplicationSelectionOptions are deprecated, calling mkSelectionOptions instead");
    // debugger;
    return mkSelectionOptions(entityReference, scope, entityLifecycleStatuses, filters);
}