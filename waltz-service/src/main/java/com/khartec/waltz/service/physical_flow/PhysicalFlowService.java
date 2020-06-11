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

package com.khartec.waltz.service.physical_flow;

import com.khartec.waltz.data.physical_flow.PhysicalFlowDao;
import com.khartec.waltz.data.physical_flow.PhysicalFlowIdSelectorFactory;
import com.khartec.waltz.data.physical_flow.PhysicalFlowSearchDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.command.CommandOutcome;
import com.khartec.waltz.model.external_identifier.ExternalIdentifier;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.model.physical_flow.*;
import com.khartec.waltz.model.physical_specification.ImmutablePhysicalSpecification;
import com.khartec.waltz.model.physical_specification.ImmutablePhysicalSpecificationDeleteCommand;
import com.khartec.waltz.model.physical_specification.PhysicalSpecification;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.external_identifier.ExternalIdentifierService;
import com.khartec.waltz.service.logical_flow.LogicalFlowService;
import com.khartec.waltz.service.physical_specification.PhysicalSpecificationService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtc;
import static com.khartec.waltz.common.StringUtilities.isEmpty;
import static com.khartec.waltz.common.StringUtilities.mkSafe;
import static com.khartec.waltz.model.EntityKind.PHYSICAL_FLOW;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static java.lang.String.format;


@Service
public class PhysicalFlowService {

    private final PhysicalFlowDao physicalFlowDao;
    private final PhysicalSpecificationService physicalSpecificationService;
    private final ChangeLogService changeLogService;
    private final LogicalFlowService logicalFlowService;
    private final PhysicalFlowSearchDao searchDao;
    private final ExternalIdentifierService externalIdentifierService;
    private final PhysicalFlowIdSelectorFactory physicalFlowIdSelectorFactory = new PhysicalFlowIdSelectorFactory();


    @Autowired
    public PhysicalFlowService(ChangeLogService changeLogService,
                               LogicalFlowService logicalFlowService,
                               PhysicalFlowDao physicalDataFlowDao,
                               PhysicalSpecificationService physicalSpecificationService,
                               PhysicalFlowSearchDao searchDao,
                               ExternalIdentifierService externalIdentifierService) {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(logicalFlowService, "logicalFlowService cannot be null");
        checkNotNull(physicalDataFlowDao, "physicalFlowDao cannot be null");
        checkNotNull(physicalSpecificationService, "physicalSpecificationService cannot be null");
        checkNotNull(searchDao, "searchDao cannot be null");

        this.changeLogService = changeLogService;
        this.logicalFlowService = logicalFlowService;
        this.physicalFlowDao = physicalDataFlowDao;
        this.physicalSpecificationService = physicalSpecificationService;
        this.searchDao = searchDao;
        this.externalIdentifierService = externalIdentifierService;
    }


