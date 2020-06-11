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

import "babel-polyfill";
import "angular-animate";
import "angular-loading-bar";
import "angular-local-storage";
import "angular-sanitize";
import "angular-tree-control";
import "angular-ui-notification";
import "angular-ui-grid/ui-grid";
import "angular-ui-bootstrap";
import "ng-tags-input";
import "ng-showdown";
import "satellizer";
import "ui-select";

import "@uirouter/angularjs";

import AngularFormly from "angular-formly";
import AngularFormlyTemplates from "angular-formly-templates-bootstrap";

import AccessLog from "./access-log";
import Actor from "./actor";
import Alias from "./alias";
import Allocation from "./allocation";
import AllocationScheme from "./allocation-scheme";
import Applications from "./applications";
import AppGroups from "./app-groups";
import Assessments from "./assessments";
import AssetCost from "./asset-cost";
import Attestation from "./attestation";
import AttributeChange from "./attribute-change";
import AuthSources from "./auth-sources";
import Bookmarks from "./bookmarks";
import ChangeInitiative from "./change-initiative";
import ClientCacheKey from "./client_cache_key";
import Complexity from "./complexity";
import Common_Module from "./common/module";
import ChangeLog from "./change-log";
import ChangeSet from "./change-set";
import ChangeUnit from "./change-unit";
import DataFlow from "./data-flow";
import DataTypeUsage from "./data-type-usage";
import DataTypes from "./data-types";
import Databases from "./databases";
import DynamicSection from "./dynamic-section";
import Embed from "./embed";
import EndUserApps from "./end-user-apps";
import Entity from "./entity";
import EntityDiagrams from "./entity-diagrams";
import EntityEnum from "./entity-enum";
import EntityNamedNote from "./entity-named-note";
import EntityRelationship from "./entity-relationship";
import EntityStatistics from "./entity-statistics";
import EntitySvgDiagram from "./entity-svg-diagram";
import Tag from "./tag";
import EnumValue from "./enum-value";
import Examples from "./examples";
import Extensions from "./extensions";
import ExternalIdentifier from "./external-identifier";
import Facet from "./facet";
import FlowDiagram from "./flow-diagram";
import Formly from "./formly";
import History from "./history";
import Involvement from "./involvement";
import InvolvementKind from "./involvement-kind";
import Licence from "./licence";
import LogicalDataElement from "./logical-data-element";
import LogicalFlow from "./logical-flow";
import LogicalFlowDecorator from "./logical-flow-decorator";
import Measurable from "./measurable";
import MeasurableCategory from "./measurable-category";
import MeasurableRating from "./measurable-rating";
import MeasurableRelationship from "./measurable-relationship";
import Navbar from "./navbar";
import Notification from "./notification";
import OrgUnits from "./org-units";
import Orphan from "./orphan";
import Person from "./person";
import PhysicalFlows from "./physical-flows";
import PhysicalSpecifications from "./physical-specifications";
import PhysicalField from "./physical-field";
import Playpen from "./playpen";
import Playpen5 from "./playpen/5";
import Profile from "./profile";
import Ratings from "./ratings";
import Roadmap from "./roadmap";
import Role from "./role";
import Scenario from "./scenario";
import SharedPreference from "./shared-preference";
import ServerInfo from "./server-info";
import SoftwareCatalog from "./software-catalog";
import SourceDataRating from "./source-data-rating";
import StaticPanel from "./static-panel";
import Survey from "./survey";
import SvgDiagram from "./svg-diagram";
import System from "./system";
import Technology from "./technology";
import TaxonomyManagement from "./taxonomy-management";
import User from "./user";
import UserContribution from "./user-contribution";
import Welcome from "./welcome";
import Widgets from "./widgets";


const dependencies = [
    "ui.bootstrap",
    "ui.router",
    "ui.select",
    "ui.grid",
    "ui.grid.autoResize",
    "ui.grid.exporter",
    "ui.grid.resizeColumns",
    "ui.grid.selection",
    "ui-notification",
    "ngAnimate",
    "ngSanitize",
    "ngTagsInput",
    "ng-showdown",
    "satellizer",
    "LocalStorageModule",
    AngularFormly,
    AngularFormlyTemplates,
    "treeControl",
    "angular-loading-bar",

    // -- waltz-modules ---
    AccessLog(),
    Actor(),
    Alias(),
    Allocation(),
    AllocationScheme(),
    Applications(),
    AppGroups(),
    Assessments(),
    AssetCost(),
    Attestation(),
    AttributeChange(),
    AuthSources(),
    Bookmarks(),
    ChangeInitiative(),
    ClientCacheKey(),
    Complexity(),
    Common_Module(),
    ChangeLog(),
    ChangeSet(),
    ChangeUnit(),
    DataFlow(),
    DataTypeUsage(),
    DataTypes(),
    Databases(),
    DynamicSection(),
    Embed(),
    EndUserApps(),
    Entity(),
    EntityDiagrams(),
    EntityEnum(),
    EntityNamedNote(),
    EntityRelationship(),
    EntityStatistics(),
    EntitySvgDiagram(),
    Tag(),
    EnumValue(),
    Examples(),
    Extensions(),
    ExternalIdentifier(),
    Facet(),
    FlowDiagram(),
    Formly(),
    History(),
    Involvement(),
    InvolvementKind(),
    Licence(),
    LogicalDataElement(),
    LogicalFlow(),
    LogicalFlowDecorator(),
    Measurable(),
    MeasurableCategory(),
    MeasurableRating(),
    MeasurableRelationship(),
    Navbar(),
    Notification(),
    OrgUnits(),
    Orphan(),
    Person(),
    PhysicalFlows(),
    PhysicalSpecifications(),
    PhysicalField(),
    Playpen(),
    Playpen5(),
    Profile(),
    Ratings(),
    Roadmap(),
    Role(),
    Scenario(),
    SharedPreference(),
    ServerInfo(),
    SoftwareCatalog(),
    SourceDataRating(),
    StaticPanel(),
    Survey(),
    SvgDiagram(),
    System(),
    Technology(),
    User(),
    UserContribution(),
    TaxonomyManagement(),
    Welcome(),
    Widgets()
];


export default dependencies;
