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

import com.khartec.waltz.common.Aliases;
import com.khartec.waltz.common.MapUtilities;
import com.khartec.waltz.data.actor.ActorDao;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.data_type.DataTypeDao;
import com.khartec.waltz.data.logical_flow.LogicalFlowDao;
import com.khartec.waltz.data.physical_flow.PhysicalFlowDao;
import com.khartec.waltz.data.physical_specification.PhysicalSpecificationDao;
import com.khartec.waltz.model.Criticality;
import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.UserTimestamp;
import com.khartec.waltz.model.actor.Actor;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.command.CommandOutcome;
import com.khartec.waltz.model.datatype.DataType;
import com.khartec.waltz.model.datatype.DataTypeDecorator;
import com.khartec.waltz.model.enum_value.EnumValueKind;
import com.khartec.waltz.model.logical_flow.ImmutableLogicalFlow;
import com.khartec.waltz.model.logical_flow.LogicalFlow;
import com.khartec.waltz.model.physical_flow.*;
import com.khartec.waltz.model.physical_specification.DataFormatKind;
import com.khartec.waltz.model.physical_specification.ImmutablePhysicalSpecification;
import com.khartec.waltz.model.physical_specification.PhysicalSpecification;
import com.khartec.waltz.service.data_type.DataTypeDecoratorService;
import com.khartec.waltz.service.enum_value.EnumValueAliasService;
import com.khartec.waltz.service.physical_specification_data_type.PhysicalSpecDataTypeService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.DateTimeUtilities.nowUtc;
import static com.khartec.waltz.common.SetUtilities.fromArray;
import static com.khartec.waltz.common.StringUtilities.isEmpty;
import static com.khartec.waltz.common.StringUtilities.lower;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;


@Service
public class PhysicalFlowUploadService {

    private final ActorDao actorDao;
    private final ApplicationDao applicationDao;
    private final DataTypeDao dataTypeDao;
    private final LogicalFlowDao logicalFlowDao;
    private final DataTypeDecoratorService dataTypeDecoratorService;
    private final PhysicalFlowDao physicalFlowDao;
    private final PhysicalSpecificationDao physicalSpecificationDao;
    private final EnumValueAliasService enumValueAliasService;

    private final Pattern basisOffsetRegex = Pattern.compile("T?(?<offset>[\\+\\-]?\\d+)");


    public PhysicalFlowUploadService(ActorDao actorDao,
                                     ApplicationDao applicationDao,
                                     DataTypeDao dataTypeDao,
                                     LogicalFlowDao logicalFlowDao,
                                     DataTypeDecoratorService dataTypeDecoratorService,
                                     PhysicalFlowDao physicalFlowDao,
                                     PhysicalSpecificationDao physicalSpecificationDao,
                                     EnumValueAliasService enumValueAliasService) {
        checkNotNull(actorDao, "actorDao cannot be null");
        checkNotNull(applicationDao, "applicationDao cannot be null");
        checkNotNull(dataTypeDao, "dataTypeDao cannot be null");
        checkNotNull(dataTypeDecoratorService, "dataTypeDecoratorService cannot be null");
        checkNotNull(logicalFlowDao, "logicalFlowDao cannot be null");
        checkNotNull(physicalFlowDao, "physicalFlowDao cannot be null");
        checkNotNull(physicalSpecificationDao, "physicalSpecificationDao cannot be null");
        checkNotNull(enumValueAliasService, "enumValueAliasService cannot be null");
        this.actorDao = actorDao;
        this.applicationDao = applicationDao;
        this.dataTypeDao = dataTypeDao;
        this.logicalFlowDao = logicalFlowDao;
        this.physicalFlowDao = physicalFlowDao;
        this.physicalSpecificationDao = physicalSpecificationDao;
        this.enumValueAliasService = enumValueAliasService;
        this.dataTypeDecoratorService = dataTypeDecoratorService;
    }