    public List<PhysicalFlow> findByEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return physicalFlowDao.findByEntityReference(ref);
    }


    public List<PhysicalFlow> findByProducerEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return physicalFlowDao.findByProducer(ref);
    }


    public List<PhysicalFlow> findByConsumerEntityReference(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return physicalFlowDao.findByConsumer(ref);
    }


    public List<PhysicalFlow> findBySpecificationId(long specificationId) {
        return physicalFlowDao.findBySpecificationId(specificationId);
    }


    public Collection<PhysicalFlow> findBySelector(IdSelectionOptions idSelectionOptions) {
        Select<Record1<Long>> selector = physicalFlowIdSelectorFactory.apply(idSelectionOptions);
        return physicalFlowDao.findBySelector(selector);
    }


    public PhysicalFlow getById(long id) {
        return physicalFlowDao.getById(id);
    }


    public List<PhysicalFlow> findByExternalId(String externalId) {
        return physicalFlowDao.findByExternalId(externalId);
    }


    public boolean merge(long fromId, long toId, String username) {
        EntityReference toRef = mkRef(PHYSICAL_FLOW, toId);
        EntityReference fromRef = mkRef(PHYSICAL_FLOW, fromId);

        int moveCount = externalIdentifierService.merge(fromRef, toRef);

        PhysicalFlow sourcePhysicalFlow = physicalFlowDao.getById(fromRef.id());
        copyExternalIdFromFlowAndSpecification(username, toRef, sourcePhysicalFlow);

        int updateStatus = physicalFlowDao.updateEntityLifecycleStatus(fromId, EntityLifecycleStatus.REMOVED);

        physicalSpecificationService.markRemovedIfUnused(
                ImmutablePhysicalSpecificationDeleteCommand
                        .builder()
                        .specificationId(sourcePhysicalFlow.specificationId())
                        .build(),
                username);

        if(updateStatus > 0) {
            String postamble = format(
                    "Merged physical flow %s to: %s",
                    fromRef.id(),
                    toRef.id());
            changeLogService.writeChangeLogEntries(
                    toRef,
                    username,
                    postamble,
                    Operation.UPDATE);
        }
        return updateStatus + moveCount > 0;
    }


    public PhysicalFlowDeleteCommandResponse delete(PhysicalFlowDeleteCommand command, String username) {
        checkNotNull(command, "command cannot be null");

        ImmutablePhysicalFlowDeleteCommandResponse.Builder responseBuilder = ImmutablePhysicalFlowDeleteCommandResponse.builder()
                .originalCommand(command)
                .entityReference(mkRef(PHYSICAL_FLOW, command.flowId()))
                .outcome(CommandOutcome.SUCCESS)
                .isSpecificationUnused(false)
                .isLastPhysicalFlow(false);

        PhysicalFlow physicalFlow = physicalFlowDao.getById(command.flowId());

        if (physicalFlow == null) {
            return responseBuilder
                    .outcome(CommandOutcome.FAILURE)
                    .message("Flow not found")
                    .build();
        } else {
            int deleteCount = physicalFlowDao.delete(command.flowId());

            if (deleteCount == 0) {
                return responseBuilder
                        .outcome(CommandOutcome.FAILURE)
                        .message("This flow cannot be deleted as it is being used in a lineage")
                        .build();
            } else {
                externalIdentifierService.delete(physicalFlow.entityReference());
                responseBuilder
                        .isSpecificationUnused(!physicalSpecificationService.isUsed(physicalFlow.specificationId()))
                        .isLastPhysicalFlow(!physicalFlowDao.hasPhysicalFlows(physicalFlow.logicalFlowId()));
            }

            changeLogService.writeChangeLogEntries(
                    physicalFlow,
                    username,
                    " removed",
                    Operation.REMOVE);
        }

        return responseBuilder.build();
    }


    public PhysicalFlowCreateCommandResponse create(PhysicalFlowCreateCommand command, String username) {
        checkNotNull(command, "command cannot be null");
        checkNotNull(username, "username cannot be null");

        //check we have a logical data flow
        ensureLogicalDataFlowExistsAndIsNotRemoved(command.logicalFlowId(), username);

        LocalDateTime now = nowUtc();
        long specId = command
                .specification()
                .id()
                .orElseGet(() -> physicalSpecificationService.create(ImmutablePhysicalSpecification
                        .copyOf(command.specification())
                        .withLastUpdatedBy(username)
                        .withLastUpdatedAt(now)
                        .withCreated(UserTimestamp.mkForUser(username, now))));

        PhysicalFlow flow = ImmutablePhysicalFlow.builder()
                .specificationId(specId)
                .basisOffset(command.flowAttributes().basisOffset())
                .frequency(command.flowAttributes().frequency())
                .transport(command.flowAttributes().transport())
                .criticality(command.flowAttributes().criticality())
                .description(mkSafe(command.flowAttributes().description()))
                .logicalFlowId(command.logicalFlowId())
                .lastUpdatedBy(username)
                .lastUpdatedAt(now)
                .created(UserTimestamp.mkForUser(username, now))
                .build();

        // ensure existing not in database
        List<PhysicalFlow> byAttributesAndSpecification = physicalFlowDao.findByAttributesAndSpecification(flow);
        if(byAttributesAndSpecification.size() > 0) {
            return ImmutablePhysicalFlowCreateCommandResponse.builder()
                    .originalCommand(command)
                    .outcome(CommandOutcome.FAILURE)
                    .message("Duplicate with existing flow")
                    .entityReference(mkRef(PHYSICAL_FLOW, byAttributesAndSpecification.get(0).id().get()))
                    .build();
        }

        long physicalFlowId = physicalFlowDao.create(flow);

        if(command.specification().isRemoved()) {
            physicalSpecificationService.makeActive(specId, username);
        }

        changeLogService.writeChangeLogEntries(
                ImmutablePhysicalFlow.copyOf(flow).withId(physicalFlowId),
                username,
                " created",
                Operation.ADD);

        return ImmutablePhysicalFlowCreateCommandResponse.builder()
                .originalCommand(command)
                .outcome(CommandOutcome.SUCCESS)
                .entityReference(mkRef(PHYSICAL_FLOW, physicalFlowId))
                .build();
    }


    public int updateSpecDefinitionId(String userName, long flowId, PhysicalFlowSpecDefinitionChangeCommand command) {
        checkNotNull(userName, "userName cannot be null");
        checkNotNull(command, "command cannot be null");

        int updateCount = physicalFlowDao.updateSpecDefinition(userName, flowId, command.newSpecDefinitionId());

        if (updateCount > 0) {
            String postamble = format(
                    "Physical flow id: %d specification definition id changed to: %d",
                    flowId,
                    command.newSpecDefinitionId());
            changeLogService.writeChangeLogEntries(
                    mkRef(PHYSICAL_FLOW, flowId),
                    userName,
                    postamble,
                    Operation.UPDATE);
        }

        return updateCount;
    }


    public Collection<EntityReference> searchReports(String query) {
        return searchDao.searchReports(query);
    }


    public int cleanupOrphans() {
        return physicalFlowDao.cleanupOrphans();
    }


    private void ensureLogicalDataFlowExistsAndIsNotRemoved(long logicalFlowId, String username) {
        LogicalFlow logicalFlow = logicalFlowService.getById(logicalFlowId);
        if (logicalFlow == null) {
            throw new IllegalArgumentException("Unknown logical flow: " + logicalFlowId);
        } else {
            if (logicalFlow.entityLifecycleStatus().equals(EntityLifecycleStatus.REMOVED)) {
                logicalFlowService.restoreFlow(logicalFlowId, username);
            }
        }
    }


    public int updateAttribute(String username, SetAttributeCommand command) {

        int rc = doUpdateAttribute(command);

        if (rc != 0) {
            String postamble = format("Updated attribute %s to %s", command.name(), command.value());
            changeLogService.writeChangeLogEntries(
                    command.entityReference(),
                    username,
                    postamble,
                    Operation.UPDATE);
        }

        return rc;
    }


    // -- HELPERS

    private int doUpdateAttribute(SetAttributeCommand command) {
        long flowId = command.entityReference().id();
        switch(command.name()) {
            case "criticality":
                return physicalFlowDao.updateCriticality(flowId, Criticality.valueOf(command.value()));
            case "frequency":
                return physicalFlowDao.updateFrequency(flowId, FrequencyKind.valueOf(command.value()));
            case "transport":
                return physicalFlowDao.updateTransport(flowId, command.value());
            case "basisOffset":
                return physicalFlowDao.updateBasisOffset(flowId, Integer.parseInt(command.value()));
            case "description":
                return physicalFlowDao.updateDescription(flowId, command.value());
            case "entity_lifecycle_status":
                return physicalFlowDao.updateEntityLifecycleStatus(flowId, EntityLifecycleStatus.valueOf(command.value()));
            default:
                String errMsg = format(
                        "Cannot update attribute %s on flow as unknown attribute name",
                        command.name());
                throw new UnsupportedOperationException(errMsg);
        }
    }


    private void copyExternalIdFromFlowAndSpecification(String username,
                                                        EntityReference toRef,
                                                        PhysicalFlow sourcePhysicalFlow) {
        PhysicalFlow targetPhysicalFlow = physicalFlowDao.getById(toRef.id());

        Set<String> externalIdentifiers =
                externalIdentifierService.findByEntityReference(toRef)
                        .stream()
                        .map(ExternalIdentifier::externalId)
                        .collect(Collectors.toSet());

        sourcePhysicalFlow
                .externalId()
                .filter(id -> !isEmpty(id))
                .ifPresent(sourceExtId -> {
                    if(isEmpty(targetPhysicalFlow.externalId())) {
                        physicalFlowDao.updateExternalId(toRef.id(), sourceExtId);
                    } else if(!externalIdentifiers.contains(sourceExtId)) {
                        externalIdentifierService.create(toRef, sourceExtId, username);
                        externalIdentifiers.add(sourceExtId);
                    }
                });

        PhysicalSpecification sourceSpec = physicalSpecificationService.getById(sourcePhysicalFlow.specificationId());

        sourceSpec
                .externalId()
                .filter(id -> !isEmpty(id))
                .ifPresent(sourceExtId -> {
                    PhysicalSpecification targetSpec = physicalSpecificationService
                            .getById(targetPhysicalFlow.specificationId());

                    if(isEmpty(targetSpec.externalId())) {
                        targetSpec.id()
                                .ifPresent(id ->
                                        physicalSpecificationService.updateExternalId(id, sourceExtId));
                    } else if(!externalIdentifiers.contains(sourceExtId)) {
                        externalIdentifierService.create(toRef, sourceExtId, username);
                    }
                });
    }
}
