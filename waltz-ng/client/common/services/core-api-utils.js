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

import {ActorStore_API as ActorStore} from "../../actor/services/actor-store";
import {AliasStore_API as AliasStore} from "../../alias/services/alias-store";
import {AllocationStore_API as AllocationStore} from "../../allocation/services/allocation-store";
import {AllocationSchemeStore_API as AllocationSchemeStore} from "../../allocation-scheme/services/allocation-scheme-store";
import {AppGroupStore_API as AppGroupStore} from "../../app-groups/services/app-group-store";
import {ApplicationStore_API as ApplicationStore} from "../../applications/services/application-store";
import {AssessmentDefinitionStore_API as AssessmentDefinitionStore} from "../../assessments/services/assessment-definition-store";
import {AssessmentRatingStore_API as AssessmentRatingStore} from "../../assessments/services/assessment-rating-store";
import {AttestationInstanceStore_API as AttestationInstanceStore} from "../../attestation/services/attestation-instance-store";
import {AttestationRunStore_API as AttestationRunStore} from "../../attestation/services/attestation-run-store";
import {AttributeChangeStore_API as AttributeChangeStore} from "../../attribute-change/services/attribute-change-store";
import {AssetCostStore_API as AssetCostStore} from "../../asset-cost/services/asset-cost-store";
import {AuthSourcesStore_API as AuthSourcesStore} from "../../auth-sources/services/auth-sources-store";
import {BookmarkStore_API as BookmarkStore} from "../../bookmarks/services/bookmark-store";
import {ChangeInitiativeStore_API as ChangeInitiativeStore} from "../../change-initiative/services/change-initiative-store";
import {ChangeLogStore_API as ChangeLogStore} from "../../change-log/services/change-log-store";
import {ChangeLogSummariesStore_API as ChangeLogSummariesStore} from "../../change-log/services/change-log-summaries-store";
import {ChangeSetStore_API as ChangeSetStore} from "../../change-set/services/change-set-store";
import {ChangeUnitStore_API as ChangeUnitStore} from "../../change-unit/services/change-unit-store";
import {ChangeUnitViewService_API as ChangeUnitViewService} from "../../change-unit/services/change-unit-view-service";
import {ClientCacheKeyStore_API as ClientCacheKeyStore} from "../../client_cache_key/services/client-cache-key-store";
import {ComplexityStore_API as ComplexityStore} from "../../complexity/services/complexity-store";
import {DatabaseStore_API as DatabaseStore} from "../../databases/services/database-store";
import {DataTypeStore_API as DataTypeStore} from "../../data-types/services/data-type-store";
import {DataTypeDecoratorStore_API as DataTypeDecoratorStore} from "../../data-types/services/data-type-decorator-store";
import {DataTypeUsageStore_API as DataTypeUsageStore} from "../../data-type-usage/services/data-type-usage-store";
import {EndUserAppStore_API as EndUserAppStore} from "../../end-user-apps/services/end-user-app-store";
import {EntityEnumStore_API as EntityEnumStore} from "../../entity-enum/services/entity-enum-store";
import {EntityNamedNoteStore_API as EntityNamedNoteStore} from "../../entity-named-note/services/entity-named-note-store";
import {EntityNamedNoteTypeStore_API as EntityNamedNoteTypeStore} from "../../entity-named-note/services/entity-named-note-type-store";
import {EntityRelationshipStore_API as EntityRelationshipStore} from "../../entity-relationship/services/entity-relationship-store";
import {EntitySearchStore_API as EntitySearchStore} from "../../entity/services/entity-search-store";
import {EntityStatisticStore_API as EntityStatisticStore} from "../../entity-statistics/services/entity-statistic-store";
import {TagStore_API as TagStore} from "../../tag/services/tag-store";
import {EnumValueStore_API as EnumValueStore} from "../../enum-value/services/enum-value-store";
import {EntitySvgDiagramStore_API as EntitySvgDiagramStore} from "../../entity-svg-diagram/services/entity-svg-diagram-store";
import {ExternalIdentifierStore_API as ExternalIdentifierStore} from "../../external-identifier/services/external-identifier-store"
import {FacetStore_API as FacetStore} from "../../facet/services/facet-store";
import {FavouritesStore_API as FavouritesStore} from "../../app-groups/services/favourites-store";
import {FlowDiagramStore_API as FlowDiagramStore} from "../../flow-diagram/services/flow-diagram-store";
import {FlowDiagramAnnotationStore_API as FlowDiagramAnnotationStore} from "../../flow-diagram/services/flow-diagram-annotation-store";
import {FlowDiagramEntityStore_API as FlowDiagramEntityStore} from "../../flow-diagram/services/flow-diagram-entity-store";
import {InvolvementStore_API as InvolvementStore} from "../../involvement/services/involvement-store";
import {InvolvementKindStore_API as InvolvementKindStore} from "../../involvement-kind/services/involvement-kind-store";
import {InvolvementViewService_API as InvolvementViewService} from "../../involvement/services/involvement-view-service";
import {LicenceStore_API as LicenceStore} from "../../licence/services/licence-store";
import {LogicalDataElementStore_API as LogicalDataElementStore} from "../../logical-data-element/services/logical-data-element-store";
import {LogicalFlowStore_API as LogicalFlowStore} from "../../logical-flow/services/logical-flow-store";
import {LogicalFlowDecoratorStore_API as LogicalFlowDecoratorStore} from "../../logical-flow-decorator/services/logical-flow-decorator-store";
import {MeasurableCategoryStore_API as MeasurableCategoryStore} from "../../measurable-category/services/measurable-category-store";
import {MeasurableRatingStore_API as MeasurableRatingStore} from "../../measurable-rating/services/measurable-rating-store";
import {MeasurableRatingPlannedDecommissionStore_API as MeasurableRatingPlannedDecommissionStore} from "../../measurable-rating/services/measurable-rating-planned-decommission-store";
import {MeasurableRatingReplacementStore_API as MeasurableRatingReplacementStore} from "../../measurable-rating/services/measurable-rating-replacement-store";
import {MeasurableRelationshipStore_API as MeasurableRelationshipStore} from "../../measurable-relationship/services/measurable-relationship-store";
import {MeasurableStore_API as MeasurableStore} from "../../measurable/services/measurable-store";
import {NotificationStore_API as NotificationStore} from "../../notification/services/notification-store";
import {OrgUnitStore_API as OrgUnitStore} from "../../org-units/services/org-unit-store";
import {PersonStore_API as PersonStore} from "../../person/services/person-store";
import {PhysicalFlowStore_API as PhysicalFlowStore} from "../../physical-flows/services/physical-flow-store";
import {PhysicalFlowParticipantStore_API as PhysicalFlowParticipantStore} from "../../physical-flows/services/physical-flow-participant-store";
import {PhysicalSpecDefinitionFieldStore_API as PhysicalSpecDefinitionFieldStore} from "../../physical-specifications/services/physical-spec-definition-field-store";
import {PhysicalSpecDefinitionStore_API as PhysicalSpecDefinitionStore} from "../../physical-specifications/services/physical-spec-definition-store";
import {PhysicalSpecDefinitionSampleFileStore_API as PhysicalSpecDefinitionSampleFileStore} from "../../physical-specifications/services/physical-spec-definition-sample-file-store";
import {PhysicalSpecificationStore_API as PhysicalSpecificationStore} from "../../physical-specifications/services/physical-specification-store";
import {RatingSchemeStore_API as RatingSchemeStore} from "../../ratings/services/rating-scheme-store";
import {RelationshipKindStore_API as RelationshipKindStore} from "../../entity-relationship/services/relationship-kind-store";
import {RoadmapStore_API as RoadmapStore} from "../../roadmap/services/roadmap-store";
import {ScenarioStore_API as ScenarioStore} from "../../scenario/services/scenario-store";
import {ServerInfoStore_API as ServerInfoStore} from "../../server-info/services/server-info-store";
import {ServerUsageStore_API as ServerUsageStore} from "../../server-info/services/server-usage-store";
import {SettingsStore_API as SettingsStore} from "../../system/services/settings-store";
import {SharedPreferenceStore_API as SharedPreferenceStore} from "../../shared-preference/services/shared-preference-store";
import {SoftwareCatalogStore_API as SoftwareCatalogStore} from "../../software-catalog/services/software-catalog-store";
import {SourceDataRatingStore_API as SourceDataRatingStore} from "../../source-data-rating/services/source-data-rating-store";
import {StaticPanelStore_API as StaticPanelStore} from "../../static-panel/services/static-panel-store";
import {SurveyInstanceStore_API as SurveyInstanceStore} from "../../survey/services/survey-instance-store";
import {SurveyQuestionStore_API as SurveyQuestionStore} from "../../survey/services/survey-question-store";
import {SurveyRunStore_API as SurveyRunStore} from "../../survey/services/survey-run-store";
import {SurveyTemplateStore_API as SurveyTemplateStore} from "../../survey/services/survey-template-store";
import {SvgDiagramStore_API as SvgDiagramStore} from "../../svg-diagram/services/svg-diagram-store";
import {TaxonomyManagementStore_API as TaxonomyManagementStore} from "../../taxonomy-management/services/taxonomy-management-store";
import {TechnologyStatisticsService_API as TechnologyStatisticsService} from "../../technology/services/technology-statistics-service";
import {ThumbnailStore_API as ThumbnailStore} from "../../thumbnail/services/thumbnail-store";
import {UserStore_API as UserStore} from "../../user/services/user-store";
import {RoleStore_API as RoleStore} from "../../role/services/role-store";


