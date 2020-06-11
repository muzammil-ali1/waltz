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

package com.khartec.waltz.service.app_group;

import com.khartec.waltz.common.Checks;
import com.khartec.waltz.common.exception.InsufficientPrivelegeException;
import com.khartec.waltz.data.app_group.AppGroupDao;
import com.khartec.waltz.data.app_group.AppGroupEntryDao;
import com.khartec.waltz.data.app_group.AppGroupMemberDao;
import com.khartec.waltz.data.app_group.AppGroupOrganisationalUnitDao;
import com.khartec.waltz.data.application.ApplicationDao;
import com.khartec.waltz.data.entity_relationship.EntityRelationshipDao;
import com.khartec.waltz.data.orgunit.OrganisationalUnitDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.app_group.*;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.change_initiative.ChangeInitiative;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.entity_relationship.EntityRelationship;
import com.khartec.waltz.model.entity_relationship.ImmutableEntityRelationship;
import com.khartec.waltz.model.entity_relationship.RelationshipKind;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.orgunit.OrganisationalUnit;
import com.khartec.waltz.service.change_initiative.ChangeInitiativeService;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.ListUtilities.append;
import static com.khartec.waltz.common.MapUtilities.indexBy;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.model.IdSelectionOptions.mkOpts;
import static java.lang.String.format;

@Service
public class AppGroupService {

    private final AppGroupDao appGroupDao;
    private final AppGroupMemberDao appGroupMemberDao;
    private final AppGroupEntryDao appGroupEntryDao;
    private final ApplicationDao applicationDao;
    private final AppGroupOrganisationalUnitDao appGroupOrganisationalUnitDao;
    private final OrganisationalUnitDao organisationalUnitDao;
    private final EntityRelationshipDao entityRelationshipDao;
    private final ChangeInitiativeService changeInitiativeService;
    private final ChangeLogService changeLogService;


