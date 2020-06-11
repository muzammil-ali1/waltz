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

package com.khartec.waltz.web.endpoints.api;


import com.khartec.waltz.model.change_unit.PhysicalFlowChangeUnitViewItem;
import com.khartec.waltz.service.change_unit.ChangeUnitViewService;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.WebUtilities;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.mkPath;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.getForList;


@Service
public class ChangeUnitViewEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "change-unit-view");

    private final ChangeUnitViewService service;

    @Autowired
    public ChangeUnitViewEndpoint(ChangeUnitViewService service) {
        checkNotNull(service, "service cannot be null");

        this.service = service;
    }


    @Override
    public void register() {

        String findByChangeSetIdForTypePath = mkPath(BASE_URL, "id", ":id", "physical-flow");

        ListRoute<PhysicalFlowChangeUnitViewItem> findByChangeSetIdForTypeRoute = (request, response) ->
                service.findPhysicalFlowChangeUnitsByChangeSetId(WebUtilities.getId(request));

        getForList(findByChangeSetIdForTypePath, findByChangeSetIdForTypeRoute);
    }

}
