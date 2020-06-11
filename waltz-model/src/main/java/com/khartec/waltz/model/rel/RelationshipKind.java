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

package com.khartec.waltz.model.rel;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.khartec.waltz.model.*;
import org.immutables.value.Value;

@Value.Immutable
@JsonSerialize(as = ImmutableRelationshipKind.class)
@JsonDeserialize(as = ImmutableRelationshipKind.class)
public abstract class RelationshipKind implements IdProvider, NameProvider, DescriptionProvider {

    public abstract EntityKind kindA();
    public abstract EntityKind kindB();
    public abstract String code();
    public abstract String reverseName();
    public abstract int position();

    @Nullable
    public abstract Long categoryA();

    @Nullable
    public abstract Long categoryB();

    @Value.Default
    public boolean isReadonly() {
        return false;
    }
}
