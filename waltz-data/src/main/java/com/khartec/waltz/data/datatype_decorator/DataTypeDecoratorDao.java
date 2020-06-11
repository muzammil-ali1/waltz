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

package com.khartec.waltz.data.datatype_decorator;

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.datatype.DataTypeDecorator;
import org.jooq.*;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public abstract class DataTypeDecoratorDao {

    public abstract DataTypeDecorator getByEntityIdAndDataTypeId(long entityId, long dataTypeId);

    public abstract List<DataTypeDecorator> findByEntityId(long entityId);

    public abstract List<DataTypeDecorator> findByEntityIdSelector(Select<Record1<Long>> idSelector,
                                                                   Optional<EntityKind> entityKind);

    public abstract List<DataTypeDecorator> findByAppIdSelector(Select<Record1<Long>> appIdSelector);

    public abstract List<DataTypeDecorator> findByDataTypeIdSelector(Select<Record1<Long>> dataTypeIdSelector);

    //only implemented for logical flows
    public abstract List<DataTypeDecorator> findByFlowIds(Collection<Long> flowIds);

    public abstract int[] addDecorators(Collection<DataTypeDecorator> dataTypeDecorators);

    public abstract int removeDataTypes(EntityReference associatedEntityRef, Collection<Long> dataTypeIds);

}
