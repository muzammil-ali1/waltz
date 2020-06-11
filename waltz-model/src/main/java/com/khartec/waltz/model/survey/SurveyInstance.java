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

package com.khartec.waltz.model.survey;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdProvider;
import com.khartec.waltz.model.Nullable;
import org.immutables.value.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value.Immutable
@JsonSerialize(as = ImmutableSurveyInstance.class)
@JsonDeserialize(as = ImmutableSurveyInstance.class)
public abstract class SurveyInstance implements IdProvider {

    public abstract Long surveyRunId();
    public abstract EntityReference surveyEntity();

    @Nullable
    public abstract String surveyEntityExternalId();
    public abstract SurveyInstanceStatus status();
    public abstract LocalDate dueDate();

    @Nullable
    public abstract LocalDateTime submittedAt();

    @Nullable
    public abstract String submittedBy();

    @Nullable
    public abstract LocalDateTime approvedAt();

    @Nullable
    public abstract String approvedBy();

    @Nullable
    public abstract Long originalInstanceId();

    @Nullable
    public abstract Long ownerId();

    @Nullable
    public abstract String owningRole();
}
