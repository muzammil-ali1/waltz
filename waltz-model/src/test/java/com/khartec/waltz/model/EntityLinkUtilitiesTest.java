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

import org.junit.Test;

import static org.junit.Assert.*;

public class EntityLinkUtilitiesTest {

    @Test(expected = IllegalArgumentException.class)
    public void mkIdLink_kindCannotBeNull() {
        EntityLinkUtilities.mkIdLink("test", null, 23L);
    }


    @Test(expected = IllegalArgumentException.class)
    public void mkIdLink_idCannotBeNull() {
        EntityLinkUtilities.mkIdLink("test", EntityKind.APPLICATION, null);
    }


    @Test
    public void internal() {
        String path = EntityLinkUtilities.mkIdLink("test/", EntityKind.APPLICATION, 23L);
        assertEquals("test/entity/APPLICATION/id/23", path);
    }


    @Test
    public void external() {
        String path = EntityLinkUtilities.mkExternalIdLink("test/", EntityKind.APPLICATION, "foo");
        assertEquals("test/entity/APPLICATION/external-id/foo", path);
    }

}