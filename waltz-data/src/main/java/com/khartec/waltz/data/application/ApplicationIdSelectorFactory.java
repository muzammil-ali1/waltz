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

package com.khartec.waltz.data.application;

import com.khartec.waltz.data.data_type.DataTypeIdSelectorFactory;
import com.khartec.waltz.data.measurable.MeasurableIdSelectorFactory;
import com.khartec.waltz.data.orgunit.OrganisationalUnitIdSelectorFactory;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.ImmutableIdSelectionOptions;
import com.khartec.waltz.schema.tables.*;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;
import static com.khartec.waltz.data.SelectorUtilities.ensureScopeIsExact;
import static com.khartec.waltz.data.SelectorUtilities.mkApplicationConditions;
import static com.khartec.waltz.data.logical_flow.LogicalFlowDao.LOGICAL_NOT_REMOVED;
import static com.khartec.waltz.model.EntityLifecycleStatus.REMOVED;
import static com.khartec.waltz.model.HierarchyQueryScope.EXACT;
import static com.khartec.waltz.schema.Tables.*;
import static com.khartec.waltz.schema.tables.Application.APPLICATION;
import static com.khartec.waltz.schema.tables.ApplicationGroupEntry.APPLICATION_GROUP_ENTRY;
import static com.khartec.waltz.schema.tables.EntityRelationship.ENTITY_RELATIONSHIP;
import static com.khartec.waltz.schema.tables.FlowDiagramEntity.FLOW_DIAGRAM_ENTITY;
import static com.khartec.waltz.schema.tables.Involvement.INVOLVEMENT;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static com.khartec.waltz.schema.tables.LogicalFlowDecorator.LOGICAL_FLOW_DECORATOR;
import static com.khartec.waltz.schema.tables.MeasurableRating.MEASURABLE_RATING;
import static com.khartec.waltz.schema.tables.Person.PERSON;
import static com.khartec.waltz.schema.tables.PersonHierarchy.PERSON_HIERARCHY;

