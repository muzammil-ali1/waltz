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

package com.khartec.waltz.service.measurable;

import com.khartec.waltz.common.DateTimeUtilities;
import com.khartec.waltz.common.StringUtilities;
import com.khartec.waltz.data.EntityReferenceNameResolver;
import com.khartec.waltz.data.measurable.MeasurableDao;
import com.khartec.waltz.data.measurable.MeasurableIdSelectorFactory;
import com.khartec.waltz.data.measurable.search.MeasurableSearchDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.measurable.Measurable;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static java.lang.String.format;
import static java.util.Optional.ofNullable;


@Service
public class MeasurableService {

    private final MeasurableDao measurableDao;
    private final MeasurableIdSelectorFactory measurableIdSelectorFactory = new MeasurableIdSelectorFactory();
    private final MeasurableSearchDao measurableSearchDao;
    private final ChangeLogService changeLogService;
    private final EntityReferenceNameResolver nameResolver;


    @Autowired
    public MeasurableService(MeasurableDao measurableDao,
                             MeasurableSearchDao measurableSearchDao,
                             EntityReferenceNameResolver nameResolver,
                             ChangeLogService changeLogService) {
        checkNotNull(measurableDao, "measurableDao cannot be null");
        checkNotNull(measurableSearchDao, "measurableSearchDao cannot be null");
        checkNotNull(nameResolver, "nameResolver cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.measurableDao = measurableDao;
        this.measurableSearchDao = measurableSearchDao;
        this.nameResolver = nameResolver;
        this.changeLogService = changeLogService;
    }


    public List<Measurable> findAll() {
        return measurableDao.findAll();
    }


    public List<Measurable> findByMeasurableIdSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = measurableIdSelectorFactory.apply(options);
        return measurableDao.findByMeasurableIdSelector(selector);
    }


    public List<Measurable> findByCategoryId(Long categoryId) {
        return measurableDao.findByCategoryId(categoryId);
    }


    public Map<String, Long> findExternalIdToIdMapByCategoryId(Long categoryId) {
        checkNotNull(categoryId, "categoryId cannot be null");

        return measurableDao.findExternalIdToIdMapByCategoryId(categoryId);
    }


    public Collection<Measurable> search(String query) {
        if (StringUtilities.isEmpty(query)) {
            return Collections.emptyList();
        }
        return search(EntitySearchOptions.mkForEntity(EntityKind.MEASURABLE, query));
    }


    public Collection<Measurable> search(EntitySearchOptions options) {
        return measurableSearchDao.search(options);
    }


    public Collection<Measurable> findByExternalId(String extId) {
        return measurableDao.findByExternalId(extId);
    }


    public Measurable getById(long id) {
        return measurableDao.getById(id);
    }


    public Collection<Measurable> findByOrgUnitId(Long id) {
        return measurableDao.findByOrgUnitId(id);
    }


    public boolean updateConcreteFlag(Long id, boolean newValue, String userId) {
        logUpdate(id, "concrete flag", Boolean.toString(newValue), m -> Optional.of(Boolean.toString(m.concrete())), userId);

        return measurableDao.updateConcreteFlag(id, newValue, userId);
    }


    public boolean updateName(long id, String newValue, String userId) {
        logUpdate(id, "name", newValue, m -> ofNullable(m.name()), userId);
        return measurableDao.updateName(id, newValue, userId);
    }


    public boolean updateDescription(long id, String newValue, String userId) {
        logUpdate(id, "description", newValue, m -> ofNullable(m.description()), userId);
        return measurableDao.updateDescription(id, newValue, userId);
    }


    public boolean updateExternalId(long id, String newValue, String userId) {
        logUpdate(id, "externalId", newValue, ExternalIdProvider::externalId, userId);
        return measurableDao.updateExternalId(id, newValue, userId);
    }


    public boolean create(Measurable measurable, String userId) {
        return measurableDao.create(measurable);
    }


    public int deleteByIdSelector(IdSelectionOptions selectionOptions) {
        Select<Record1<Long>> selector = measurableIdSelectorFactory
                .apply(selectionOptions);
        return measurableDao
                .deleteByIdSelector(selector);
    }


    /**
     * Changes the parentId of the given measurable to the new parent specified
     * by destinationId.  If destination id is null the measurable will be a new
     * root node in the tree.
     *
     * @param measurableId  measurable id of item to move
     * @param destinationId new parent id (or null if root)
     * @param userId        who initiated this move
     */
    public boolean updateParentId(Long measurableId, Long destinationId, String userId) {
        checkNotNull(measurableId, "Cannot updateParentId a measurable with a null id");

        writeAuditMessage(
                measurableId,
                userId,
                format("Measurable: [%s] moved to new parent: [%s]",
                        resolveName(measurableId),
                        destinationId == null
                                ? "<root of tree>"
                                : resolveName(destinationId)));

        return measurableDao.updateParentId(measurableId, destinationId, userId);
    }


    // --- helpers ---

    private void logUpdate(long id, String valueName, String newValue, Function<Measurable, Optional<String>> valueExtractor, String userId) {
        String existingValue = ofNullable(measurableDao.getById(id))
                .flatMap(valueExtractor)
                .orElse("<null>");

        writeAuditMessage(
                id,
                userId,
                format("Measurable: [%s] %s updated, from: [%s] to: [%s]",
                        resolveName(id),
                        valueName,
                        existingValue,
                        newValue));
    }


    private String resolveName(long id) {
        return nameResolver
                .resolve(mkRef(EntityKind.MEASURABLE, id))
                .flatMap(EntityReference::name)
                .orElse("UNKNOWN");
    }


    private void writeAuditMessage(Long measurableId, String userId, String msg) {
        changeLogService.write(ImmutableChangeLog.builder()
                .severity(Severity.INFORMATION)
                .userId(userId)
                .operation(Operation.UPDATE)
                .parentReference(mkRef(EntityKind.MEASURABLE, measurableId))
                .createdAt(DateTimeUtilities.nowUtc())
                .message(msg)
                .build());
    }
}