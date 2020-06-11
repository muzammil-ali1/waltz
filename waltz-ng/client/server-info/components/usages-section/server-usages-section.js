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

import template from "./server-usages-section.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import _ from "lodash";


const bindings = {
    parentEntityRef: "<"
};


const initialState = {
    usages: []
};


function prepareUsages(participations = [],
                       physicalFlows = [],
                       physicalSpecs = [],
                       logicalFlows = []) {
    const logicalsById = _.keyBy(logicalFlows, "id");
    const physicalsById = _.keyBy(physicalFlows, "id");
    const specsById = _.keyBy(physicalSpecs, "id");

    return _
        .chain(participations)
        .map(p => {
            const pf = physicalsById[p.physicalFlowId];
            if (!pf) return null;
            const ps = specsById[pf.specificationId];
            if (!ps) return null;
            const lf = logicalsById[pf.logicalFlowId];
            if (!lf) return null;
            return {
                participation: p,
                logicalFlow: lf,
                physicalSpecification: ps,
                physicalFlow: pf
            };
        })
        .compact()
        .value();
}


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        const selectionOptions = mkSelectionOptions(vm.parentEntityRef);

        const participantPromise = serviceBroker
            .loadViewData(
                CORE_API.PhysicalFlowParticipantStore.findByParticipant,
                [ vm.parentEntityRef ])
            .then(r => r.data);

        const physicalFlowPromise = serviceBroker
            .loadViewData(
                CORE_API.PhysicalFlowStore.findBySelector,
                [ selectionOptions ])
            .then(r => r.data);

        const physicalSpecsPromise = serviceBroker
            .loadViewData(
                CORE_API.PhysicalSpecificationStore.findBySelector,
                [ selectionOptions ])
            .then(r => r.data);

        const logicalFlowPromise = serviceBroker
            .loadViewData(
                CORE_API.LogicalFlowStore.findBySelector,
                [ selectionOptions ])
            .then(r => r.data);

        const promises = [
            participantPromise,
            physicalFlowPromise,
            physicalSpecsPromise,
            logicalFlowPromise
        ];

        $q.all(promises)
            .then(([ participations, physicalFlows, physicalSpecs, logicalFlows ]) =>
                vm.usages = prepareUsages(
                    participations,
                    physicalFlows,
                    physicalSpecs,
                    logicalFlows));
    }
}


controller.$inject = ["$q", "ServiceBroker"];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzServerUsagesSection"
};