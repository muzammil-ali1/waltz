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
import {initialiseData, termSearch} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkEntityLinkGridCell, mkLinkGridCell} from "../../../common/grid-utils";
import {mkSelectionOptions} from "../../../common/selector-utils";
import {withWidth} from "../../../physical-flow/physical-flow-table-utilities";
import {countByVersionsByPackageId} from "../../../software-catalog/software-catalog-utilities";
import {loadAssessmentsBySelector} from "../../../assessments/assessment-utils";

import template from "./technology-section.html";
import {tryOrDefault} from "../../../common/function-utils";


const bindings = {
    parentEntityRef: "<"
};

const initialState = {
    activeTabIndex: 0,
    repeatedPackages: []
};


function mkEndOfLifeCell(title, dateField, flagField) {
    return {
        field: dateField,
        displayName: title,
        cellTemplate: `
            <div class="ui-grid-cell-contents">
                <span ng-bind="row.entity.${dateField}"></span>
                <waltz-icon ng-if="row.entity.${flagField}"
                            name="power-off">
                </waltz-icon>
            </div>`
    };
}


function mkBooleanColumnFilter(uiGridConstants) {
    return {
        type: uiGridConstants.filter.SELECT,
        selectOptions: [
            {value: "true", label: "Yes"},
            {value: "false", label: "No"}
        ]
    };
}


function isEndOfLife(endOfLifeStatus) {
    return endOfLifeStatus === "END_OF_LIFE";
}


function createDefaultTableOptions($animate, uiGridConstants, exportFileName = "export.csv") {
    return {
        columnDefs: [],
        data: [],
        enableGridMenu: true,
        enableFiltering: true,
        enableHorizontalScrollbar: uiGridConstants.scrollbars.NEVER,
        enableSorting: true,
        exporterCsvFilename: exportFileName,
        exporterMenuPdf: false,
        onRegisterApi: (gridApi) => {
            $animate.enabled(gridApi.grid.element, false);
        }
    };
}


function prepareServerGridOptions($animate, uiGridConstants) {

    const columnDefs = [
        mkLinkGridCell("Host", "hostname", "id", "main.server.view"),
        { field: "environment" },
        {
            field: "virtual",
            displayName: "Virtual",
            width: "5%",
            filter: mkBooleanColumnFilter(uiGridConstants),
            cellTemplate: `
                <div class="ui-grid-cell-contents">
                    <waltz-icon ng-if="COL_FIELD"
                                name="check">
                    </waltz-icon>
                </div>`
        },
        { field: "operatingSystem", displayName: "OS" },
        { field: "operatingSystemVersion", displayName: "Version" },
        { field: "location" },
        { field: "country" },
        mkEndOfLifeCell("h/w EOL On", "hardwareEndOfLifeDate", "isHwEndOfLife"),
        mkEndOfLifeCell("OS EOL On", "operatingSystemEndOfLifeDate", "isOperatingSystemEndOfLife"),
        {
            field: "lifecycleStatus",
            displayName: "Lifecycle",
            cellFilter: "toDisplayName:'lifecycleStatus'"
        }
    ];

    const baseTable = createDefaultTableOptions($animate, uiGridConstants, "server.csv");
    return _.extend(baseTable, {
        columnDefs,
        rowTemplate: "<div ng-class=\"{'bg-danger': row.entity.isHwEndOfLife || row.entity.isOperatingSystemEndOfLife}\"><div ng-repeat=\"col in colContainer.renderedColumns track by col.colDef.name\" class=\"ui-grid-cell\" ui-grid-cell></div></div>"
    });
}


