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

package com.khartec.waltz.model.physical_flow;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.Criticality;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.Nullable;
import com.khartec.waltz.model.physical_specification.DataFormatKind;
import org.immutables.value.Value;


@Value.Immutable
@JsonSerialize(as = ImmutablePhysicalFlowParsed.class)
@JsonDeserialize(as = ImmutablePhysicalFlowParsed.class)
public abstract class PhysicalFlowParsed {

    // Logical Flow
    @Nullable
    public abstract EntityReference source();

    @Nullable
    public abstract EntityReference target();


    // Specification
    @Nullable
    public abstract EntityReference owner();

    @Nullable
    public abstract DataFormatKind format();

    public abstract String name();

    @Nullable
    public abstract String specDescription();

    @Nullable
    public abstract String specExternalId();


    // Flow Attributes
    @Nullable
    public abstract Integer basisOffset();

    @Nullable
    public abstract Criticality criticality();

    public abstract String description();

    @Nullable
    public abstract String externalId();

    @Nullable
    public abstract FrequencyKind frequency();

    @Nullable
    public abstract String transport();

    @Nullable
    public abstract EntityReference dataType();
}
