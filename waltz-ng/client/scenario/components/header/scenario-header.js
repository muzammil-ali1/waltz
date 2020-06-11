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

import template from "./scenario-header.html";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import roles from "../../../user/system-roles";


const bindings = {
    scenarioId: "<",
    availableSections: "<",
    openSections: "<"
};

const modes = {
    LOADING: "LOADING",
    VIEW: "VIEW",
    CONFIGURE_SCENARIO: "CONFIGURE_SCENARIO"
};

const initialState = {
    modes,
    roadmap: null,
    permissions: {
        admin: false,
        edit: false
    },
    visibility: {
        mode: modes.LOADING
    }
};


const addToHistory = (historyStore, scenario) => {
    if (! scenario) { return; }
    historyStore.put(
        scenario.name,
        "SCENARIO",
        "main.scenario.view",
        { id: scenario.id });
};


function controller($q,
                    historyStore,
                    notification,
                    serviceBroker,
                    userService) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        vm.visibility.mode = modes.LOADING;

        reloadAllData()
            .then(() => {
                vm.visibility.mode = modes.VIEW;
                addToHistory(historyStore, vm.scenario);
            });

        userService
            .whoami()
            .then(u => vm.permissions = {
                admin: userService.hasRole(u, roles.SCENARIO_ADMIN),
                edit: userService.hasRole(u, roles.SCENARIO_EDITOR)
            });

    };


    vm.onSaveScenarioName = (data, ctx) => {
        return updateField(
            ctx.id,
            CORE_API.ScenarioStore.updateName,
            data,
            true,
            "Scenario name updated")
            .then(() => reloadAllData());
    };

    vm.onSaveScenarioDescription = (data, ctx) => {
        return updateField(
            ctx.id,
            CORE_API.ScenarioStore.updateDescription,
            data,
            false,
            "Scenario description updated")
            .then(() => reloadAllData());
    };

    vm.onSaveScenarioEffectiveDate = (data, ctx) => {
        return updateField(
            ctx.id,
            CORE_API.ScenarioStore.updateEffectiveDate,
            data,
            true,
            "Scenario target date updated");
    };

    vm.onSaveScenarioType = (data, ctx) => {
        return updateField(
            ctx.id,
            CORE_API.ScenarioStore.updateScenarioType,
            { newVal: data },   // as this is coming from enum field, we need to fake out change object
            true,
            "Scenario type updated");
    };

    vm.onConfigureScenario = () => {
        vm.visibility.mode = modes.CONFIGURE_SCENARIO;
    };


    vm.onAddAxisItem = (axisItem) => {
        const args = [
            vm.scenario.id,
            axisItem.orientation,
            axisItem.domainItem,
            axisItem.position
        ];
        return serviceBroker
            .execute(
                CORE_API.ScenarioStore.addAxisItem,
                args);

    };

    vm.onRemoveAxisItem = (axisItem) => {
        const args = [
            vm.scenario.id,
            axisItem.orientation,
            axisItem.domainItem
        ];
        return serviceBroker
            .execute(
                CORE_API.ScenarioStore.removeAxisItem,
                args);
    };

    vm.onRepositionAxisItems = (scenarioId, orientation, ids) => {
        const args = [
            vm.scenario.id,
            orientation,
            ids
        ];

        return serviceBroker
            .execute(
                CORE_API.ScenarioStore.reorderAxis,
                args);
    };

    vm.onCancel = () => {
        vm.visibility.mode = modes.VIEW;
        reloadAllData();
    };


    // -- helpers --

    function updateField(roadmapId,
                         method,
                         data,
                         preventNull = true,
                         message = "Updated") {
        if (preventNull && (_.isEmpty(data.newVal) && !_.isDate(data.newVal))) {
            return Promise.reject("Waltz:updateField - Cannot set an empty value");
        }
        if (data.newVal !== data.oldVal) {
            return serviceBroker
                .execute(
                    method,
                    [ roadmapId, data.newVal ])
                .then(() => notification.success(message));
        } else {
            return Promise.reject("Nothing updated")
        }
    }


    function reloadAllData() {
        const roadmapPromise = serviceBroker
            .loadViewData(
                CORE_API.ScenarioStore.getById,
                [ vm.scenarioId ],
                { force: true })
            .then(r => {
                Object.assign(vm, r.data);
                Object.assign(vm.roadmap, {kind: 'ROADMAP'});
            });

        return $q
            .all([roadmapPromise]);
    }
}


controller.$inject = [
    "$q",
    "HistoryStore",
    "Notification",
    "ServiceBroker",
    "UserService"
];


const component = {
    bindings,
    template,
    controller
};


export default {
    id: "waltzScenarioHeader",
    component
};