    @Autowired
    public AppGroupService(AppGroupDao appGroupDao,
                           AppGroupMemberDao appGroupMemberDao,
                           AppGroupEntryDao appGroupEntryDao,
                           ApplicationDao applicationDao,
                           AppGroupOrganisationalUnitDao appGroupOrganisationalUnitDao,
                           OrganisationalUnitDao organisationalUnitDao,
                           EntityRelationshipDao entityRelationshipDao,
                           ChangeInitiativeService changeInitiativeService,
                           ChangeLogService changeLogService) {
        checkNotNull(appGroupDao, "appGroupDao cannot be null");
        checkNotNull(appGroupEntryDao, "appGroupEntryDao cannot be null");
        checkNotNull(appGroupEntryDao, "appGroupEntryDao cannot be null");
        checkNotNull(applicationDao, "applicationDao cannot be null");
        checkNotNull(appGroupOrganisationalUnitDao, "appGroupOrganisationalUnitDao cannot be null");
        checkNotNull(organisationalUnitDao, "organisationalUnitDao cannot be null");
        checkNotNull(entityRelationshipDao, "entityRelationshipDao cannot be null");
        checkNotNull(changeInitiativeService, "changeInitiativeService cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.appGroupDao = appGroupDao;
        this.appGroupMemberDao = appGroupMemberDao;
        this.appGroupEntryDao = appGroupEntryDao;
        this.applicationDao = applicationDao;
        this.appGroupOrganisationalUnitDao = appGroupOrganisationalUnitDao;
        this.organisationalUnitDao = organisationalUnitDao;
        this.entityRelationshipDao = entityRelationshipDao;
        this.changeInitiativeService = changeInitiativeService;
        this.changeLogService = changeLogService;
    }


    public AppGroupDetail getGroupDetailById(long groupId) {
        return ImmutableAppGroupDetail.builder()
                .appGroup(appGroupDao.getGroup(groupId))
                .applications(appGroupEntryDao.getEntriesForGroup(groupId))
                .members(appGroupMemberDao.getMembers(groupId))
                .organisationalUnits(appGroupOrganisationalUnitDao.getEntriesForGroup(groupId))
                .build();
    }


    public List<AppGroupSubscription> findGroupSubscriptionsForUser(String userId) {
        List<AppGroupMember> subscriptions = appGroupMemberDao.getSubscriptions(userId);
        Map<Long, AppGroupMemberRole> roleByGroup = indexBy(
                m -> m.groupId(),
                m -> m.role(),
                subscriptions);

        List<AppGroup> groups = appGroupDao.findGroupsForUser(userId);

        return groups
                .stream()
                .map(g -> ImmutableAppGroupSubscription.builder()
                        .appGroup(g)
                        .role(roleByGroup.get(g.id().get()))
                        .build())
                .collect(Collectors.toList());
    }


    public List<AppGroup> findPublicGroups() {
        return appGroupDao.findPublicGroups();
    }


    public List<AppGroup> findPrivateGroupsByOwner(String ownerId) {
        return appGroupDao.findPrivateGroupsByOwner(ownerId);
    }


    public List<AppGroup> findRelatedByEntityReference(EntityReference ref, String username) {
        switch (ref.kind()) {
            case APPLICATION:
                return appGroupDao.findRelatedByApplicationId(ref.id(), username);
            default:
                return appGroupDao.findRelatedByEntityReference(ref, username);
        }
    }


    public List<AppGroup> search(EntitySearchOptions options) {
        return appGroupDao.search(options);
    }


    public void subscribe(String userId, long groupId) {
        audit(groupId,
                userId,
                "Subscribed to group " + appGroupDao.getGroup(groupId).name(),
                EntityKind.PERSON,
                Operation.ADD);
        appGroupMemberDao.register(groupId, userId);
    }


    public void unsubscribe(String userId, long groupId) {
        audit(groupId,
                userId,
                "Unsubscribed from group" + appGroupDao.getGroup(groupId).name(),
                EntityKind.PERSON,
                Operation.REMOVE);
        appGroupMemberDao.unregister(groupId, userId);
    }


    public List<AppGroupSubscription> deleteGroup(String userId, long groupId) throws InsufficientPrivelegeException {
        verifyUserCanUpdateGroup(userId, groupId);
        appGroupDao.deleteGroup(groupId);
        entityRelationshipDao.removeAnyInvolving(mkRef(EntityKind.APP_GROUP, groupId));
        audit(groupId, userId, format("Removed group %d", groupId), null, Operation.REMOVE);
        return findGroupSubscriptionsForUser(userId);
    }


    public List<EntityReference> addApplication(String userId, long groupId, long applicationId) throws InsufficientPrivelegeException {

        verifyUserCanUpdateGroup(userId, groupId);

        Application app = applicationDao.getById(applicationId);
        if (app != null) {
            appGroupEntryDao.addApplication(groupId, applicationId);
            audit(groupId, userId, format("Added application %s to group", app.name()), EntityKind.APPLICATION, Operation.ADD);
        }

        return appGroupEntryDao.getEntriesForGroup(groupId);
    }


    public List<EntityReference> addApplications(String userId,
                                                 long groupId,
                                                 List<Long> applicationIds,
                                                 List<String> unknownIdentifiers) throws InsufficientPrivelegeException {
        verifyUserCanUpdateGroup(userId, groupId);

        appGroupEntryDao.addApplications(groupId, applicationIds);

        EntityReference entityReference = mkRef(EntityKind.APP_GROUP, groupId);
        List<Application> apps = applicationDao.findByIds(applicationIds);

        List<ChangeLog> addedApplicationChangeLogs = apps
                .stream()
                .map(app -> ImmutableChangeLog.builder()
                            .message(format("Added application %s to group", app.name()))
                            .userId(userId)
                            .parentReference(entityReference)
                            .childKind(EntityKind.APPLICATION)
                            .operation(Operation.ADD)
                            .build())
                .collect(Collectors.toList());

        List<ChangeLog> changeLogs = (unknownIdentifiers.size() == 0)
                ? addedApplicationChangeLogs
                : append(addedApplicationChangeLogs,
                        ImmutableChangeLog.builder()
                                .message(format("The following {%d} identifiers could not be found and were not added to this group: %s", unknownIdentifiers.size(), unknownIdentifiers))
                                .userId(userId)
                                .parentReference(entityReference)
                                .childKind(EntityKind.APPLICATION)
                                .operation(Operation.UNKNOWN)
                                .build());

        changeLogService.write(changeLogs);

        return appGroupEntryDao.getEntriesForGroup(groupId);
    }


    public List<EntityReference> removeApplication(String userId, long groupId, long applicationId) throws InsufficientPrivelegeException {
        verifyUserCanUpdateGroup(userId, groupId);
        appGroupEntryDao.removeApplication(groupId, applicationId);
        Application app = applicationDao.getById(applicationId);
        audit(groupId, userId, format(
                    "Removed application %s from group",
                    app != null
                        ? app.name()
                        : applicationId),
                EntityKind.APPLICATION,
                Operation.REMOVE);
        return appGroupEntryDao.getEntriesForGroup(groupId);
    }

    public List<EntityReference> addOrganisationalUnit(String userId, long groupId, long orgUnitId) throws InsufficientPrivelegeException {

        verifyUserCanUpdateGroup(userId, groupId);
        OrganisationalUnit orgUnit = organisationalUnitDao.getById(orgUnitId);
        if (orgUnit != null) {
            appGroupOrganisationalUnitDao.addOrgUnit(groupId, orgUnitId);
            audit(groupId, userId, format("Added application %s to group", orgUnit.name()), EntityKind.ORG_UNIT, Operation.ADD);
        }
        return appGroupOrganisationalUnitDao.getEntriesForGroup(groupId);
    }

    public List<EntityReference> removeOrganisationalUnit(String userId, long groupId, long orgUnitId) throws InsufficientPrivelegeException {
        verifyUserCanUpdateGroup(userId, groupId);
        appGroupOrganisationalUnitDao.removeOrgUnit(groupId, orgUnitId);
        OrganisationalUnit ou = organisationalUnitDao.getById(orgUnitId);
        audit(groupId, userId, format("Removed application %s from group", ou != null ? ou.name() : orgUnitId), EntityKind.ORG_UNIT, Operation.REMOVE);
        return appGroupOrganisationalUnitDao.getEntriesForGroup(groupId);
    }
    public List<EntityReference> removeApplications(String userId, long groupId, List<Long> applicationIds) throws InsufficientPrivelegeException {
        verifyUserCanUpdateGroup(userId, groupId);

        appGroupEntryDao.removeApplications(groupId, applicationIds);

        List<Application> apps = applicationDao.findByIds(applicationIds);
        List<ChangeLog> changeLogs = apps
                .stream()
                .map(app -> ImmutableChangeLog.builder()
                        .message(format("Removed application %s from group", app.name()))
                        .userId(userId)
                        .parentReference(ImmutableEntityReference.builder().id(groupId).kind(EntityKind.APP_GROUP).build())
                        .childKind(EntityKind.APPLICATION)
                        .operation(Operation.REMOVE)
                        .build())
                .collect(Collectors.toList());
        changeLogService.write(changeLogs);

        return appGroupEntryDao.getEntriesForGroup(groupId);
    }


    public int addOwner(String userId, long groupId, String ownerId) throws InsufficientPrivelegeException {
        verifyUserCanUpdateGroup(userId, groupId);
        audit(groupId, userId, format("Added owner %s to group %d", ownerId, groupId), EntityKind.PERSON, Operation.ADD);
        return appGroupMemberDao.register(groupId, ownerId, AppGroupMemberRole.OWNER);
    }


    public boolean removeOwner(String userId, long groupId, String ownerToRemoveId) throws InsufficientPrivelegeException {
        verifyUserCanUpdateGroup(userId, groupId);
        boolean result = appGroupMemberDao.unregister(groupId, ownerToRemoveId);
        subscribe(ownerToRemoveId, groupId);
        audit(groupId, userId, format("Removed owner %s from group %d", ownerToRemoveId, groupId), EntityKind.PERSON, Operation.REMOVE);
        return result;
    }


    public List<AppGroupMember> getMembers(long groupId) {
        return appGroupMemberDao.getMembers(groupId);
    }


    public AppGroupDetail updateOverview(String userId, AppGroup appGroup) throws InsufficientPrivelegeException {
        verifyUserCanUpdateGroup(userId, appGroup.id().get());
        appGroupDao.update(appGroup);
        audit(appGroup.id().get(), userId, "Updated group overview", null, Operation.UPDATE);
        return getGroupDetailById(appGroup.id().get());
    }


    public Long createNewGroup(String userId) {
        long groupId = appGroupDao.insert(ImmutableAppGroup.builder()
                .description("New group created by: " + userId)
                .name("New group created by: " + userId)
                .appGroupKind(AppGroupKind.PRIVATE)
                .build());

        appGroupMemberDao.register(groupId, userId, AppGroupMemberRole.OWNER);

        audit(groupId, userId, "Created group", null, Operation.ADD);


        return groupId;
    }


    public Collection<AppGroup> findByIds(String user, List<Long> ids) {
        Checks.checkNotEmpty(user, "user cannot be empty");
        checkNotNull(ids, "ids cannot be null");
        return appGroupDao.findByIds(user, ids);
    }


    public Collection<ChangeInitiative> addChangeInitiative(
            String username,
            long groupId,
            long changeInitiativeId) throws InsufficientPrivelegeException {
        verifyUserCanUpdateGroup(username, groupId);

        EntityRelationship entityRelationship = buildChangeInitiativeRelationship(username, groupId, changeInitiativeId);
        entityRelationshipDao.save(entityRelationship);

        audit(groupId,
                username,
                format("Associated change initiative: %d", changeInitiativeId),
                EntityKind.CHANGE_INITIATIVE,
                Operation.ADD);

        return changeInitiativeService.findForSelector(mkOpts(
                mkRef(EntityKind.APP_GROUP, groupId),
                HierarchyQueryScope.EXACT));
    }


    public Collection<ChangeInitiative> removeChangeInitiative(
            String username,
            long groupId,
            long changeInitiativeId) throws InsufficientPrivelegeException {
        verifyUserCanUpdateGroup(username, groupId);

        EntityRelationship entityRelationship = buildChangeInitiativeRelationship(username, groupId, changeInitiativeId);
        entityRelationshipDao.remove(entityRelationship);

        audit(groupId,
                username,
                format("Removed associated change initiative: %d", changeInitiativeId),
                EntityKind.CHANGE_INITIATIVE,
                Operation.REMOVE);

        return changeInitiativeService.findForSelector(mkOpts(
                mkRef(EntityKind.APP_GROUP, groupId),
                HierarchyQueryScope.EXACT));
    }


    private void verifyUserCanUpdateGroup(String userId, long groupId) throws InsufficientPrivelegeException {
        if (!appGroupMemberDao.canUpdate(groupId, userId)) {
            throw new InsufficientPrivelegeException(userId + " cannot update group: " + groupId);
        }
    }


    private EntityRelationship buildChangeInitiativeRelationship(String username, long groupId, long changeInitiativeId) {
        EntityReference appGroupRef = ImmutableEntityReference.builder()
                .kind(EntityKind.APP_GROUP)
                .id(groupId)
                .build();

        EntityReference changeInitiativeRef = ImmutableEntityReference.builder()
                .kind(EntityKind.CHANGE_INITIATIVE)
                .id(changeInitiativeId)
                .build();

        return ImmutableEntityRelationship.builder()
                .a(appGroupRef)
                .b(changeInitiativeRef)
                .relationship(RelationshipKind.RELATES_TO.name())
                .lastUpdatedBy(username)
                .provenance("waltz")
                .build();
    }


    private void audit(long groupId, String userId, String message, EntityKind childKind, Operation operation) {
        changeLogService.write(ImmutableChangeLog.builder()
                .message(message)
                .userId(userId)
                .parentReference(ImmutableEntityReference.builder().id(groupId).kind(EntityKind.APP_GROUP).build())
                .childKind(Optional.ofNullable(childKind))
                .operation(operation)
                .build());
    }
}