    /**
     * Validate upload
     * resolve entity refs - source, target, owner
     * resolve enums - format, frequency, transport, criticality
     * check for nulls or duplicates in other fields
     * check for duplicates, circular refs
     * @param cmds
     * @return
     */
    public List<PhysicalFlowUploadCommandResponse> validate(List<PhysicalFlowUploadCommand> cmds) {
        checkNotNull(cmds, "cmds cannot be empty");

        // load application and actor maps
        Map<String, Application> applicationsByAssetCode = loadApplicationsByAssetCode();
        Map<String, Actor> actorsByNameMap = loadActorsByName();
        Map<String, DataType> dataTypesByNameOrCodeMap = loadDataTypesByNameOrCode();
        Aliases<String> transportAliases = loadTransportAliases();

        // parse flows and resolve strings into entities or enums
        List<PhysicalFlowUploadCommandResponse> parsedFlows = cmds.stream()
                .map(cmd -> validateCommand(actorsByNameMap, applicationsByAssetCode, dataTypesByNameOrCodeMap, transportAliases, cmd))
                .collect(toList());

        // enumerate and locate an existing physical flows that exist - iff no parse errors
        if(parsedFlows.stream().anyMatch(f -> f.outcome() == CommandOutcome.FAILURE)) {
            return parsedFlows;
        }

        // no parse errors - check for duplicates
        List<PhysicalFlowUploadCommandResponse> responses = parsedFlows.stream()
                .map(f -> Optional.ofNullable(physicalFlowDao.getByParsedFlow(f.parsedFlow()))
                    .map(m -> (PhysicalFlowUploadCommandResponse) ImmutablePhysicalFlowUploadCommandResponse
                            .copyOf(f)
                            .withEntityReference(m.entityReference()))
                    .orElse(f))
                .collect(toList());

        return responses;
    }

    
    public List<PhysicalFlowUploadCommandResponse> upload(String username,
                                                          List<PhysicalFlowUploadCommand> cmds) throws Exception {
        checkNotNull(cmds, "cmds cannot be empty");

        // load application and actor maps
        List<PhysicalFlowUploadCommandResponse> validated = validate(cmds);

        if(validated.stream().anyMatch(v -> v.outcome() == CommandOutcome.FAILURE)) {
            throw new IllegalArgumentException("Cannot upload flows which contain parse errors, please validate");
        }

        List<PhysicalFlowUploadCommandResponse> newFlowCmds = validated.stream()
                .filter(v -> v.outcome() == CommandOutcome.SUCCESS && v.entityReference() == null)
                .collect(toList());

        // create physical flow with ids from the above two
        List<PhysicalFlowUploadCommandResponse> created = newFlowCmds.stream()
                .map(v -> {
                    if (!(v.outcome() == CommandOutcome.SUCCESS && v.entityReference() == null)) {
                        return v;
                    }

                    PhysicalFlowParsed flow = v.parsedFlow();
                    LogicalFlow logicalFlow = getOrCreateLogicalFlow(flow.source(), flow.target(), flow.dataType(), username);
                    PhysicalSpecification specification = getOrCreatePhysicalSpec(flow, username);

                    PhysicalFlow newFlow = ImmutablePhysicalFlow.builder()
                            .logicalFlowId(logicalFlow.id().get())
                            .specificationId(specification.id().get())
                            .basisOffset(flow.basisOffset())
                            .frequency(flow.frequency())
                            .transport(flow.transport())
                            .criticality(flow.criticality())
                            .description(flow.description())
                            .externalId(Optional.ofNullable(flow.externalId()))
                            .lastUpdatedBy(username)
                            .lastUpdatedAt(nowUtc())
                            .build();

                    long id = getOrCreatePhysicalFlow(newFlow);

                    return ImmutablePhysicalFlowUploadCommandResponse.copyOf(v)
                            .withEntityReference(EntityReference.mkRef(EntityKind.PHYSICAL_FLOW, id));
                })
                .collect(toList());

        return created;
    }


    ////////////////////// PRIVATE //////////////////////
    /////////////////////////////////////////////////////

