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

package com.khartec.waltz.jobs.generators.stress;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.common.RandomUtilities;
import com.khartec.waltz.schema.tables.records.MeasurableRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.DSLContext;
import org.jooq.lambda.tuple.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static com.khartec.waltz.jobs.WaltzUtilities.getOrCreateMeasurableCategory;
import static com.khartec.waltz.schema.tables.Measurable.MEASURABLE;
import static org.jooq.lambda.tuple.Tuple.tuple;

public class MeasurableStressGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(MeasurableStressGenerator.class);


    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);

        LOG.info("Ensuring category exists");
        Long categoryId = getOrCreateMeasurableCategory(dsl, "STRESS", "Stress Test");

        LOG.info("Deleting existing records");
        dsl.deleteFrom(MEASURABLE)
                .where(MEASURABLE.MEASURABLE_CATEGORY_ID.eq(categoryId))
                .execute();

        LOG.info("Generating records");
        List<MeasurableRecord> records = mkNodes(1, "")
                .stream()
                .map(t -> {
                    MeasurableRecord mr = new MeasurableRecord();
                    mr.setExternalId(t.v1);
                    mr.setExternalParentId(t.v2);
                    mr.setDescription(t.v1);
                    mr.setName(t.v1);
                    mr.setConcrete(true);
                    mr.setLastUpdatedBy("admin");
                    mr.setProvenance("stress");
                    mr.setLastUpdatedAt(DateTimeUtilities.nowUtcTimestamp());
                    mr.setMeasurableCategoryId(categoryId);
                    return mr;
                })
                .collect(Collectors.toList());

        LOG.info("Inserting {} records into category {}", records.size(), categoryId);
        dsl.batchInsert(records)
                .execute();

        LOG.info("Done");

    }


    static Random rnd = RandomUtilities.getRandom();


    private static List<Tuple2<String, String>> mkNodes(int level, String path) {
        List<Tuple2<String, String>> ts = new ArrayList<>();

        int siblingCount = rnd.nextInt(2) + 4;

        for (int i = 1; i <= level; i++) {
            String id = path + (level > 1 ? "." : "") + i;
            ts.add(tuple(id,path));
            if (level < 8) {  // 7 == ~5K, 8 = 46K
                ts.addAll(mkNodes(level + 1, id));
            }
        }
        return ts;
    }
}
