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

package com.khartec.waltz.model;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.model.application.ApplicationIdSelectionOptions;
import com.khartec.waltz.model.application.ApplicationKind;
import com.khartec.waltz.model.application.ImmutableApplicationIdSelectionOptions;
import org.junit.Assert;
import org.junit.Test;

public class ApplicationIdSelectionOptionsTests {

    @Test
    public void convertsIdSelectionOptionsWhenAppOptions() {
        IdSelectionOptions appOptions = ImmutableApplicationIdSelectionOptions.builder()
                .entityReference(EntityReference.mkRef(EntityKind.ORG_UNIT, 1))
                .scope(HierarchyQueryScope.CHILDREN)
                .entityLifecycleStatuses(ListUtilities.newArrayList(EntityLifecycleStatus.PENDING, EntityLifecycleStatus.ACTIVE))
                .applicationKinds(ListUtilities.newArrayList(ApplicationKind.CUSTOMISED, ApplicationKind.EUC))
                .build();

        ApplicationIdSelectionOptions convertedAppOptions = ApplicationIdSelectionOptions.mkOpts(appOptions);

        Assert.assertEquals(appOptions, convertedAppOptions);
        Assert.assertEquals(SetUtilities.fromArray(ApplicationKind.CUSTOMISED, ApplicationKind.EUC), convertedAppOptions.applicationKinds());
    }


    @Test
    public void convertsIdSelectionOptionsWhenIdOptions() {
        IdSelectionOptions options = ImmutableIdSelectionOptions.builder()
                .entityReference(EntityReference.mkRef(EntityKind.ORG_UNIT, 1))
                .scope(HierarchyQueryScope.CHILDREN)
                .entityLifecycleStatuses(ListUtilities.newArrayList(EntityLifecycleStatus.PENDING, EntityLifecycleStatus.ACTIVE))
                .build();

        ApplicationIdSelectionOptions convertedOptions = ApplicationIdSelectionOptions.mkOpts(options);

        ApplicationIdSelectionOptions targetAppOptions = ImmutableApplicationIdSelectionOptions.builder()
                .entityReference(EntityReference.mkRef(EntityKind.ORG_UNIT, 1))
                .scope(HierarchyQueryScope.CHILDREN)
                .entityLifecycleStatuses(ListUtilities.newArrayList(EntityLifecycleStatus.PENDING, EntityLifecycleStatus.ACTIVE))
                .applicationKinds(SetUtilities.fromArray(ApplicationKind.values()))
                .build();

        Assert.assertEquals(targetAppOptions, convertedOptions);
    }


    @Test
    public void mkOptsCorrect() {
        ApplicationIdSelectionOptions optionsWithScope = ApplicationIdSelectionOptions.mkOpts(EntityReference.mkRef(EntityKind.ORG_UNIT, 1), HierarchyQueryScope.CHILDREN);

        ApplicationIdSelectionOptions targetOptions = ImmutableApplicationIdSelectionOptions.builder()
                .entityReference(EntityReference.mkRef(EntityKind.ORG_UNIT, 1))
                .scope(HierarchyQueryScope.CHILDREN)
                .build();

        Assert.assertEquals(targetOptions, optionsWithScope);

        ApplicationIdSelectionOptions optionsNoScope = ApplicationIdSelectionOptions.mkOpts(EntityReference.mkRef(EntityKind.ORG_UNIT, 1));

        Assert.assertEquals(targetOptions, optionsNoScope);
    }

}
