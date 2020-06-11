package com.khartec.waltz.jobs.tools.roadmaps;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.common.LoggingUtilities;
import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.model.AxisOrientation;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.schema.tables.*;
import com.khartec.waltz.schema.tables.records.ScenarioRatingItemRecord;
import com.khartec.waltz.service.DIConfiguration;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.schema.Tables.*;
import static java.lang.String.format;
import static java.util.stream.Collectors.collectingAndThen;

/**
 * This tool will take a roadmap with some predefined scenarios (with effective dates and
 * col/row axes defined).
 *
 * It will then populate (destructively) those scenarios with a projection of how the apps
 * will look, taking into account app decomms and new app commissions.
 *
 */
public class ScenarioPopulator {

    private static final MeasurableCategory mc = MEASURABLE_CATEGORY.as("mc");
    private static final Roadmap road = ROADMAP.as("road");
    private static final Scenario s = SCENARIO.as("s");
    private static final ScenarioAxisItem cai = SCENARIO_AXIS_ITEM.as("cai");
    private static final ScenarioAxisItem rai = SCENARIO_AXIS_ITEM.as("rai");
    private static final ScenarioRatingItem sri = SCENARIO_RATING_ITEM.as("sri");
    private static final MeasurableRating mr = MEASURABLE_RATING.as("mr");
    private static final MeasurableRatingPlannedDecommission pd = MEASURABLE_RATING_PLANNED_DECOMMISSION.as("pd");
    private static final MeasurableRatingReplacement repl = MEASURABLE_RATING_REPLACEMENT.as("repl");

    /**
     * Calculates whether the current app is in scope for scenario
     */
    private static final Field<Boolean> inclCurrent = DSL
            .when(pd.PLANNED_DECOMMISSION_DATE.isNull(), true)  // no decomm therefore in scope
            .when(s.EFFECTIVE_DATE.lessOrEqual(pd.PLANNED_DECOMMISSION_DATE), true)
            .otherwise(false);

    /**
     * Calculates whether the replacement app is in scope for scenario
     */
    private static final Field<Boolean> inclReplacement = DSL
            .when(repl.PLANNED_COMMISSION_DATE.isNull(), false) // no replacement therefore not in scope
            .when(s.EFFECTIVE_DATE.greaterOrEqual(repl.PLANNED_COMMISSION_DATE), true)
            .otherwise(false);


    public static void main(String[] args) {
        LoggingUtilities.configureLogging();

        // these two parameters specify which roadmap to use and what set of applications to use in the population.
        long roadmapId = 62L;
        IdSelectionOptions appSelectionOptions = IdSelectionOptions.mkOpts(mkRef(EntityKind.APP_GROUP, 11261L));

        Select<Record1<Long>> appSelector = new ApplicationIdSelectorFactory().apply(appSelectionOptions);

        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        DSLContext dsl = ctx.getBean(DSLContext.class);


        dsl.transaction(context -> {
            DSLContext tx = context.dsl();
            Timestamp now = DateTimeUtilities.nowUtcTimestamp();

            scrub(tx, roadmapId);

            SelectConditionStep<Record> qry = tx
                    .select(road.NAME)
                    .select(mc.NAME)
                    .select(s.NAME, s.ID, s.EFFECTIVE_DATE)
                    .select(cai.DOMAIN_ITEM_ID, cai.DOMAIN_ITEM_KIND)
                    .select(rai.DOMAIN_ITEM_ID, rai.DOMAIN_ITEM_KIND)
                    .select(mr.ENTITY_ID, mr.ENTITY_KIND, mr.RATING)
                    .select(pd.PLANNED_DECOMMISSION_DATE)
                    .select(repl.ENTITY_ID, repl.ENTITY_KIND, repl.PLANNED_COMMISSION_DATE)
                    .select(inclCurrent)
                    .select(inclReplacement)
                    .from(road)
                    .innerJoin(mc).on(mc.ID.eq(road.ROW_TYPE_ID))
                    .innerJoin(s).on(s.ROADMAP_ID.eq(road.ID))
                    .innerJoin(cai)
                        .on(cai.SCENARIO_ID.eq(s.ID))
                        .and(cai.ORIENTATION.eq(AxisOrientation.COLUMN.name()))
                    .innerJoin(rai)
                        .on(rai.SCENARIO_ID.eq(s.ID))
                        .and(rai.ORIENTATION.eq(AxisOrientation.ROW.name()))
                    .innerJoin(mr).on(mr.MEASURABLE_ID.eq(rai.DOMAIN_ITEM_ID))
                    .leftJoin(pd)
                        .on(pd.ENTITY_ID.eq(mr.ENTITY_ID))
                        .and(pd.ENTITY_KIND.eq(mr.ENTITY_KIND))
                        .and(mr.MEASURABLE_ID.eq(pd.MEASURABLE_ID))
                    .leftJoin(repl).on(pd.ID.eq(repl.DECOMMISSION_ID))
                    .where(road.ID.eq(roadmapId))
                    .and(mr.ENTITY_ID.in(appSelector))
                    .and(mr.ENTITY_KIND.eq(EntityKind.APPLICATION.name()));


            int[] insertResult = qry.fetch()
                    .stream()
                    .flatMap(r -> mkRecordsFromRow(tx, r, now))
                    .collect(collectingAndThen(
                            Collectors.toSet(),
                            tx::batchInsert))
                    .execute();


        });
    }