export const CORE_API = {
    ActorStore,
    AliasStore,
    AllocationStore,
    AllocationSchemeStore,
    AppGroupStore,
    ApplicationStore,
    AttestationInstanceStore,
    AttestationRunStore,
    AttributeChangeStore,
    AssetCostStore,
    AssessmentDefinitionStore,
    AssessmentRatingStore,
    AuthSourcesStore,
    BookmarkStore,
    ChangeInitiativeStore,
    ChangeLogStore,
    ChangeLogSummariesStore,
    ChangeSetStore,
    ChangeUnitStore,
    ChangeUnitViewService,
    ClientCacheKeyStore,
    ComplexityStore,
    DatabaseStore,
    DataTypeStore,
    DataTypeDecoratorStore,
    DataTypeUsageStore,
    EndUserAppStore,
    EntityEnumStore,
    EntityNamedNoteStore,
    EntityNamedNoteTypeStore,
    EntityRelationshipStore,
    EntitySearchStore,
    EntityStatisticStore,
    TagStore,
    EnumValueStore,
    ExternalIdentifierStore,
    FacetStore,
    FavouritesStore,
    FlowDiagramStore,
    FlowDiagramAnnotationStore,
    FlowDiagramEntityStore,
    EntitySvgDiagramStore,
    InvolvementStore,
    InvolvementKindStore,
    InvolvementViewService,
    LicenceStore,
    LogicalDataElementStore,
    LogicalFlowStore,
    LogicalFlowDecoratorStore,
    MeasurableCategoryStore,
    MeasurableRatingStore,
    MeasurableRatingPlannedDecommissionStore,
    MeasurableRatingReplacementStore,
    MeasurableRelationshipStore,
    MeasurableStore,
    NotificationStore,
    OrgUnitStore,
    PersonStore,
    PhysicalFlowStore,
    PhysicalFlowParticipantStore,
    PhysicalSpecDefinitionFieldStore,
    PhysicalSpecDefinitionStore,
    PhysicalSpecDefinitionSampleFileStore,
    PhysicalSpecificationStore,
    RatingSchemeStore,
    RelationshipKindStore,
    RoadmapStore,
    ScenarioStore,
    ServerInfoStore,
    ServerUsageStore,
    SettingsStore,
    SharedPreferenceStore,
    SoftwareCatalogStore,
    SourceDataRatingStore,
    StaticPanelStore,
    SurveyInstanceStore,
    SurveyQuestionStore,
    SurveyRunStore,
    SurveyTemplateStore,
    SvgDiagramStore,
    TaxonomyManagementStore,
    TechnologyStatisticsService,
    ThumbnailStore,
    UserStore,
    RoleStore
};


export function getApiReference(serviceName, serviceFnName) {
    return CORE_API[serviceName][serviceFnName];
}
