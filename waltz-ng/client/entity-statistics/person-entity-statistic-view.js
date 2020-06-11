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
import { entityLifecycleStatuses, resetData } from "../common";
import { mkSelectionOptions } from "../common/selector-utils";
import { hasRelatedDefinitions, navigateToStatistic } from "./utilities";
import { dynamicSections } from "../dynamic-section/dynamic-section-definitions";
import { CORE_API } from "../common/services/core-api-utils";


import template from "./person-entity-statistic-view.html";


const initData = {
    allDefinitions: [],
    applications: [],
    bookmarkSection: dynamicSections.bookmarksSection,
    orgUnits: [],
    statistic: {
        definition: null,
        summary: null,
        values: []
    },
    relatedDefinitions: null,
    summaries: [],
    directs: [],
    duration: "MONTH",
    managers: [],
    peers: [],
    person: null,
    history: [],
    visibility: {
        related: false
    },
    reloading: false
};


function mkHistory(history = [], current) {
    if (!current) return history;

    return _.concat([current], history);
}


function mkStatisticSelector(entityRef, scope) {
    const selector = mkSelectionOptions(entityRef, scope);
    selector.entityLifecycleStatuses = [
        entityLifecycleStatuses.ACTIVE,
        entityLifecycleStatuses.PENDING,
        entityLifecycleStatuses.REMOVED
    ];

    return selector;
}


function controller($q,
                    $state,
                    $stateParams,
                    serviceBroker) {

    const vm = resetData(this, initData);
    const statId = $stateParams.statId;
    const personId = $stateParams.id;

    const personPromise = serviceBroker
        .loadViewData(CORE_API.PersonStore.getById, [personId])
        .then(r => r.data);

    vm.statRef = {
        id: statId,
        kind: "ENTITY_STATISTIC"
    };

    const definitionPromise = serviceBroker
        .loadViewData(CORE_API.EntityStatisticStore.findRelatedStatDefinitions, [statId])
        .then(r => r.data)
        .then(ds => vm.relatedDefinitions = ds)
        .then(ds => vm.statistic.definition = ds.self)
        .then(() => vm.statRef = Object.assign({}, vm.statRef, { name: vm.statistic.definition.name }))
        .then(() => vm.visibility.related = hasRelatedDefinitions(vm.relatedDefinitions));

    const allDefinitionsPromise = serviceBroker
        .loadViewData(CORE_API.EntityStatisticStore.findAllActiveDefinitions, [])
        .then(r => vm.allDefinitions = r.data);

    const orgUnitsPromise = serviceBroker
        .loadAppData(CORE_API.OrgUnitStore.findAll, [])
        .then(r => vm.orgUnits = r.data);

    $q.all([personPromise, definitionPromise])
        .then(([person, definitions]) => vm.onSelectPerson(person))
        .then(allDefinitionsPromise)
        .then(orgUnitsPromise);


    function resetValueData() {
        const clearData = resetData({}, initData);
        vm.statistic.summary = clearData.statistic.summary;
        vm.statistic.values = clearData.statistic.values;
        vm.summaries = clearData.summaries;
        vm.history = [];
    }

    function loadHistory() {
        const selector = mkStatisticSelector(vm.parentRef, "CHILDREN");

        serviceBroker
            .loadViewData(
                CORE_API.EntityStatisticStore.calculateHistoricStatTally,
                [vm.statistic.definition, selector, vm.duration])
            .then(r => vm.history = mkHistory(r.data, vm.statistic.summary));
    }

    vm.onSelectPerson = (person) => {
        vm.reloading = true;

        resetValueData();
        vm.person = person;

        const entityReference = {
            id: person.id,
            kind: "PERSON"
        };
        vm.parentRef = entityReference;

        const selector = mkStatisticSelector(entityReference, "CHILDREN");

        serviceBroker
            .loadViewData(
                CORE_API.EntityStatisticStore.calculateStatTally,
                [vm.statistic.definition, selector])
            .then(r => {
                vm.statistic.summary = r.data;
                vm.reloading = false;
            })
            .then(() => {
                const related = vm.relatedDefinitions.children;

                const relatedIds = _.chain(related)
                    .filter(s => s !== null)
                    .map("id")
                    .value();

                return serviceBroker
                    .loadViewData(CORE_API.EntityStatisticStore.findStatTallies, [relatedIds, selector])
                    .then(r => r.data)
            })
            .then(summaries => vm.summaries = summaries);

        serviceBroker
            .loadViewData(
                CORE_API.EntityStatisticStore.findStatValuesByIdSelector,
                [statId, selector])
            .then(r => vm.statistic.values = r.data);

        serviceBroker
            .loadViewData(CORE_API.PersonStore.findDirects, [person.employeeId])
            .then(r => vm.directs = r.data);

        serviceBroker
            .loadViewData(CORE_API.PersonStore.findManagers, [person.employeeId])
            .then(r => vm.managers = r.data);

        serviceBroker
            .loadViewData(CORE_API.PersonStore.findDirects, [person.managerEmployeeId])
            .then(r => r.data)
            .then(peers => _.reject(peers, p => p.id === person.id))
            .then(peers => vm.peers = peers);

        serviceBroker
            .loadViewData(
                CORE_API.EntityStatisticStore.findStatAppsByIdSelector,
                [statId, selector])
            .then(r => vm.applications = r.data);

        loadHistory();

    };

    vm.onSelectDefinition = (node) => {
        navigateToStatistic($state, node.id, vm.parentRef);
    };


    vm.onChangeDuration = (d) => {
        vm.duration = d;
        loadHistory();
    };
}


controller.$inject = [
    "$q",
    "$state",
    "$stateParams",
    "ServiceBroker"
];


const page = {
    controller,
    template,
    controllerAs: "ctrl"
};


export default page;