    private PhysicalFlowUploadCommandResponse validateCommand(Map<String, Actor> actorsByName,
                                                              Map<String, Application> applicationsByAssetCode,
                                                              Map<String, DataType> dataTypeMap,
                                                              Aliases<String> transportAliases,
                                                              PhysicalFlowUploadCommand cmd) {
        checkNotNull(cmd, "cmd cannot be null");

        Map<String, String> errors = new HashMap<>();

        // resolve entity refs - source, target, owner
        EntityReference source = getNodeRefByString(actorsByName, applicationsByAssetCode, cmd.source());
        EntityReference target = getNodeRefByString(actorsByName, applicationsByAssetCode, cmd.target());
        EntityReference owner = getNodeRefByString(actorsByName, applicationsByAssetCode, cmd.owner());
        EntityReference dataType = getDataTypeByString(dataTypeMap, cmd.dataType());

        if (source == null) {
            errors.put("source", String.format("%s not found", cmd.source()));
        }

        if (target == null) {
            errors.put("target", String.format("%s not found", cmd.target()));
        }

        if (owner == null) {
            errors.put("owner", String.format("%s not found", cmd.owner()));
        }

        if (dataType == null) {
            errors.put("dataType", String.format("%s not found", cmd.dataType()));
        }


        // resolve enums - format, frequency, transport, criticality
        DataFormatKind format = DataFormatKind.parse(cmd.format(), (s) -> null);
        if (format == null) {
            errors.put("format", String.format("%s is not a recognised value", cmd.format()));
        }

        FrequencyKind frequency = FrequencyKind.parse(cmd.frequency(), (s) -> null);
        if (frequency == null) {
            errors.put("frequency", String.format("%s is not a recognised value", cmd.frequency()));
        }

        String transport = transportAliases
                .lookup(cmd.transport())
                .orElseGet(() -> {
                    errors.put("transport", String.format("%s is not a recognised value", cmd.transport()));
                    return null;
                });

        Criticality criticality = Criticality.parse(cmd.criticality(), (s) -> null);
        if (criticality == null) {
            errors.put("criticality", String.format("%s is not a recognised value", cmd.criticality()));
        }

        // check for nulls or duplicates in other fields
        if(isEmpty(cmd.name())) {
            errors.put("name", "name not provided");
        }

        Integer basisOffset = parseBasisOffset(cmd.basisOffset());
        if (basisOffset == null) {
            errors.put("basisOffset", String.format("%s is not a recognised value, expect this to be a number", cmd.basisOffset()));
        }

        ImmutablePhysicalFlowParsed parsedFlow = ImmutablePhysicalFlowParsed.builder()
                .source(source)
                .target(target)
                .owner(owner)
                .name(cmd.name())
                .format(format)
                .specDescription(cmd.specDescription())
                .specExternalId(cmd.specExternalId())
                .frequency(frequency)
                .transport(transport)
                .criticality(criticality)
                .description(cmd.description())
                .externalId(cmd.externalId())
                .basisOffset(basisOffset)
                .dataType(dataType)
                .build();

        return ImmutablePhysicalFlowUploadCommandResponse.builder()
                .parsedFlow(parsedFlow)
                .errors(errors)
                .originalCommand(cmd)
                .outcome(errors.size() > 0 ? CommandOutcome.FAILURE : CommandOutcome.SUCCESS)
                .build();
    }


    private Integer parseBasisOffset(String basisOffset) {
        Matcher matcher = basisOffsetRegex.matcher(basisOffset);
        if(matcher.matches()) {
            String offset = matcher.group("offset");
            return Integer.valueOf(offset);
        } else {
            return null;
        }
    }


    /**
     * Retrieve Entity Reference by string input (can either be asset code if application or name of an actor)
     * @param input
     * @return
     */
    private EntityReference getNodeRefByString(Map<String, Actor> actorsByName,
                                               Map<String, Application> applicationsByAssetCode,
                                               String input) {
        checkNotNull(input, "input cannot be null");
        input = input.trim();

        return Optional.ofNullable(getActorRefByName(actorsByName, input))
                .orElse(getAppRefByAssetCode(applicationsByAssetCode, input));
    }


    private EntityReference getActorRefByName(Map<String, Actor> actorsByName, String name) {
        return Optional.ofNullable(actorsByName.get(lower(name)))
                .map(Actor::entityReference)
                .orElse(null);
    }


    private EntityReference getAppRefByAssetCode(Map<String, Application> applicationsByAssetCode, String source) {
        return Optional.ofNullable(applicationsByAssetCode.get(lower(source)))
                .map(Application::entityReference)
                .orElse(null);
    }


    private EntityReference getDataTypeByString(Map<String, DataType> dataTypeMap, String value) {
        return Optional.ofNullable(dataTypeMap.get(lower(value)))
                .map(DataType::entityReference)
                .orElse(null);
    }