    /**
     * Each row may potentially yeild up to two records to be inserted:
     * <ul>
     *     <li>Current app (if scenario is before decomm date)</li>
     *     <li>Replacement app (if after scenario effective date)</li>
     * </ul>
     *
     * Note: We rely upon the equivalence of records to prevent duplicates (hence passing
     * in a single copy of now).
     *
     * @param tx  Transactional DSL context
     * @param r  Row from the initial query
     * @param now  timestamp representing now
     * @return  Stream of 0-2 records
     */
    private static Stream<ScenarioRatingItemRecord> mkRecordsFromRow(DSLContext tx,
                                                                     Record r,
                                                                     Timestamp now) {

        Stream.Builder<ScenarioRatingItemRecord> sbuilder = Stream.builder();

        if (r.get(inclCurrent)) {
            ScenarioRatingItemRecord item = mkBaseRecord(tx, r, now);
            item.setDomainItemKind(r.get(mr.ENTITY_KIND));
            item.setDomainItemId(r.get(mr.ENTITY_ID));
            item.setRating(r.get(mr.RATING));

            item.setDescription(mkDecomMessage(r));
            sbuilder.add(item);
        }

        if (r.get(inclReplacement)) {
            ScenarioRatingItemRecord item = mkBaseRecord(tx, r, now);
            item.setDomainItemKind(r.get(repl.ENTITY_KIND));
            item.setDomainItemId(r.get(repl.ENTITY_ID));
            item.setRating("G");
            item.setDescription(mkCommMessage(r));

            sbuilder.add(item);
        }

        return sbuilder.build();
    }


    /**
     * Generates a simple message to describe an app decomm if appropriate
     * @param r   The query result row
     * @return  String representing a decomm message (empty string if none)
     */
    private static String mkDecomMessage(Record r) {
        Date decommDate = r.get(pd.PLANNED_DECOMMISSION_DATE);
        return decommDate == null
                ? ""
                : format("%s is being decommissioned on: %s",
                    r.get(mc.NAME),
                    decommDate);
    }


    /**
     * Generates a simple message to describe an app commission if appropriate
     * @param r   The query result row
     * @return  String representing a commission message (empty string if none)
     */
    private static String mkCommMessage(Record r) {
        Date commDate = r.get(repl.PLANNED_COMMISSION_DATE);
        return commDate == null
                ? ""
                : format("%s is being commissioned on: %s",
                    r.get(mc.NAME),
                    commDate);
    }


    /**
     * Creates a new scenario rating item record and populates the base fields
     * from the given query result row.
     *
     * @param tx  Transactional dsl context
     * @param r  Query result row
     * @param now  timestamp representing now
     * @return  Paritally completed ScenarioRatingItemRecord
     */
    private static ScenarioRatingItemRecord mkBaseRecord(DSLContext tx,
                                                         Record r,
                                                         Timestamp now) {
        ScenarioRatingItemRecord item = tx.newRecord(SCENARIO_RATING_ITEM);
        item.setScenarioId(r.get(s.ID));

        item.setRowKind(r.get(rai.DOMAIN_ITEM_KIND));
        item.setRowId(r.get(rai.DOMAIN_ITEM_ID));
        item.setColumnKind(r.get(cai.DOMAIN_ITEM_KIND));
        item.setColumnId(r.get(cai.DOMAIN_ITEM_ID));

        item.setLastUpdatedAt(now);
        item.setLastUpdatedBy("admin");
        return item;
    }


    /**
     * Deletes all scenario rating items for all scenarios belonging to the
     * given roadmap.
     *
     * @param tx  Transactional dsl context
     * @param roadmapId  roadmap whose scenarios we want to scrub
     */
    private static void scrub(DSLContext tx, long roadmapId) {
        int rmResult = tx
                .deleteFrom(sri)
                .where(sri.SCENARIO_ID.in(
                        DSL.select(s.ID)
                                .from(s)
                                .where(s.ROADMAP_ID.eq(roadmapId))))
                .execute();
    }


}
