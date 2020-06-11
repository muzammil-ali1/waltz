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

import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.data.InlineSelectFieldFactory;
import com.khartec.waltz.data.logical_flow.LogicalFlowDao;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.jooq.tools.json.ParseException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.CollectionUtilities.map;
import static com.khartec.waltz.common.ListUtilities.newArrayList;
import static com.khartec.waltz.common.MapUtilities.groupBy;
import static com.khartec.waltz.model.EntityKind.ACTOR;
import static com.khartec.waltz.model.EntityKind.APPLICATION;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.tables.LogicalFlow.LOGICAL_FLOW;
import static com.khartec.waltz.schema.tables.LogicalFlowDecorator.LOGICAL_FLOW_DECORATOR;


public class FlowLineageHarness {

    public static void main(String[] args) throws ParseException {

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);
        DSLContext dsl = ctx.getBean(DSLContext.class);

        LogicalFlowDao flowDao = ctx.getBean(LogicalFlowDao.class);

        EntityReference ref1 = mkRef(APPLICATION, 25662);
        EntityReference ref2 = mkRef(ACTOR, 10);
        EntityReference ref3 = mkRef(APPLICATION, 25612);

        findIncomingByRefs(dsl, SetUtilities.fromArray(ref1, ref2, ref3));


    }

    private static void findIncomingByRefs(DSLContext dsl, Set<EntityReference> entityReferences) {


        Map<EntityKind, Collection<EntityReference>> refsByKind = groupBy(ref -> ref.kind(), entityReferences);

        Condition anyTargetMatches = refsByKind
                .entrySet()
                .stream()
                .map(entry -> LOGICAL_FLOW.TARGET_ENTITY_KIND.eq(entry.getKey().name())
                        .and(LOGICAL_FLOW.TARGET_ENTITY_ID.in(map(entry.getValue(), ref -> ref.id()))))
                .collect(Collectors.reducing(DSL.falseCondition(), (acc, c) -> acc.or(c)));

        System.out.println(anyTargetMatches);


        Field<String> SOURCE_NAME_FIELD = InlineSelectFieldFactory.mkNameField(
                LOGICAL_FLOW.SOURCE_ENTITY_ID,
                LOGICAL_FLOW.SOURCE_ENTITY_KIND,
                newArrayList(EntityKind.APPLICATION, EntityKind.ACTOR));

        dsl.select(LOGICAL_FLOW.fields())
                .select(SOURCE_NAME_FIELD)
                .from(LOGICAL_FLOW)
                .where(anyTargetMatches.and(LogicalFlowDao.LOGICAL_NOT_REMOVED))
                .forEach(System.out::println);

        dsl.select()
                .from(LOGICAL_FLOW_DECORATOR)
                .innerJoin(LOGICAL_FLOW)
                    .on(LOGICAL_FLOW.ID.eq(LOGICAL_FLOW_DECORATOR.LOGICAL_FLOW_ID))
                .where(anyTargetMatches)
                .forEach(System.out::println);


    }

}
