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

package com.khartec.waltz.service.roadmap;

import com.khartec.waltz.data.entity_relationship.EntityRelationshipDao;
import com.khartec.waltz.data.roadmap.RoadmapDao;
import com.khartec.waltz.data.roadmap.RoadmapIdSelectorFactory;
import com.khartec.waltz.data.roadmap.RoadmapSearchDao;
import com.khartec.waltz.data.scenario.ScenarioDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.entity_relationship.EntityRelationship;
import com.khartec.waltz.model.entity_relationship.ImmutableEntityRelationship;
import com.khartec.waltz.model.entity_relationship.RelationshipKind;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.roadmap.Roadmap;
import com.khartec.waltz.model.roadmap.RoadmapAndScenarioOverview;
import com.khartec.waltz.model.roadmap.RoadmapCreateCommand;
import com.khartec.waltz.model.scenario.Scenario;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.service.roadmap.RoadmapUtilities.mkBasicLogEntry;
import static java.util.stream.Collectors.toList;

@Service
public class RoadmapService {

    private final RoadmapDao roadmapDao;
    private final RoadmapSearchDao roadmapSearchDao;
    private final ScenarioDao scenarioDao;
    private final RoadmapIdSelectorFactory roadmapIdSelectorFactory = new RoadmapIdSelectorFactory();
    private final ChangeLogService changeLogService;
    private final EntityRelationshipDao entityRelationshipDao;


    @Autowired
    public RoadmapService(RoadmapDao roadmapDao,
                          RoadmapSearchDao roadmapSearchDao,
                          ScenarioDao scenarioDao,
                          ChangeLogService changeLogService,
                          EntityRelationshipDao entityRelationshipDao) {
        checkNotNull(roadmapDao, "roadmapDao cannot be null");
        checkNotNull(roadmapSearchDao, "roadmapSearchDao cannot be null");
        checkNotNull(scenarioDao, "scenarioDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(entityRelationshipDao, "entityRelationshipDao cannot be null");
        this.roadmapDao = roadmapDao;
        this.roadmapSearchDao = roadmapSearchDao;
        this.scenarioDao = scenarioDao;
        this.changeLogService = changeLogService;
        this.entityRelationshipDao = entityRelationshipDao;
    }


    public Roadmap getById(long id) {
        return roadmapDao.getById(id);
    }


    public Long createRoadmap(RoadmapCreateCommand command, String userId) {
        long roadmapId = roadmapDao.createRoadmap(
                command.name(),
                command.ratingSchemeId(),
                command.columnType(),
                command.rowType(),
                userId);

        if (roadmapId > 0) {
            changeLogService.write(ImmutableChangeLog
                    .copyOf(mkBasicLogEntry(roadmapId, String.format("Created roadmap: %s", command.name()), userId))
                    .withOperation(Operation.ADD));
        }

        EntityRelationship reln = ImmutableEntityRelationship.builder()
                .a(command.linkedEntity())
                .b(mkRef(EntityKind.ROADMAP, roadmapId))
                .relationship(RelationshipKind.RELATES_TO.name())
                .lastUpdatedBy(userId)
                .build();

        entityRelationshipDao.create(reln);

        return roadmapId;

    }

    public Collection<Roadmap> findRoadmapsBySelector(IdSelectionOptions selectionOptions) {
        Select<Record1<Long>> selector = roadmapIdSelectorFactory.apply(selectionOptions);
        return roadmapDao.findRoadmapsBySelector(selector);
    }


    public Boolean updateDescription(long id, String newDescription, String userId) {
        Boolean result = roadmapDao.updateDescription(id, newDescription, userId);
        if (result) {
            writeLogEntriesForUpdate(id, "Updated Description", newDescription, userId);
        }
        return result;
    }


    public Boolean updateName(long id, String newName, String userId) {
        Boolean result = roadmapDao.updateName(id, newName, userId);
        if (result) {
            writeLogEntriesForUpdate(id, "Updated Name", newName, userId);
        }
        return result;
    }


    public Boolean updateLifecycleStatus(long id, EntityLifecycleStatus newStatus, String userId) {
        Boolean result = roadmapDao.updateLifecycleStatus(id, newStatus, userId);
        if (result) {
            writeLogEntriesForUpdate(id, "Updated Entity Lifecycle Status", newStatus.name(), userId);
        }
        return result;
    }


    public Scenario addScenario(long roadmapId, String name, String userId) {
        changeLogService.write(ImmutableChangeLog
                .copyOf(mkBasicLogEntry(roadmapId, String.format("Added scenario %s", name), userId))
                .withChildKind(EntityKind.SCENARIO)
                .withOperation(Operation.ADD));
        return scenarioDao.add(roadmapId, name, userId);
    }


    public Collection<RoadmapAndScenarioOverview> findAllRoadmapsAndScenarios() {
        return roadmapDao.findAllRoadmapsAndScenarios();
    }


    public Collection<RoadmapAndScenarioOverview> findRoadmapsAndScenariosByRatedEntity(EntityReference ratedEntity) {
        return roadmapDao.findRoadmapsAndScenariosByRatedEntity(ratedEntity);
    }


    public Collection<RoadmapAndScenarioOverview> findRoadmapsAndScenariosByFormalRelationship(EntityReference relatedEntity) {
        return roadmapDao.findRoadmapsAndScenariosByFormalRelationship(relatedEntity);
    }


    public List<EntityReference> search(String query) {
        List<Roadmap> roadmaps = search(EntitySearchOptions.mkForEntity(EntityKind.ROADMAP, query));
        return roadmaps.stream()
                .map(Roadmap::entityReference)
                .collect(toList());
    }


    public List<Roadmap> search(EntitySearchOptions options) {
        return roadmapSearchDao.search(options);
    }


    // -- helpers --

    private void writeLogEntriesForUpdate(long roadmapId, String desc, String newValue, String userId) {
        String message = String.format("%s: '%s'", desc, newValue);
        changeLogService.write(mkBasicLogEntry(roadmapId, message, userId));
    }

}
