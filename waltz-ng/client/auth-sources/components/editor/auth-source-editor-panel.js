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

import template from './auth-source-editor-panel.html';
import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";
import _ from 'lodash';
import {toEntityRef} from "../../../common/entity-utils";


const bindings = {
    parentEntityRef: '<'
};


const initialState = {
    createForm: {
        dataType: null,
        orgUnit: null,
        rating: null,
        app: null,
    },
    updateForm: {
        description: "",
        rating: null
    },
    preventCreateFormSubmit: true,
    visibility: {
        createForm: false,
        updateForm: false
    }
};



function controller($q, serviceBroker, notification) {

    const vm = initialiseData(this, initialState);

    vm.onCreateFormChange = () => {
        vm.preventCreateFormSubmit = _.some(_.values(vm.createForm), v => v === null);
    };

    vm.closeForms = () => {
        vm.visibility = {
            createForm: false,
            updateForm: false
        }
    };

    const refresh = () => {
        serviceBroker
            .loadViewData(CORE_API.AuthSourcesStore.findAll, [], { force: true })
            .then(r => {
                const orgUnitsById = _.keyBy(vm.orgUnits, 'id');
                const dataTypesByCode = _.keyBy(vm.dataTypes, 'code');
                const authSources = r.data;

                vm.authSourceTable = _
                    .chain(authSources)
                    .map(d => {
                        return {
                            authSource: d,
                            orgUnit: toEntityRef(orgUnitsById[d.parentReference.id], 'ORG_UNIT'),
                            dataType: dataTypesByCode[d.dataType]
                        };
                    })
                    .sortBy('dataType.name', 'authSource.applicationReference.name')
                    .value();
            });
    };

    vm.$onInit = () => {
        const promises  = [
            serviceBroker
                .loadAppData(CORE_API.DataTypeStore.findAll)
                .then(r => {
                    vm.dataTypes = r.data;
                }),
            serviceBroker
                .loadAppData(CORE_API.OrgUnitStore.findAll)
                .then(r => vm.orgUnits = r.data)
        ];

        $q.all(promises).then(() => refresh());
    };

    vm.remove = (authSource) => {
        if (confirm('Are you sure you want to delete this Authoritative Source ?')) {
            serviceBroker
                .execute(CORE_API.AuthSourcesStore.remove, [authSource.id])
                .then(() => {
                    refresh();
                    notification.warning('Authoritative Source removed');
                });
        }
    };

    vm.onSelectDataType = (dt) => {
        vm.createForm.dataType = Object.assign({}, dt, { kind: 'DATA_TYPE' });
        vm.onCreateFormChange();
    };

    vm.onClearDataType = () => {
        vm.createForm.dataType = null;
        vm.onCreateFormChange();
    };

    vm.onSelectOrgUnit = (ou) => {
        vm.createForm.orgUnit = Object.assign({}, ou, { kind: 'ORG_UNIT' });
        vm.onCreateFormChange();
    };

    vm.onClearOrgUnit = () => {
        vm.createForm.orgUnit = null;
        vm.onCreateFormChange();
    };

    vm.onSelectApp = (app) => {
        vm.createForm.app = Object.assign({}, app, { kind: 'APPLICATION'});
        vm.onCreateFormChange();
    };

    vm.onClearApp = () => {
        vm.createForm.app = null;
        vm.onCreateFormChange();
    };

    vm.isCreateFormDisabled = () => vm.preventCreateFormSubmit;

    vm.onShowCreateForm = () => {
        vm.closeForms();
        vm.visibility.createForm = true;
    };

    vm.onDismissCreateForm = () => {
        vm.closeForms();
    };

    vm.onShowUpdateForm = (d) => {
        vm.closeForms();
        vm.updateForm.description = d.description;
        vm.updateForm.rating = d.rating;
        vm.updateForm.id = d.id;
        vm.visibility.updateForm = true;
    };

    vm.onDismissUpdateForm = () => {
        console.log('dismiss')
        vm.closeForms();
    };

    vm.submitUpdate = () => {
        console.log('sn', vm.updateForm);
        const cmd = {
            description: vm.updateForm.description || "",
            rating: vm.updateForm.rating,
            id: vm.updateForm.id
        };

        serviceBroker
            .execute(CORE_API.AuthSourcesStore.update, [cmd])
            .then(() => {
                notification.success("Authoritative Source updated");
                vm.closeForms();
                refresh();
            });

    };

    vm.submitCreate = () => {
        const cmd = {
            description: vm.createForm.description,
            rating: vm.createForm.rating,
            applicationId: vm.createForm.app.id,
            dataTypeId: vm.createForm.dataType.id,
            orgUnitId: vm.createForm.orgUnit.id
        };

        serviceBroker
            .execute(CORE_API.AuthSourcesStore.insert, [cmd])
            .then(() => {
                notification.success("Authoritative Source created");
                vm.closeForms();
                refresh();
            });
    };
}


controller.$inject = [
    '$q',
    'ServiceBroker',
    'Notification'
];


export const component = {
    template,
    controller,
    bindings
};


export const id = 'waltzAuthSourceEditorPanel';