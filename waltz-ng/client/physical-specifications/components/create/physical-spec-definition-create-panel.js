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

import {initialiseData, invokeFunction} from "../../../common";
import _ from "lodash";
import template from './physical-spec-definition-create-panel.html';


const bindings = {
    status: '<',
    onCancel: '<',
    onSubmit: '<'
};


const initialState = {
    status: null,
    specDefinition: {
        def: {
            delimiter: ',',
            type: 'DELIMITED'
        }
    },
    specDefFields: {},
    onCancel: () => console.log('psdcp::onCancel'),
    onSubmit: (specDef) => console.log('psdcp::onSubmit', specDef)
};

const allowedTypes= ['DATE', 'DECIMAL', 'INTEGER', 'STRING', 'BOOLEAN', 'ENUM'];


function attemptSplit(line, position) {
    /* TEST STRINGS
Field1	INTEGER	description for field 1
Field2	DATE	description for field 2
Field 3 and a bit  DECIMAL	fooobaa for the win
Field 4 and a bit, STRING,	more text	, and more
Field 5 and a bit,STRING,desc
 any, ENUM hfsjkhs jhdjsah jsah d
     */

    const atoms = _.split(line, /[,\W]+/);
    if (atoms.length > 2) {
        const words = _.chain(atoms)
            .map(w => w.trim())
            .filter(w => w.length > 0)
            .value();
        const typePos = _.findIndex(words, w => _.includes(allowedTypes, w));
        const name =_.join(_.take(words, typePos), " ");
        const type = words[typePos];
        const description = _.join(_.drop(words, typePos + 1), " ");
        return {
            name,
            type,
            description,
            position
        };
    }
}




function parseFieldLine(line = [], position) {
    const fieldData = {
        field: attemptSplit(line, position),
        errors: []
    };
    if (! fieldData.field) {
        fieldData.errors.push('Make sure all three columns: Name, Type and Description are populated');
    } else {
        if (_.isEmpty(fieldData.field.name)) {
            fieldData.errors.push('Name must be defined');
        }

        if (_.isEmpty(fieldData.field.type)) {
            fieldData.errors.push('Type must be defined');
        }

        if (_.isEmpty(fieldData.field.description)) {
            fieldData.errors.push('Description must be defined');
        }

        if (! _.includes(allowedTypes, fieldData.field.type)) {
            fieldData.errors.push('Type is invalid');
        }
    }

    return fieldData;
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.cancel = () => invokeFunction(vm.onCancel);

    vm.preview = () => {
        vm.specDefFields.hasErrors = false;
        vm.specDefFields.parsedData = [];

        const lines = _.chain(vm.specDefFields.rawData)
            .split('\n')
            .value();

        vm.specDefFields.parsedData = _.map(lines, (line, index) => {
            const fieldData = parseFieldLine(line, index + 1);
            if (fieldData.errors.length > 0) {
                vm.specDefFields.hasErrors = true;
            }
            return fieldData;
        });
    };

    vm.onSelectorEntitySelect = (item, itemId) => {
        const match = _.find(vm.specDefFields.parsedData, ['field.position', itemId]);
        if(match) {
            match.field.logicalDataElementId = item.id;
            console.log('match: ', match)
        }
        console.log('fields: ', vm.specDefFields)
    };

    vm.submit = () => {
        vm.specDefinition.fields = _.map(vm.specDefFields.parsedData, 'field');
        invokeFunction(vm.onSubmit, vm.specDefinition);
    };
}


const component = {
    controller,
    template,
    bindings
};


export default component;