@Service
public class ApplicationIdSelectorFactory implements Function<IdSelectionOptions, Select<Record1<Long>>> {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationIdSelectorFactory.class);

    private static final DataTypeIdSelectorFactory dataTypeIdSelectorFactory = new DataTypeIdSelectorFactory();
    private static final MeasurableIdSelectorFactory measurableIdSelectorFactory = new MeasurableIdSelectorFactory();
    private static final OrganisationalUnitIdSelectorFactory orgUnitIdSelectorFactory = new OrganisationalUnitIdSelectorFactory();

    private static final FlowDiagramEntity flowDiagram = FLOW_DIAGRAM_ENTITY.as("fd");
    private static final Involvement involvement = INVOLVEMENT.as("inv");
    private static final LogicalFlow logicalFlow = LOGICAL_FLOW.as("lf");
    private static final MeasurableRating measurableRating = MEASURABLE_RATING.as("mr");
    private static final Person person = PERSON.as("p");
    private static final PersonHierarchy personHierarchy = PERSON_HIERARCHY.as("ph");


    public Select<Record1<Long>> apply(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        EntityReference ref = options.entityReference();
        switch (ref.kind()) {
            case ACTOR:
                return mkForActor(options);
            case APP_GROUP:
                return mkForAppGroup(options);
            case APPLICATION:
                return mkForApplication(options);
            case CHANGE_INITIATIVE:
                return mkForEntityRelationship(options);
            case DATA_TYPE:
                return mkForDataType(options);
            case FLOW_DIAGRAM:
                return mkForFlowDiagram(options);
            case LICENCE:
                return mkForLicence(options);
            case MEASURABLE:
                return mkForMeasurable(options);
            case SCENARIO:
                return mkForScenario(options);
            case SERVER:
                return mkForServer(options);
            case ORG_UNIT:
                return mkForOrgUnit(options);
            case PERSON:
                return mkForPerson(options);
            case SOFTWARE:
                return mkForSoftwarePackage(options);
            case SOFTWARE_VERSION:
                return mkForSoftwareVersion(options);
            case TAG:
                return mkForTag(options);
            default:
                throw new IllegalArgumentException("Cannot create selector for entity kind: " + ref.kind());
        }
    }


    private Select<Record1<Long>> mkForTag(IdSelectionOptions options) {
        return DSL.select(TAG_USAGE.ENTITY_ID)
                .from(TAG_USAGE)
                .innerJoin(APPLICATION)
                .on(APPLICATION.ID.eq(TAG_USAGE.ENTITY_ID))
                .where(TAG_USAGE.TAG_ID.eq(options.entityReference().id()))
                .and(TAG_USAGE.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(mkApplicationConditions(options));
    }


    private Select<Record1<Long>> mkForSoftwarePackage(IdSelectionOptions options) {
        ensureScopeIsExact(options);

        Condition applicationConditions = mkApplicationConditions(options);
        return DSL
                .selectDistinct(SOFTWARE_USAGE.APPLICATION_ID)
                .from(SOFTWARE_USAGE)
                .innerJoin(APPLICATION)
                    .on(APPLICATION.ID.eq(SOFTWARE_USAGE.APPLICATION_ID))
                .innerJoin(SOFTWARE_VERSION)
                    .on(SOFTWARE_VERSION.ID.eq(SOFTWARE_USAGE.SOFTWARE_VERSION_ID))
                .where(SOFTWARE_VERSION.SOFTWARE_PACKAGE_ID.eq(options.entityReference().id()))
                .and(applicationConditions);
    }


    private Select<Record1<Long>> mkForSoftwareVersion(IdSelectionOptions options) {
        ensureScopeIsExact(options);

        Condition applicationConditions = mkApplicationConditions(options);
        return DSL
                .selectDistinct(SOFTWARE_USAGE.APPLICATION_ID)
                .from(SOFTWARE_USAGE)
                .innerJoin(APPLICATION)
                .on(APPLICATION.ID.eq(SOFTWARE_USAGE.APPLICATION_ID))
                .where(SOFTWARE_USAGE.SOFTWARE_VERSION_ID.eq(options.entityReference().id()))
                .and(applicationConditions);
    }


    private Select<Record1<Long>> mkForLicence(IdSelectionOptions options) {
        ensureScopeIsExact(options);

        Condition applicationConditions = mkApplicationConditions(options);
        return DSL
                .selectDistinct(SOFTWARE_USAGE.APPLICATION_ID)
                .from(SOFTWARE_USAGE)
                .innerJoin(APPLICATION)
                    .on(APPLICATION.ID.eq(SOFTWARE_USAGE.APPLICATION_ID))
                .innerJoin(SOFTWARE_VERSION_LICENCE)
                    .on(SOFTWARE_VERSION_LICENCE.SOFTWARE_VERSION_ID.eq(SOFTWARE_USAGE.SOFTWARE_VERSION_ID))
                .where(SOFTWARE_VERSION_LICENCE.LICENCE_ID.eq(options.entityReference().id()))
                .and(applicationConditions);
    }


    private Select<Record1<Long>> mkForScenario(IdSelectionOptions options) {
        ensureScopeIsExact(options);

        Condition applicationConditions = mkApplicationConditions(options);
        return DSL
                .selectDistinct(SCENARIO_RATING_ITEM.DOMAIN_ITEM_ID)
                .from(SCENARIO_RATING_ITEM)
                .innerJoin(APPLICATION).on(APPLICATION.ID.eq(SCENARIO_RATING_ITEM.DOMAIN_ITEM_ID))
                .and(SCENARIO_RATING_ITEM.DOMAIN_ITEM_KIND.eq(EntityKind.APPLICATION.name()))
                .where(SCENARIO_RATING_ITEM.SCENARIO_ID.eq(options.entityReference().id()))
                .and(applicationConditions);
    }


    private Select<Record1<Long>> mkForServer(IdSelectionOptions options) {
        ensureScopeIsExact(options);

        Condition applicationConditions = mkApplicationConditions(options);

        return DSL.selectDistinct(SERVER_USAGE.ENTITY_ID)
                .from(SERVER_USAGE)
                .innerJoin(APPLICATION).on(APPLICATION.ID.eq(SERVER_USAGE.ENTITY_ID))
                .where(SERVER_USAGE.SERVER_ID.eq(options.entityReference().id()))
                .and(SERVER_USAGE.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(applicationConditions);
    }


    public static Select<Record1<Long>> mkForActor(IdSelectionOptions options) {
        ensureScopeIsExact(options);
        long actorId = options.entityReference().id();

        Condition applicationConditions = mkApplicationConditions(options);

        Select<Record1<Long>> sourceAppIds = DSL
                .select(logicalFlow.SOURCE_ENTITY_ID)
                .from(logicalFlow)
                .innerJoin(APPLICATION).on(APPLICATION.ID.eq(logicalFlow.SOURCE_ENTITY_ID))
                .where(logicalFlow.TARGET_ENTITY_ID.eq(actorId)
                        .and(logicalFlow.TARGET_ENTITY_KIND.eq(EntityKind.ACTOR.name()))
                        .and(logicalFlow.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                        .and(logicalFlow.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name())))
                        .and(applicationConditions);

        Select<Record1<Long>> targetAppIds = DSL.select(logicalFlow.TARGET_ENTITY_ID)
                .from(logicalFlow)
                .innerJoin(APPLICATION).on(APPLICATION.ID.eq(logicalFlow.TARGET_ENTITY_ID))
                .where(logicalFlow.SOURCE_ENTITY_ID.eq(actorId)
                        .and(logicalFlow.SOURCE_ENTITY_KIND.eq(EntityKind.ACTOR.name()))
                        .and(logicalFlow.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                        .and(logicalFlow.ENTITY_LIFECYCLE_STATUS.ne(REMOVED.name())))
                        .and(applicationConditions);

        return DSL.selectFrom(sourceAppIds
                .union(targetAppIds).asTable());
    }


    private Select<Record1<Long>> mkForFlowDiagram(IdSelectionOptions options) {
        ensureScopeIsExact(options);

        long diagramId = options.entityReference().id();

        Condition logicalFlowInClause = LOGICAL_FLOW.ID.in(DSL
                .select(FLOW_DIAGRAM_ENTITY.ENTITY_ID)
                .from(FLOW_DIAGRAM_ENTITY)
                .where(FLOW_DIAGRAM_ENTITY.ENTITY_KIND.eq(EntityKind.LOGICAL_DATA_FLOW.name())
                        .and(FLOW_DIAGRAM_ENTITY.DIAGRAM_ID.eq(diagramId))));

        Condition applicationConditions = DSL.trueCondition(); //mkApplicationConditions(options);

        SelectConditionStep<Record1<Long>> directlyReferencedApps = DSL
                .select(flowDiagram.ENTITY_ID)
                .from(flowDiagram)
                .innerJoin(APPLICATION)
                .on(APPLICATION.ID.eq(flowDiagram.ENTITY_ID))
                .where(flowDiagram.DIAGRAM_ID.eq(diagramId))
                .and(flowDiagram.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(applicationConditions);

        SelectConditionStep<Record1<Long>> appsViaSourcesOfFlows = DSL
                .select(LOGICAL_FLOW.SOURCE_ENTITY_ID)
                .from(LOGICAL_FLOW)
                .innerJoin(APPLICATION)
                .on(APPLICATION.ID.eq(LOGICAL_FLOW.SOURCE_ENTITY_ID))
                .where(logicalFlowInClause)
                .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(applicationConditions);

        SelectConditionStep<Record1<Long>> appsViaTargetsOfFlows = DSL
                .select(LOGICAL_FLOW.TARGET_ENTITY_ID)
                .from(LOGICAL_FLOW)
                .innerJoin(APPLICATION)
                .on(APPLICATION.ID.eq(LOGICAL_FLOW.TARGET_ENTITY_ID))
                .where(logicalFlowInClause)
                .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(applicationConditions);

        return DSL.selectFrom(
                directlyReferencedApps
                    .unionAll(appsViaSourcesOfFlows)
                    .unionAll(appsViaTargetsOfFlows).asTable());
    }


    private Select<Record1<Long>> mkForMeasurable(IdSelectionOptions options) {
        Select<Record1<Long>> measurableSelector = measurableIdSelectorFactory.apply(options);

        Condition applicationConditions = mkApplicationConditions(options);

        return DSL
                .select(measurableRating.ENTITY_ID)
                .from(measurableRating)
                .innerJoin(APPLICATION)
                    .on(APPLICATION.ID.eq(measurableRating.ENTITY_ID))
                .where(measurableRating.ENTITY_KIND.eq(DSL.val(EntityKind.APPLICATION.name())))
                .and(measurableRating.MEASURABLE_ID.in(measurableSelector))
                .and(applicationConditions);
    }


    private Select<Record1<Long>> mkForApplication(IdSelectionOptions options) {
        checkTrue(options.scope() == EXACT, "Can only create selector for exact matches if given an APPLICATION ref");
        return DSL.select(DSL.val(options.entityReference().id()));
    }


    private Select<Record1<Long>> mkForEntityRelationship(IdSelectionOptions options) {
        ensureScopeIsExact(options);

        Condition applicationConditions = mkApplicationConditions(options);

        Select<Record1<Long>> appToEntity = DSL.selectDistinct(ENTITY_RELATIONSHIP.ID_A)
                .from(ENTITY_RELATIONSHIP)
                .innerJoin(APPLICATION)
                    .on(APPLICATION.ID.eq(ENTITY_RELATIONSHIP.ID_A))
                .where(ENTITY_RELATIONSHIP.KIND_A.eq(EntityKind.APPLICATION.name()))
                .and(ENTITY_RELATIONSHIP.KIND_B.eq(options.entityReference().kind().name()))
                .and(ENTITY_RELATIONSHIP.ID_B.eq(options.entityReference().id()))
                .and(applicationConditions);

        Select<Record1<Long>> entityToApp = DSL.selectDistinct(ENTITY_RELATIONSHIP.ID_B)
                .from(ENTITY_RELATIONSHIP)
                .innerJoin(APPLICATION)
                    .on(APPLICATION.ID.eq(ENTITY_RELATIONSHIP.ID_B))
                .where(ENTITY_RELATIONSHIP.KIND_B.eq(EntityKind.APPLICATION.name()))
                .and(ENTITY_RELATIONSHIP.KIND_A.eq(options.entityReference().kind().name()))
                .and(ENTITY_RELATIONSHIP.ID_A.eq(options.entityReference().id()))
                .and(applicationConditions);


        return appToEntity
                .union(entityToApp);
    }


    private SelectConditionStep<Record1<Long>> mkForOrgUnit(IdSelectionOptions options) {

        ImmutableIdSelectionOptions ouSelectorOptions = ImmutableIdSelectionOptions.builder()
                .entityReference(options.entityReference())
                .scope(options.scope())
                .build();

        Select<Record1<Long>> ouSelector = orgUnitIdSelectorFactory.apply(ouSelectorOptions);

        Condition applicationConditions = mkApplicationConditions(options);

        return DSL
                .selectDistinct(APPLICATION.ID)
                .from(APPLICATION)
                .where(APPLICATION.ORGANISATIONAL_UNIT_ID.in(ouSelector))
                .and(applicationConditions);
    }


    public static SelectOrderByStep<Record1<Long>> mkForAppGroup(IdSelectionOptions options) {
        if (options.scope() != EXACT) {
            throw new UnsupportedOperationException(
                    "App Groups are not hierarchical therefore ignoring requested scope of: " + options.scope());
        }

        Condition applicationConditions = mkApplicationConditions(options);

        SelectConditionStep<Record1<Long>> associatedOrgUnits = DSL
                .selectDistinct(ENTITY_HIERARCHY.ID)
                .from(APPLICATION_GROUP_OU_ENTRY)
                .innerJoin(ENTITY_HIERARCHY)
                .on(ENTITY_HIERARCHY.ANCESTOR_ID.eq(APPLICATION_GROUP_OU_ENTRY.ORG_UNIT_ID)
                        .and(ENTITY_HIERARCHY.KIND.eq(EntityKind.ORG_UNIT.name())))
                .where(APPLICATION_GROUP_OU_ENTRY.GROUP_ID.eq(options.entityReference().id()));

        SelectConditionStep<Record1<Long>> applicationIdsFromAssociatedOrgUnits = DSL
                .select(APPLICATION.ID)
                .from(APPLICATION)
                .innerJoin(ORGANISATIONAL_UNIT)
                .on(APPLICATION.ORGANISATIONAL_UNIT_ID.eq(ORGANISATIONAL_UNIT.ID))
                .where(ORGANISATIONAL_UNIT.ID.in(associatedOrgUnits));

        SelectConditionStep<Record1<Long>> directApps = DSL
                .select(APPLICATION_GROUP_ENTRY.APPLICATION_ID)
                .from(APPLICATION_GROUP_ENTRY)
                .where(APPLICATION_GROUP_ENTRY.GROUP_ID.eq(options.entityReference().id()));

        SelectWhereStep<Record1<Long>> appIds = DSL
                .selectFrom(directApps
                        .union(applicationIdsFromAssociatedOrgUnits)
                        .asTable());

        return DSL
                .select(APPLICATION.ID)
                .from(APPLICATION)
                .where(APPLICATION.ID.in(appIds))
                .and(applicationConditions);
    }


    private Select<Record1<Long>> mkForPerson(IdSelectionOptions options) {
        switch (options.scope()) {
            case EXACT:
                return mkForSinglePerson(options);
            case CHILDREN:
                return mkForPersonReportees(options);
            default:
                throw new UnsupportedOperationException(
                        "Querying for appIds of person using (scope: '"
                                + options.scope()
                                + "') not supported");
        }
    }


    private Select<Record1<Long>> mkForPersonReportees(IdSelectionOptions options) {

        Select<Record1<String>> emp = DSL
                .select(person.EMPLOYEE_ID)
                .from(person)
                .where(person.ID.eq(options.entityReference().id()));

        SelectConditionStep<Record1<String>> reporteeIds = DSL
                .selectDistinct(personHierarchy.EMPLOYEE_ID)
                .from(personHierarchy)
                .where(personHierarchy.MANAGER_ID.eq(emp));

        Condition applicationConditions = mkApplicationConditions(options);
        Condition condition = involvement.ENTITY_KIND.eq(EntityKind.APPLICATION.name())
                .and(involvement.EMPLOYEE_ID.eq(emp)
                        .or(involvement.EMPLOYEE_ID.in(reporteeIds)))
                .and(applicationConditions);

        return DSL
                .selectDistinct(involvement.ENTITY_ID)
                .from(involvement)
                .innerJoin(APPLICATION)
                    .on(APPLICATION.ID.eq(involvement.ENTITY_ID))
                .where(condition);
    }


    private Select<Record1<Long>> mkForSinglePerson(IdSelectionOptions options) {

        Select<Record1<String>> employeeId = DSL
                .select(person.EMPLOYEE_ID)
                .from(person)
                .where(person.ID.eq(options.entityReference().id()));

        Condition applicationConditions = mkApplicationConditions(options);
        return DSL
                .selectDistinct(involvement.ENTITY_ID)
                .from(involvement)
                .innerJoin(APPLICATION)
                    .on(APPLICATION.ID.eq(involvement.ENTITY_ID))
                .where(involvement.ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                .and(involvement.EMPLOYEE_ID.eq(employeeId))
                .and(applicationConditions);
    }


    private Select<Record1<Long>> mkForDataType(IdSelectionOptions options) {
        Select<Record1<Long>> dataTypeSelector = dataTypeIdSelectorFactory.apply(options);

        Condition condition = LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_ID.in(dataTypeSelector)
                .and(LOGICAL_FLOW_DECORATOR.DECORATOR_ENTITY_KIND.eq(EntityKind.DATA_TYPE.name()));

        Field<Long> appId = DSL.field("app_id", Long.class);

        Condition applicationConditions = mkApplicationConditions(options);

        SelectConditionStep<Record1<Long>> sources = selectLogicalFlowAppsByDataType(
                LOGICAL_FLOW.SOURCE_ENTITY_ID.as(appId),
                condition
                        .and(LOGICAL_FLOW.SOURCE_ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                        .and(applicationConditions),
                LOGICAL_FLOW.SOURCE_ENTITY_ID);

        SelectConditionStep<Record1<Long>> targets = selectLogicalFlowAppsByDataType(
                LOGICAL_FLOW.TARGET_ENTITY_ID.as(appId),
                condition
                        .and(LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(EntityKind.APPLICATION.name()))
                        .and(applicationConditions),
                LOGICAL_FLOW.TARGET_ENTITY_ID);

        return DSL
                .selectDistinct(appId)
                .from(sources.union(targets).asTable());
    }


    private SelectConditionStep<Record1<Long>> selectLogicalFlowAppsByDataType(Field<Long> appField, Condition condition, Field<Long> joinField) {
        return DSL
                .select(appField)
                .from(LOGICAL_FLOW)
                .innerJoin(LOGICAL_FLOW_DECORATOR)
                    .on(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID.eq(LOGICAL_FLOW.ID))
                .innerJoin(APPLICATION)
                    .on(APPLICATION.ID.eq(joinField))
                .where(condition)
                .and(LOGICAL_NOT_REMOVED);
    }

}