function prepareDatabaseGridOptions($animate, uiGridConstants) {

    const columnDefs = [
        { field: "instanceName", displayName: "Instance" },
        { field: "databaseName", displayName: "Database" },
        { field: "environment" },
        { field: "dbmsVendor", displayName: "Vendor" },
        { field: "dbmsName", displayName: "Product Name" },
        { field: "dbmsVersion", displayName: "Version" },
        mkEndOfLifeCell("EOL", "endOfLifeDate", "isEndOfLife"),
        {
            field: "lifecycleStatus",
            displayName: "Lifecycle",
            cellFilter: "toDisplayName:'lifecycleStatus'"
        }
    ];

    const baseTable = createDefaultTableOptions($animate, uiGridConstants, "database.csv");
    return _.extend(baseTable, {
        columnDefs,
        rowTemplate: "<div ng-class=\"{'bg-danger': row.entity.isEndOfLife}\"><div ng-repeat=\"col in colContainer.renderedColumns track by col.colDef.name\" class=\"ui-grid-cell\" ui-grid-cell></div></div>"
    });
}


function prepareLicenceGridOptions($animate, uiGridConstants, assessmentDefs) {
    const assessmentFields = _.map(assessmentDefs, d => {
        return {
            field: `${d.externalId}.ratingItem.name`,
            displayName: d.name
        };
    });

    const columnDefs = _.union(
        [
            mkLinkGridCell("Name", "name", "id", "main.licence.view"),
            { field: "externalId", displayName: "External Id" },
        ],
        assessmentFields
    );

    const baseTable = createDefaultTableOptions($animate, uiGridConstants, "licences.csv");
    return _.extend(baseTable, {
        columnDefs
    });
}


function prepareSoftwareCatalogGridOptions($animate, uiGridConstants) {

    const columnDefs = [
        withWidth(mkEntityLinkGridCell("Name", "package", "none", "right"), "20%"),
        { field: "version.externalId", displayName: "External Id", width: "35%" },
        withWidth(mkEntityLinkGridCell("Version", "version", "none", "right"), "10%"),
        { field: "version.releaseDate", displayName: "Release Date", width: "5%"},
        { field: "package.description", displayName: "Description", width: "25%"},
        { field: "package.isNotable", displayName: "Notable", width: "5%"}
    ];

    const baseTable = createDefaultTableOptions($animate, uiGridConstants, "software.csv");
    return _.extend(baseTable, {
        columnDefs
    });
}


function enrichServersWithEOLFlags(servers = []) {
    return _
        .map(
            servers,
            s => Object.assign(
                {},
                s,
                {
                    "isHwEndOfLife": isEndOfLife(s.hardwareEndOfLifeStatus),
                    "isOperatingSystemEndOfLife": isEndOfLife(s.operatingSystemEndOfLifeStatus)
                }));
}


function combineServersAndUsage(servers = [], serverUsage = []) {
    const serversById = _.keyBy(servers, "id");
    return _.map(serverUsage, su => Object.assign({}, serversById[su.serverId], su));
}