    private Map<String, Application> loadApplicationsByAssetCode() {
        return MapUtilities.indexBy(
                a -> lower(a.assetCode().get()),
                applicationDao.findAll());
    }


    private Map<String, Actor> loadActorsByName() {
        return  MapUtilities.indexBy(
                a -> lower(a.name()),
                actorDao.findAll());
    }


    private Map<String, DataType> loadDataTypesByNameOrCode() {
        List<DataType> allDataTypes = dataTypeDao.findAll();
        Map<String, DataType> dataTypesByName = MapUtilities.indexBy(dt -> lower(dt.name()), identity(), allDataTypes, (d1, d2) -> d2);
        Map<String, DataType> dataTypesByCode = MapUtilities.indexBy(dt -> lower(dt.code()), identity(), allDataTypes, (d1, d2) -> d2);
        dataTypesByName.putAll(dataTypesByCode);
        return dataTypesByName;
    }


    private LogicalFlow getOrCreateLogicalFlow(EntityReference source,
                                               EntityReference target,
                                               EntityReference dataType,
                                               String username) {

        LogicalFlow flow = logicalFlowDao.getBySourceAndTarget(source, target);

        if (flow == null) {
            LocalDateTime now = nowUtc();

            LogicalFlow flowToAdd = ImmutableLogicalFlow.builder()
                    .source(source)
                    .target(target)
                    .lastUpdatedBy(username)
                    .lastUpdatedAt(now)
                    .provenance("waltz")
                    .created(UserTimestamp.mkForUser(username, now))
                    .build();

            flow = logicalFlowDao.addFlow(flowToAdd);
        }


        EntityReference logicalFlowEntityRef = EntityReference.mkRef(EntityKind.LOGICAL_DATA_FLOW, flow.id().get());
        DataTypeDecorator existingDecorator = dataTypeDecoratorService.getByEntityRefAndDataTypeId(
                logicalFlowEntityRef,
                dataType.id());

        if(existingDecorator == null) {
            dataTypeDecoratorService
                    .addDecorators(username, logicalFlowEntityRef, fromArray(dataType.id()));
        }
        return flow;
    }


    private PhysicalSpecification getOrCreatePhysicalSpec(PhysicalFlowParsed flow,
                                                          String username) {
        EntityReference owner = flow.owner();
        DataFormatKind format = flow.format();
        String name = flow.name();


        // check database
        PhysicalSpecification spec = physicalSpecificationDao.getByParsedFlow(flow);
        if (spec == null) {

            // create
            LocalDateTime now = nowUtc();
            PhysicalSpecification specToAdd = ImmutablePhysicalSpecification.builder()
                    .owningEntity(owner)
                    .format(format)
                    .name(name)
                    .externalId(Optional.ofNullable(flow.specExternalId()).orElse(""))
                    .description(Optional.ofNullable(flow.specDescription()).orElse(""))
                    .lastUpdatedBy(username)
                    .lastUpdatedAt(now)
                    .provenance("waltz")
                    .created(UserTimestamp.mkForUser(username, now))
                    .build();

            Long id = physicalSpecificationDao.create(specToAdd);
            spec = ImmutablePhysicalSpecification
                    .copyOf(specToAdd)
                    .withId(id);
        }

        long dataTypeId = flow.dataType().id();
        EntityReference specificationEntityRef = EntityReference.mkRef(EntityKind.PHYSICAL_SPECIFICATION, spec.id().get());

        DataTypeDecorator existingDataType = dataTypeDecoratorService.getByEntityRefAndDataTypeId(specificationEntityRef, dataTypeId);
        if(existingDataType == null) {
            dataTypeDecoratorService.addDecorators(username, specificationEntityRef, fromArray(dataTypeId));
        }
        return spec;
    }


    private long getOrCreatePhysicalFlow(PhysicalFlow newFlow) {
        PhysicalFlow existing = physicalFlowDao.matchPhysicalFlow(newFlow);
        if (existing != null) {
            return existing.id().get();
        } else {
            return physicalFlowDao.create(newFlow);
        }
    }

    private Aliases<String> loadTransportAliases() {
        return enumValueAliasService.mkAliases(EnumValueKind.TRANSPORT_KIND);
    }


}
