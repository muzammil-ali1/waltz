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
import {CORE_API} from "../../../common/services/core-api-utils";
import {nest} from "d3-collection";

import {initialiseData} from "../../../common";
import {invokeFunction} from "../../../common/index";
import {refToString} from "../../../common/entity-utils";
import {mkEntityLinkGridCell} from "../../../common/grid-utils";


import template from "./bulk-logical-flow-uploader.html";


const bindings = {
    flows: '<',

    onInitialise: '<',
    onUploadComplete: '<'
};


const initialState = {
    columnDefs: [],
    loading: false,

    onInitialise: (event) => console.log('default onInitialise handler for bulk-logical-flow-uploader: ', event),
    onUploadComplete: () => console.log('default onUploadComplete handler for bulk-logical-flow-uploader')
};


async function findExistingLogicalFlows(serviceBroker, sourcesAndTargets, force = false) {
    return serviceBroker
        .loadViewData(
            CORE_API.LogicalFlowStore.findBySourceAndTargetEntityReferences,
            [sourcesAndTargets],
            { force })
        .then(r => r.data)
}


async function findOrAddLogicalFlows(serviceBroker, sourceTargets = []) {
    if(_.isEmpty(sourceTargets)) {
        return sourceTargets;
    }

    // retrieves logical flows by source & target refs
    const existingLogicalFlows = await findExistingLogicalFlows(serviceBroker, sourceTargets);

    //compare with flows and add new ones
    const existingFlowsBySourceByTarget = nest()
        .key(flow => refToString(flow.source))
        .key(flow => refToString(flow.target))
        .object(existingLogicalFlows);

    const logicalFlowsToAdd = _
        .chain(sourceTargets)
        .filter(f => {
            const sourceRefString = refToString(f.source);
            const targetRefString = refToString(f.target);
            return _.get(existingFlowsBySourceByTarget, `[${sourceRefString}][${targetRefString}]`) === undefined;
        })
        .uniqBy(f => refToString(f.source) + ' ' + refToString(f.target))
        .value();

    const addFlowCmds = _.map(logicalFlowsToAdd, p => ({source: p.source, target: p.target}));

    let allServerLogicalFlows = existingLogicalFlows;
    if(! _.isEmpty(addFlowCmds)) {
        const addedFlows = await serviceBroker
            .execute(CORE_API.LogicalFlowStore.addFlows, [addFlowCmds])
            .then(r => r.data);
        allServerLogicalFlows = _.union(existingLogicalFlows, addedFlows);
    }

    return allServerLogicalFlows;
}


async function updateDecorators(serviceBroker, existingLogicalFlows, newFlows) {
    // get the decorators that need to be added by flow id
    const flowsBySourceByTarget = nest()
        .key(flow => refToString(flow.source))
        .key(flow => refToString(flow.target))
        .object(newFlows);

    const updateCmds = _.map(existingLogicalFlows, f => {
        const sourceRefString = refToString(f.source);
        const targetRefString = refToString(f.target);
        const dataTypeRefs = _
            .chain(flowsBySourceByTarget)
            .get(`[${sourceRefString}][${targetRefString}]`)
            .map(f => f.dataType)
            .value();

        return {
            flowId: f.id,
            addedDecorators: dataTypeRefs,
            removedDecorators: []
        };
    });

    return serviceBroker
        .execute(CORE_API.LogicalFlowDecoratorStore.updateDecoratorsBatch, [updateCmds])
        .then(r => r.data);
}


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const uploadFlows = () => {
        if(vm.flows) {
            vm.loading = true;
            findOrAddLogicalFlows(serviceBroker, vm.flows)
                .then(logicalFlows => updateDecorators(serviceBroker, logicalFlows, vm.flows))
                .then(decorators => vm.loading = false)
                .then(() => invokeFunction(vm.onUploadComplete));
        }
    };


    vm.$onInit = () => {
        vm.columnDefs = [
            mkEntityLinkGridCell('Source', 'source'),
            mkEntityLinkGridCell('Target', 'target'),
            mkEntityLinkGridCell('Data Type', 'dataType')
        ];

        const api = {
            uploadFlows
        };
        invokeFunction(vm.onInitialise, api);
    };
}


controller.$inject = [
    'ServiceBroker'
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzBulkLogicalFlowUploader'
};