function controller($q, $animate, uiGridConstants, serviceBroker) {

    const vm = initialiseData(this, initialState);

    function refresh(qry) {
        if (qry) {
            vm.filteredServers = termSearch(vm.servers, qry);
            vm.filteredServerUsage = termSearch(vm.serverGridOptions.data, qry);
            vm.filteredDatabases = termSearch(vm.databases, qry);
        } else {
            vm.filteredServers = vm.servers;
            vm.filteredServerUsage = vm.serverGridOptions.data;
            vm.filteredDatabases = vm.databases;
        }
    }

    vm.$onInit = () => {
        const usagePromise = serviceBroker
            .loadViewData(
                CORE_API.ServerUsageStore.findByReferencedEntity,
                [ vm.parentEntityRef ])
            .then(r => vm.serverUsage = r.data);

        const serverPromise = serviceBroker
            .loadViewData(
                CORE_API.ServerInfoStore.findByAppId,
                [ vm.parentEntityRef.id ])
            .then(r => {
                vm.servers = enrichServersWithEOLFlags(r.data);
            });

        $q.all([usagePromise, serverPromise])
            .then(() => {
                vm.serverGridOptions.data = combineServersAndUsage(vm.servers, vm.serverUsage);
                refresh(vm.qry);
            });

        serviceBroker
            .loadViewData(
                CORE_API.DatabaseStore.findByAppId,
                [ vm.parentEntityRef.id ])
            .then(r => {
                vm.databases = r.data;
                _.forEach(vm.databases,
                          (db) => Object.assign(db, {
                              "isEndOfLife": isEndOfLife(db.endOfLifeStatus)
                          })
                );
                vm.databaseGridOptions.data = vm.databases;
            })
            .then(() => refresh(vm.qry));

        // licences
        const licencePromise = serviceBroker
            .loadViewData(
                CORE_API.LicenceStore.findBySelector,
                [mkSelectionOptions(vm.parentEntityRef)]
            )
            .then(r => r.data);

        $q.all([licencePromise, loadAssessmentsBySelector($q, serviceBroker, "LICENCE", mkSelectionOptions(vm.parentEntityRef),true)])
            .then(([licences, assessments]) => {
                const definitions = assessments.definitions;
                const assessmentsByLicenceId = assessments.assessmentsByEntityId;

                vm.licences = licences;
                const licenceWithAssessments =_.map(
                    vm.licences,
                    l => {
                        const assessmentsByDefinitionExtId = _.get(assessmentsByLicenceId, l.id, []);
                        return Object.assign({}, l, assessmentsByDefinitionExtId)
                    });

                vm.licenceGridOptions = prepareLicenceGridOptions($animate, uiGridConstants, definitions);
                vm.licenceGridOptions.data = licenceWithAssessments;
            })
            .then(() => refresh(vm.qry));


        // software catalog
        serviceBroker
            .loadViewData(
                CORE_API.SoftwareCatalogStore.findByAppIds,
                [[vm.parentEntityRef.id]]
            )
            .then(r => r.data)
            .then(softwareCatalog => {
                vm.softwareCatalog = softwareCatalog;
                const versionsById = _.keyBy(vm.softwareCatalog.versions, v => v.id);
                const packagesById = _.keyBy(vm.softwareCatalog.packages, v => v.id);

                const packageCounts = countByVersionsByPackageId(vm.softwareCatalog.usages);
                vm.repeatedPackages =_.chain(packageCounts)
                    .map((v,k) => ({
                        package: packagesById[k],
                        packageId: k,
                        count: v
                    }))
                    .filter(p => p.count > 1)
                    .orderBy(["count"], ["desc"])
                    .value();

                const gridData = _
                    .chain(vm.softwareCatalog.usages)
                    .map(u => Object.assign({}, _.pick(u, ["softwarePackageId", "softwareVersionId"])))
                    .uniqWith(_.isEqual)
                    .map(u => Object.assign(
                        { },
                        { package: packagesById[u.softwarePackageId] },
                        { version: versionsById[u.softwareVersionId] }
                    ))
                    .value();
                vm.softwareCatalogGridOptions.data = gridData;
            })
            .then(() => refresh(vm.qry));
    };

    vm.serverGridOptions = prepareServerGridOptions($animate, uiGridConstants);
    vm.databaseGridOptions = prepareDatabaseGridOptions($animate, uiGridConstants);
    vm.softwareCatalogGridOptions = prepareSoftwareCatalogGridOptions($animate, uiGridConstants);

    vm.doSearch = () => refresh(vm.qry);

    vm.hasAnyData = () => {
        const hasServers = tryOrDefault(() => vm.servers.length > 0, false);
        const hasDatabases = tryOrDefault(() => vm.databases.length > 0, false);
        const hasLicences = tryOrDefault(() => vm.licences.length > 0, false);
        const hasSoftware = tryOrDefault(() => vm.softwareCatalog.length > 0, false);
        return hasServers || hasDatabases || hasLicences || hasSoftware;
    };
}


controller.$inject = [
    "$q",
    "$animate",
    "uiGridConstants",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};

export default {
    id: "waltzTechnologySection",
    component
};

