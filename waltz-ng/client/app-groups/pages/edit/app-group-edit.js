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

import template from "./app-group-edit.html";
import {mkSelectionOptions} from "../../../common/selector-utils";


const initialState = {
    changeInitiatives: [],
    selectedChangeInitiative: null,
    editor: "SINGLE",
    canDelete: false,
    history: [],
    organisationalUnits: [],
    currentOrgUnit: null
};


function setup(groupDetail) {
    const {organisationalUnits, applications, members, appGroup} = groupDetail;

    const owners = _.filter(members, m => m.role === "OWNER");
    const viewers = _.filter(members, m => m.role === "VIEWER");

    return {
        owners,
        viewers,
        organisationalUnits,
        applications,
        appGroup
    };
}


function handleError(e) {
    alert(e.data.message);
}


function navigateToLastView($state, historyStore) {
    const lastHistoryItem = historyStore.getAll()[0];
    if (lastHistoryItem) {
        $state.go(lastHistoryItem.state, lastHistoryItem.stateParams);
    } else {
        $state.go("main.home");
    }
}


function removeFromHistory(historyStore, appGroup) {
    if (! appGroup ) { return; }

    const historyObj = mkHistoryObj(appGroup);

    historyStore.remove(
        historyObj.name,
        historyObj.kind,
        historyObj.state,
        historyObj.stateParams);
}


function mkHistoryObj(appGroup) {
    return {
        name: appGroup.name,
        kind: "APP_GROUP",
        state: "main.app-group.view",
        stateParams: { id: appGroup.id }
    };
}

function recalcMembers(data) {
    const owners = _.filter(data, m => m.role === "OWNER");
    const viewers = _.filter(data, m => m.role === "VIEWER");

    return {
        owners,
        viewers
    };
}


