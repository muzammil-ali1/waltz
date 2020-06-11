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

package com.khartec.waltz.jobs.harness;

import com.khartec.waltz.data.EntityReferenceNameResolver;
import com.khartec.waltz.data.app_group.AppGroupDao;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.complexity.ConnectionComplexityDao;
import com.khartec.waltz.data.data_type_usage.DataTypeUsageDao;
import com.khartec.waltz.data.entity_relationship.EntityRelationshipDao;
import com.khartec.waltz.data.involvement.InvolvementDao;
import com.khartec.waltz.data.licence.LicenceDao;
import com.khartec.waltz.data.logical_data_element.search.LogicalDataElementSearchDao;
import com.khartec.waltz.data.measurable.MeasurableDao;
import com.khartec.waltz.data.measurable.MeasurableIdSelectorFactory;
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.data.physical_flow.PhysicalFlowDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.LeveledEntityReference;
import com.khartec.waltz.model.app_group.AppGroup;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.logical_data_element.LogicalDataElement;
import com.khartec.waltz.model.measurable.Measurable;
import com.khartec.waltz.model.physical_flow.PhysicalFlow;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Map;

import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;

/**
 * This class is to do ad-hoc testing of union clauses in jOOQ.
 * We have had issues with MariaDB and the generated union statements.
 * This class simply exercises code which performs unions.
 */
public class UnionHarness {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        appGroupDao_findRelatedByApplicationId(ctx);
        appGroupDao_search(ctx);
        appIdSelectorFactory_mkForActor(ctx);
        appIdSelectorFactory_mkForAppGroup(ctx);
        appIdSelectorFactory_mkForDataType(ctx);
        appIdSelectorFactory_mkForFlowDiagram(ctx);
        connectionComplexityDao_findCounts(ctx);
        dataTypeUsageDao_recalculateForAllApplications(ctx);
        entityNameResolver_resolve(ctx);
        entityRelationshipDao_tallyRelationshipsInvolving(ctx);
        involvementDao_findAllApplicationsByEmployeeId(ctx);
        licenceDao_countApplications(ctx);
        logicalDataElementSearch_search(ctx);
        measurableIdSelectorFactory_mkForFlowDiagram(ctx);
        organisationalUnitDao_findImmediateHierarchy(ctx);
        physicalFlowDao_findByEntityRef(ctx);
    }


    private static void physicalFlowDao_findByEntityRef(ApplicationContext ctx) {
        List<PhysicalFlow> flows = ctx.getBean(PhysicalFlowDao.class).findByEntityReference(mkRef(EntityKind.APPLICATION, 12L));
        System.out.println(flows.size());
    }


    private static void organisationalUnitDao_findImmediateHierarchy(ApplicationContext ctx) {
        List<LeveledEntityReference> hier = ctx.getBean(OrganisationalUnitDao.class).findImmediateHierarchy(100L);
        System.out.println(hier.size());
    }


    private static void measurableIdSelectorFactory_mkForFlowDiagram(ApplicationContext ctx) {
        Select<Record1<Long>> selector = new MeasurableIdSelectorFactory().apply(mkOpts(mkRef(EntityKind.FLOW_DIAGRAM, 12L)));
        List<Measurable> measurables = ctx.getBean(MeasurableDao.class).findByMeasurableIdSelector(selector);
        System.out.println(measurables.size());
    }


    private static void licenceDao_countApplications(ApplicationContext ctx) {
        List<Tally<Long>> counts = ctx.getBean(LicenceDao.class).countApplications();
        System.out.println(counts);
    }


    private static void involvementDao_findAllApplicationsByEmployeeId(ApplicationContext ctx) {
        List<Application> apps = ctx.getBean(InvolvementDao.class).findAllApplicationsByEmployeeId("1234");
        System.out.println(apps.size());
    }


    private static void entityRelationshipDao_tallyRelationshipsInvolving(ApplicationContext ctx) {
        Map<EntityKind, Integer> relns = ctx.getBean(EntityRelationshipDao.class)
                .tallyRelationshipsInvolving(mkRef(EntityKind.APPLICATION, 12L));
        System.out.println(relns);
    }


    private static void dataTypeUsageDao_recalculateForAllApplications(ApplicationContext ctx) {
        boolean done = ctx.getBean(DataTypeUsageDao.class).recalculateForAllApplications();
        System.out.println(done);
    }


    private static void connectionComplexityDao_findCounts(ApplicationContext ctx) {
        List<Tally<Long>> counts = ctx.getBean(ConnectionComplexityDao.class).findCounts();
        System.out.println(counts);
    }


    private static void appIdSelectorFactory_mkForAppGroup(ApplicationContext ctx) {
        checkAppIdSelectorFatory(ctx, mkRef(EntityKind.APP_GROUP, 12));
    }


    private static void appIdSelectorFactory_mkForDataType(AnnotationConfigApplicationContext ctx) {
        checkAppIdSelectorFatory(ctx, mkRef(EntityKind.DATA_TYPE, 12));
    }


    private static void appIdSelectorFactory_mkForFlowDiagram(AnnotationConfigApplicationContext ctx) {
        checkAppIdSelectorFatory(ctx, mkRef(EntityKind.FLOW_DIAGRAM, 12));
    }


    private static void appIdSelectorFactory_mkForActor(AnnotationConfigApplicationContext ctx) {
        checkAppIdSelectorFatory(ctx, mkRef(EntityKind.ACTOR, 12));
    }


    private static void checkAppIdSelectorFatory(ApplicationContext ctx, EntityReference ref) {
        List<Application> byAppIdSelector = ctx.getBean(ApplicationDao.class)
                .findByAppIdSelector(new ApplicationIdSelectorFactory()
                        .apply(mkOpts(ref)));
        System.out.println(byAppIdSelector);
    }


    private static void logicalDataElementSearch_search(ApplicationContext ctx) {
        List<LogicalDataElement> xs = ctx
                .getBean(LogicalDataElementSearchDao.class)
                .search(EntitySearchOptions.mkForEntity(
                        EntityKind.LOGICAL_DATA_ELEMENT,
                        "test"));
        System.out.println(xs);
    }


    private static void entityNameResolver_resolve(ApplicationContext ctx) {
        ctx.getBean(EntityReferenceNameResolver.class)
                .resolve(mkRef(EntityKind.APPLICATION, 12L))
                .ifPresent(er -> System.out.println(er.name().orElse("?")));
    }


    private static void appGroupDao_search(ApplicationContext ctx) {
        List<AppGroup> xs = ctx.getBean(AppGroupDao.class)
                .search(EntitySearchOptions.mkForEntity(EntityKind.APP_GROUP, "test"));
        System.out.println(xs);
    }


    private static void appGroupDao_findRelatedByApplicationId(ApplicationContext ctx) {
        List<AppGroup> xs = ctx
                .getBean(AppGroupDao.class)
                .findRelatedByApplicationId(12L, "admin");
        System.out.println(xs);
    }

}
