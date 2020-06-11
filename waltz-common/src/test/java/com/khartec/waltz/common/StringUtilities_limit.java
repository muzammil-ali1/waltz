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

package com.khartec.waltz.common;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class StringUtilities_limit {

    @Test
    public void limitingNullGivesNull() {
        String result = StringUtilities.limit(null, 10);
        assertNull(result);
    }


    @Test
    public void limitingWithANegativeGivesNull() {
        String result = StringUtilities.limit(null, -10);
        assertNull(result);
    }


    @Test
    public void limitingLongStringReturnsInitialPortion() {
        String result = StringUtilities.limit("hello world", 5);
        assertEquals("hello", result);
    }


    @Test
    public void limitingShortStringReturnsFullString() {
        String result = StringUtilities.limit("hello", 10);
        assertEquals("hello", result);
    }


}