function controller($q,
                    $state,
                    $scope,
                    $stateParams,
                    appStore,
                    historyStore,
                    logicalFlowStore,
                    localStorageService,
                    notification,
                    serviceBroker,
                    userService) {

    const { id }  = $stateParams;
    const vm = Object.assign(this, initialState);

    userService
        .whoami()
        .then(user => vm.user = user);


    serviceBroker.loadViewData(CORE_API.AppGroupStore.getById, [id])
        .then(r => setup(r.data))
        .then(data => Object.assign(vm, data))
        .then(() => {
            return userService
                .whoami()
                .then(me => {
                    const owner = _.find(vm.owners, o => o.userId === me.userName && o.role === "OWNER");
                    vm.canDelete = owner != null;
                });
        });


    vm.addToGroup = (app) => {
        serviceBroker
            .execute(CORE_API.AppGroupStore.addApplication, [id, app.id])
            .then(r => r.data)
            .then(apps => vm.applications = apps, e => handleError(e))
            .then(() => notification.success("Added: " + app.name));
    };


    vm.removeFromGroup = (app) => {
        serviceBroker
            .execute(CORE_API.AppGroupStore.removeApplication, [id, app.id])
            .then(r => r.data)
            .then(apps => vm.applications = apps, e => handleError(e))
            .then(() => notification.warning("Removed: " + app.name));
    };

    vm.addOrgUnitToGroup = (orgUnit) => {
        serviceBroker
            .execute(CORE_API.AppGroupStore.addOrganisationalUnit, [id, orgUnit.id])
            .then(r => r.data)
            .then(orgUnits => vm.organisationalUnits = orgUnits, e => handleError(e))
            .then(() => notification.success("Added: " + orgUnit.name));
    };


    vm.removeOrgUnitFromGroup = (orgUnit) => {
        serviceBroker
            .execute(CORE_API.AppGroupStore.removeOrganisationalUnit, [id, orgUnit.id])
            .then(r => r.data)
            .then(orgUnits => vm.organisationalUnits = orgUnits, e => handleError(e))
            .then(() => notification.warning("Removed: " + orgUnit.name));
    };


    vm.isAppInGroup = (app) => {
        return _.some(vm.applications, a => app.id === a.id);
    };

    vm.onOrgUnitSelect = (entity) => {
        vm.currentOrgUnit = entity;
        vm.addOrgUnitToGroup(entity);
    };
    vm.orgUnitSelectionFilter = (orgUnit) => {
        return (vm.currentOrgUnit && orgUnit.id !== vm.currentOrgUnit.id) || !vm.organisationalUnits.map(e=>e.id).includes(orgUnit.id);
    };
    vm.promoteToOwner = (member) => {
        serviceBroker
            .execute(CORE_API.AppGroupStore.addOwner, [member.groupId, member.userId])
            .then(r => Object.assign(vm, recalcMembers(r.data)))
            .then(() => notification.success(`User: ${member.userId} is now an owner of the group`));
    };


    vm.demoteToViewer = (member) => {
        serviceBroker
            .execute(CORE_API.AppGroupStore.removeOwner, [member.groupId, member.userId])
            .then(r => Object.assign(vm, recalcMembers(r.data)))
            .then(() => notification.success(`User: ${member.userId} is now an viewer of the group`));
    };


    vm.updateGroupOverview = () => {
        serviceBroker
            .execute(CORE_API.AppGroupStore.updateGroupOverview, [id, vm.appGroup])
            .then(() => notification.success("Group details updated"));
    };


    vm.focusOnApp = (app) => {
        const focusApp = {};

        appStore.getById(app.id)
            .then(fullApp => {
                focusApp.app = fullApp;
                const promises = [
                    appStore.findRelatedById(fullApp.id),
                    logicalFlowStore.findByEntityReference("APPLICATION", fullApp.id),
                    appStore.findBySelector({ entityReference: { id: fullApp.organisationalUnitId, kind: "ORG_UNIT"}, scope: "EXACT"})
                ];
                return $q.all(promises);
            })
            .then(([ related, flows, unitMembers]) => {
                focusApp.related = _.flatten(_.values(related));
                focusApp.unitMembers = _.reject(unitMembers, m => m.id === app.id);
                focusApp.upstream = _.chain(flows)
                    .map(f => f.source)
                    .uniqBy(source => source.id)
                    .reject(source => source.id === app.id)
                    .value();
                focusApp.downstream = _.chain(flows)
                    .map(f => f.target)
                    .uniqBy(target => target.id)
                    .reject(target => target.id === app.id)
                    .value();
            })
            .then(() => vm.focusApp = focusApp);
    };
    vm.showSingleEditor = () => {
        vm.editor = "SINGLE"
    };

    vm.showBulkEditor = () => {
        vm.editor = "BULK";
    };

    vm.saveApplications = (results) => {

        const unknownIdentifiers = _.chain(results)
            .filter(r => r.action == null)
            .map(r => r.identifier)
            .value();

        const appIdsToAdd = _.chain(results)
            .filter(r => r.action === "ADD")
            .map(r => r.entityRef.id)
            .value();

        const appIdsToRemove = _.chain(results)
            .filter(r => r.action === "REMOVE")
            .map(r => r.entityRef.id)
            .value();

        if (appIdsToAdd.length > 0) {
            serviceBroker
                .execute(CORE_API.AppGroupStore.addApplications,
                    [id, Object.assign({}, {applicationIds: appIdsToAdd, unknownIdentifiers: unknownIdentifiers})])
                .then(r => r.data)
                .then(apps => vm.applications = apps, e => handleError(e))
                .then(() => notification.success(`Added ${appIdsToAdd.length} applications`));
        }

        if (appIdsToRemove.length > 0) {
            serviceBroker
                .execute(CORE_API.AppGroupStore.removeApplications, [id, appIdsToRemove])
                .then(r => r.data)
                .then(apps => vm.applications = apps, e => handleError(e))
                .then(() => notification.success(`Removed ${appIdsToRemove.length} applications`));
        }

        if (appIdsToRemove.length === 0 && appIdsToAdd.length === 0){
            notification.info("There are no applications to be added or removed");
        }
    };


    vm.deleteGroup = () => {
        if (!confirm("Really delete this group ? \n " + vm.appGroup.name)) return;

        serviceBroker
            .execute(CORE_API.AppGroupStore.deleteGroup, [id])
            .then(() => notification.warning("Deleted group: " + vm.appGroup.name))
            .then(() => {
                removeFromHistory(historyStore, vm.appGroup);
                navigateToLastView($state, historyStore);
            });
    };



    // add app via search
    vm.searchedApp = {};

    $scope.$watch("ctrl.searchedApp.app", (app) => {
        if (!_.isObject(app)) return;
        vm.addToGroup(app);
        vm.focusOnApp(app);
    }, true);


    //add app via recently viewed
    vm.history = localStorageService
        .get("history_2").filter(r => r.kind === "APPLICATION") || [];

    vm.addRecentViewed = (app) => {
        app.id = app.stateParams.id;
        vm.addToGroup(app);
        vm.focusOnApp(app);
    };

    $scope.$watch(
        "ctrl.selectedChangeInitiative",
        (changeInitiative) => {
            if (!changeInitiative) return;

            serviceBroker
                .execute(CORE_API.AppGroupStore.addChangeInitiative, [id, changeInitiative.id])
                .then(r => r.data)
                .then(cis => vm.changeInitiatives = cis)
                .then(() => notification.success("Associated Change Initiative: " + changeInitiative.name));

        });

    vm.removeChangeInitiative = (changeInitiative) => serviceBroker
        .execute(CORE_API.AppGroupStore.removeChangeInitiative, [id, changeInitiative.id])
        .then(r => r.data)
        .then(cis => vm.changeInitiatives = cis)
        .then(() => notification.warning("Removed Change Initiative: " + changeInitiative.name));

    serviceBroker
        .loadViewData(
            CORE_API.ChangeInitiativeStore.findBySelector,
            [mkSelectionOptions({kind: "APP_GROUP", id}, "EXACT")])
        .then(result => vm.changeInitiatives = result.data);

}

controller.$inject = [
    "$q",
    "$state",
    "$scope",
    "$stateParams",
    "ApplicationStore",
    "HistoryStore",
    "LogicalFlowStore",
    "localStorageService",
    "Notification",
    "ServiceBroker",
    "UserService"
];


export default {
    template,
    controller,
    controllerAs: "ctrl"
};
