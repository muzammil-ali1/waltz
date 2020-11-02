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

package com.khartec.waltz.jobs.harness;

import com.khartec.waltz.common.SetUtilities;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.survey.*;
import com.khartec.waltz.service.DIConfiguration;
import com.khartec.waltz.service.survey.SurveyInstanceService;
import com.khartec.waltz.service.survey.SurveyQuestionService;
import com.khartec.waltz.service.survey.SurveyRunService;
import com.khartec.waltz.service.survey.SurveyTemplateService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.Collections;
import java.util.List;
import java.util.stream.LongStream;

import static java.util.stream.Collectors.toList;


public class SurveyHarness {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(DIConfiguration.class);

        surveyTempateHarness(ctx);
//        surveyRunHarness(ctx);
//        surveyResponseHarness(ctx);
    }

    private static void surveyTempateHarness(AnnotationConfigApplicationContext ctx) {
        SurveyTemplateService surveyTemplateService = ctx.getBean(SurveyTemplateService.class);
        SurveyQuestionService surveyQuestionService = ctx.getBean(SurveyQuestionService.class);

        SurveyTemplateChangeCommand surveyTemplateChangeCommand = ImmutableSurveyTemplateChangeCommand.builder()
                .name("AAA")
                .description("BBB")
                .targetEntityKind(EntityKind.CHANGE_INITIATIVE)
                .build();

        long templateId = surveyTemplateService.create("admin", surveyTemplateChangeCommand);
        System.out.println("Created: template create with ID = " + templateId);

        SurveyQuestion surveyQuestion = ImmutableSurveyQuestion.builder()
                .surveyTemplateId(templateId)
                .sectionName("SSS")
                .questionText("QQQ")
                .helpText("HHH")
                .fieldType(SurveyQuestionFieldType.TEXTAREA)
                .position(1)
                .isMandatory(false)
                .allowComment(true)
                .build();

        long questionId = surveyQuestionService.create(surveyQuestion);
        System.out.println("Created: question create with ID = " + questionId);
    }


    private static void surveyRunHarness(AnnotationConfigApplicationContext ctx) {
        SurveyQuestionService surveyQuestionService = ctx.getBean(SurveyQuestionService.class);
        surveyQuestionService.findForSurveyTemplate(1).forEach(System.out::println);

        IdSelectionOptions idSelectionOptions = ImmutableIdSelectionOptions.builder()
                .entityReference(ImmutableEntityReference.mkRef(EntityKind.APP_GROUP, 1))
                .scope(HierarchyQueryScope.EXACT)
                .build();

        SurveyRunCreateCommand surveyRunCreateCommand = ImmutableSurveyRunCreateCommand.builder()
                .surveyTemplateId(1L)
                .name("Q1 Quality Survey")
                .selectionOptions(idSelectionOptions)
                .issuanceKind(SurveyIssuanceKind.INDIVIDUAL)
                .involvementKindIds(SetUtilities.fromCollection(
                        LongStream.range(1, 5).mapToObj(Long::valueOf)
                        .collect(toList())))
                .contactEmail("jack.livingston12@gmail.com")
                .build();

        SurveyRunService surveyRunService = ctx.getBean(SurveyRunService.class);

        String userName = "livingston@mail.com";
        long surveyRunId = surveyRunService.createSurveyRun(userName, surveyRunCreateCommand).id().get();

        List<SurveyInstanceRecipient> surveyInstanceRecipients = surveyRunService.generateSurveyInstanceRecipients(surveyRunId);

        surveyInstanceRecipients.forEach(r -> System.out.println(
                r.surveyInstance().surveyEntity().name().get()
                + " => "
                + r.person().email()));

        System.out.println("Generated recipients count: " + surveyInstanceRecipients.size());

        surveyRunService.createSurveyInstancesAndRecipients(surveyRunId, surveyInstanceRecipients.subList(0, 5));

        ImmutableSurveyRunChangeCommand surveyRunChangeCommand = ImmutableSurveyRunChangeCommand.builder()
                .surveyTemplateId(1L)
                .name("Q2 Quality Survey")
                .selectionOptions(idSelectionOptions)
                .issuanceKind(SurveyIssuanceKind.GROUP)
                .involvementKindIds(SetUtilities.fromCollection(
                        LongStream.range(3, 7).mapToObj(Long::valueOf)
                                .collect(toList())))
                .contactEmail("jack.livingston12@gmail.com")
                .build();

        // update survey run
        surveyRunService.updateSurveyRun(userName, surveyRunId, surveyRunChangeCommand);

        List<SurveyInstanceRecipient> updatedSurveyInstanceRecipients = surveyRunService.generateSurveyInstanceRecipients(surveyRunId);
        System.out.println("Updated Generated recipients count: " + updatedSurveyInstanceRecipients.size());

        // generate the instances and recipients again
        surveyRunService.createSurveyInstancesAndRecipients(surveyRunId, Collections.emptyList());

        // finally publish
        surveyRunService.updateSurveyRunStatus(userName, surveyRunId, SurveyRunStatus.ISSUED);
    }


    private static void surveyResponseHarness(AnnotationConfigApplicationContext ctx) {
        String userName = "1258battle@gmail.com";

        SurveyQuestionService surveyQuestionService = ctx.getBean(SurveyQuestionService.class);
        SurveyInstanceService surveyInstanceService = ctx.getBean(SurveyInstanceService.class);

        List<SurveyInstance> instances = surveyInstanceService.findForRecipient(userName);

        System.out.println("===========Instances==========");
        System.out.println(instances);

        SurveyInstance instance = instances.get(0);
        List<SurveyQuestion> questions = surveyQuestionService.findForSurveyInstance(instance.id().get());

        System.out.println("===========Questions==========");
        System.out.println(questions);

        List<SurveyInstanceQuestionResponse> responses = surveyInstanceService.findResponses(instance.id().get());

        System.out.println("===========Responses==========");
        System.out.println(responses);

        ImmutableSurveyQuestionResponse insertResponse = ImmutableSurveyQuestionResponse.builder()
                .questionId(1L)
                .comment("some comment")
                .stringResponse("some response")
                .build();

        surveyInstanceService.saveResponse(userName, instance.id().get(), insertResponse);
        System.out.println("===========Inserted Responses==========");
        System.out.println(surveyInstanceService.findResponses(instance.id().get()));

        ImmutableSurveyQuestionResponse updateResponse = insertResponse
                .withStringResponse("updated string response");

        surveyInstanceService.saveResponse(userName, instance.id().get(), updateResponse);
        System.out.println("===========Updated Responses==========");
        System.out.println(surveyInstanceService.findResponses(instance.id().get()));

        surveyInstanceService.updateStatus(
                userName,
                instance.id().get(),
                ImmutableSurveyInstanceStatusChangeCommand.builder()
                        .action(SurveyInstanceAction.SAVING)
                        .build());
    }


}
