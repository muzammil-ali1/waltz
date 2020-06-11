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

package com.khartec.waltz.service.licence;


import com.khartec.waltz.data.licence.LicenceDao;
import com.khartec.waltz.data.licence.LicenceIdSelectorFactory;
import com.khartec.waltz.model.IdSelectionOptions;
import com.khartec.waltz.model.licence.Licence;
import com.khartec.waltz.model.tally.Tally;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.khartec.waltz.common.Checks.checkNotNull;

@Service
public class LicenceService {

    private final LicenceDao licenceDao;
    private final LicenceIdSelectorFactory licenceIdSelectorFactory = new LicenceIdSelectorFactory();


    @Autowired
    public LicenceService(LicenceDao licenceDao) {
        checkNotNull(licenceDao, "licenceDao cannot be null");
        this.licenceDao = licenceDao;
    }


    public List<Licence> findAll() {
        return licenceDao.findAll();
    }


    public Licence getById(long id) {
        return licenceDao.getById(id);
    }


    public List<Licence> findBySelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = licenceIdSelectorFactory.apply(options);
        return licenceDao.findBySelector(selector);
    }


    public List<Tally<Long>> countApplications() {
        return licenceDao.countApplications();
    }
}
