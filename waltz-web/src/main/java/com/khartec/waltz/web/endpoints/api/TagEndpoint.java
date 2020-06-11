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

import com.khartec.waltz.model.EntityKind;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.tag.Tag;
import com.khartec.waltz.service.tag.TagService;
import com.khartec.waltz.web.DatumRoute;
import com.khartec.waltz.web.ListRoute;
import com.khartec.waltz.web.endpoints.Endpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.web.WebUtilities.*;
import static com.khartec.waltz.web.endpoints.EndpointUtilities.*;


@Service
public class TagEndpoint implements Endpoint {

    private static final String BASE_URL = mkPath("api", "tag");
    private final TagService tagService;


    @Autowired
    public TagEndpoint(TagService tagService) {
        checkNotNull(tagService, "tagService cannot be null");
        this.tagService = tagService;
    }

    @Override
    public void register() {
        DatumRoute<Tag> getByIdRoute = (req, res) ->
             tagService.getById(getId(req));

        ListRoute<Tag> updateRoute = (req, resp) -> {
            String username = getUsername(req);
            EntityReference ref = getEntityReference(req);
            List<String> tags = readStringsFromBody(req);
            return tagService.updateTags(ref, tags, username);
        };

        ListRoute<Tag> findTagsForEntityReference = (req, resp) -> {
            EntityReference ref = getEntityReference(req);
            return tagService.findTagsForEntityReference(ref);
        };

        ListRoute<Tag> findTagsForEntityKind = (req, resp) -> {
            EntityKind entityKind = getKind(req);
            return tagService.findTagsForEntityKind(entityKind);
        };

        ListRoute<Tag> findTagsForEntityKindAndTargetSelector = (req, resp) -> {
            EntityKind entityKind = getKind(req);
            IdSelectionOptions options = readIdSelectionOptionsFromBody(req);
            return tagService.findTagsForEntityKindAndTargetSelector(entityKind, options);
        };

        getForDatum(mkPath(BASE_URL, "id", ":id"), getByIdRoute);
        getForList(mkPath(BASE_URL, "entity", ":kind", ":id"), findTagsForEntityReference);
        getForList(mkPath(BASE_URL, "target-kind", ":kind"), findTagsForEntityKind);
        postForList(mkPath(BASE_URL, "target-kind", ":kind", "target-selector"), findTagsForEntityKindAndTargetSelector);
        postForList(mkPath(BASE_URL, "entity", ":kind", ":id"), updateRoute);
    }

}
