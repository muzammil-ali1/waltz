/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.data.change_initiative.search;

import com.khartec.waltz.data.DatabaseVendorSpecific;
import com.khartec.waltz.data.FullTextSearch;
import com.khartec.waltz.data.JooqUtilities;
import com.khartec.waltz.data.change_initiative.ChangeInitiativeDao;
import com.khartec.waltz.model.change_initiative.ChangeInitiative;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import org.jooq.DSLContext;

import java.util.List;

import static com.khartec.waltz.data.SearchUtilities.mkTerms;
import static com.khartec.waltz.schema.tables.ChangeInitiative.CHANGE_INITIATIVE;

public class SqlServerChangeInitiativeSearch implements FullTextSearch<ChangeInitiative>, DatabaseVendorSpecific {

    @Override
    public List<ChangeInitiative> searchFullText(DSLContext dsl, EntitySearchOptions options) {
        List<String> terms = mkTerms(options.searchQuery());
        return dsl.select(CHANGE_INITIATIVE.fields())
                .from(CHANGE_INITIATIVE)
                .where(JooqUtilities.MSSQL.mkContainsPrefix(terms))
                .limit(options.limit())
                .fetch(ChangeInitiativeDao.TO_DOMAIN_MAPPER);
    }

}
