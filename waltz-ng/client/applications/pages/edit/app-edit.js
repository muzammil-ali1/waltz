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
import * as fields from "../../formly/fields";
import {CORE_API} from '../../../common/services/core-api-utils';

import template from './app-edit.html';


function mkRef(orgUnit) {
    return {
        kind: 'ORG_UNIT',
        id: orgUnit.id,
        name: orgUnit.name,
        description: orgUnit.description
    };
}


const fieldLayout = [
    {
        className: 'row',
        fieldGroup: [
            {className: 'col-xs-8', fieldGroup: [ fields.nameField, fields.orgUnitField ]},
            {className: 'col-xs-4', fieldGroup: [ fields.assetCodeField, fields.parentAssetCodeField ]}
        ]
    }, {
        className: 'row',
        fieldGroup: [
            { className: 'col-xs-8', fieldGroup: [ fields.descriptionField ]},
            { className: 'col-xs-4', fieldGroup: [ fields.overallRatingField, fields.typeField, fields.lifecyclePhaseField, fields.businessCriticalityField ]}
        ]
    }
];


function fieldValuesRender(field) {
    const original = field.originalModel ? field.originalModel[field.key] : undefined;
    const current = field.model[field.key];

    if (field.type === 'tags-input') {
        return {
            original: JSON.stringify(original),
            current: JSON.stringify(_.map(current, 'text'))
        };
    } else {
        return { original, current };
    }
}


function fieldDiff(field) {
    const values = fieldValuesRender(field);
    return Object.assign({}, {
        key: field.key,
        name: field.templateOptions.label,
        dirty: field.formControl.$dirty }, values);
}


function setupFields(fields = [], formModel = {}) {
    fields.nameField.model = formModel.app;
    fields.descriptionField.model = formModel.app;
    fields.assetCodeField.model = formModel.app;
    fields.parentAssetCodeField.model = formModel.app;
    fields.orgUnitField.model = formModel.app;
    fields.typeField.model = formModel.app;
    fields.lifecyclePhaseField.model = formModel.app;
    fields.overallRatingField.model = formModel.app;
    fields.businessCriticalityField.model = formModel.app;
}


function controller(app,
                    appStore,
                    notification,
                    $state,
                    serviceBroker) {

    const vm = this;



    // -- BOOT --
    vm.$onInit = () => {
        vm.application = app;
        vm.fieldLayout = fieldLayout
        vm.formModel = Object.assign({}, { app });

        serviceBroker
            .loadViewData(CORE_API.OrgUnitStore.getById, [app.organisationalUnitId])
            .then(r => mkRef(r.data))
            .then(ouRef => fields.orgUnitField.templateOptions.ouRef = ouRef)
            .then(() => setupFields(fields, vm.formModel));
    };


    // -- INTERACT --
    vm.onSubmit = () => {
        const onSuccess = () => {
            notification.success('Application updated');
            $state.go('main.app.view', { id: app.id });
        };

        const onFailure = (result) => {
            console.error(result);
            notification.error('Error: '+ result.statusText);
        };

        const changes = _.chain(fields)
            .map(fieldDiff)
            .filter(fd => fd.dirty)
            .value();

        const action = {
            app: vm.formModel.app,
            changes
        };

        appStore
            .update(app.id, action)
            .then(onSuccess, onFailure);
    }
}


controller.$inject = [
    'app',
    'ApplicationStore',
    'Notification',
    '$state',
    'ServiceBroker'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};
