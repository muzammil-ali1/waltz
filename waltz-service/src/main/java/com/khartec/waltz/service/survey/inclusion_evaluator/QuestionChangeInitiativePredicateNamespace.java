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

package com.khartec.waltz.service.survey.inclusion_evaluator;

import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.survey.SurveyQuestion;
import com.khartec.waltz.model.survey.SurveyQuestionResponse;
import com.khartec.waltz.schema.tables.ChangeInitiative;
import org.jooq.DSLContext;

import java.util.List;
import java.util.Map;

/**
 *
 * NOTE: methods in this class may show as unused.  This is to be expected as they are referred to via
 * predicates in survey questions
 */
public class QuestionChangeInitiativePredicateNamespace extends QuestionEntityPredicateNamespace {

    public QuestionChangeInitiativePredicateNamespace(DSLContext dsl,
                                                       EntityReference subjectRef,
                                                      List<SurveyQuestion> questions,
                                                      Map<Long, SurveyQuestionResponse> responsesByQuestionId) {
        super(dsl, subjectRef, questions, responsesByQuestionId);
    }


    public boolean belongsToOrgUnit(String name) {
        ChangeInitiative ci = ChangeInitiative.CHANGE_INITIATIVE.as("ci");
        return belongsToOrgUnit(name, ci, ci.ID, ci.ORGANISATIONAL_UNIT_ID);
    }

}
