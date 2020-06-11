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

package com.khartec.waltz.web;

import com.khartec.waltz.common.SetUtilities;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.BeforeClass;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.khartec.waltz.common.CollectionUtilities.any;
import static com.khartec.waltz.common.LoggingUtilities.configureLogging;

/**
 * It is advisable to add a rule into your logback config to switch
 * logger `com.tngtech` to `WARN` as it is very chatty.
 */
public class BaseArchitectureComplianceCheck {

    static JavaClasses waltzAndJavaUtilClasses = null;
    static JavaClasses waltzOnlyClasses = null;

    @BeforeClass
    public static void beforeClass() {
        configureLogging();
        waltzAndJavaUtilClasses = new ClassFileImporter().importPackages("com.khartec", "java.util");
        waltzOnlyClasses = new ClassFileImporter().importPackages("com.khartec");
    }

    static ArchCondition<JavaClass> haveFindMethodsThatReturnCollectionsOrMapsOrOptionals =
        new ArchCondition<JavaClass>("have 'find*' methods that return Collections, Maps or Optionals")
        {
            Set<Class<?>> validReturnTypes = SetUtilities.asSet(Collection.class, Optional.class, Map.class);

            @Override
            public void check(JavaClass item, ConditionEvents events) {
                item.getMethods()
                        .stream()
                        .filter(m -> m.getName().startsWith("find"))
                        .filter(m -> m.getModifiers().contains(JavaModifier.PUBLIC))
                        .forEach(m -> {
                            JavaClass returnType = m.getReturnType();
                            if (! any(validReturnTypes, vrt -> returnType.isAssignableTo(vrt))) {
                                String message = String.format(
                                        "Method %s.%s does not return a collection, map or optional. It returns: %s",
                                        item.getName(),
                                        m.getName(),
                                        returnType.getName());
                                events.add(SimpleConditionEvent.violated(item, message));
                            }
                        });
            }
        };

}
