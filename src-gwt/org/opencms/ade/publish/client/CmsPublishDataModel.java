/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.publish.client;

import org.opencms.ade.publish.client.CmsPublishItemStatus.Signal;
import org.opencms.ade.publish.client.CmsPublishItemStatus.State;
import org.opencms.ade.publish.shared.CmsPublishGroup;
import org.opencms.ade.publish.shared.CmsPublishResource;
import org.opencms.ade.publish.shared.CmsPublishResourceInfo.Type;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

/**
 * This class contains the data for the publish resources which are displayed
 * in the publish dialog.<p>
 *
 * @since 8.0.0
 */
public class CmsPublishDataModel {

    /**
     * Predicate used to check if a resource has problems.<p>
     */
    public static class HasProblems implements I_CmsPublishResourceCheck {

        /**
         * @see org.opencms.ade.publish.client.CmsPublishDataModel.I_CmsPublishResourceCheck#check(org.opencms.ade.publish.shared.CmsPublishResource)
         */
        public boolean check(CmsPublishResource res) {

            return hasProblems(res);
        }
    }

    /**
     * Predicate for testing properties of publish resources.<p>
     */
    public static interface I_CmsPublishResourceCheck {

        /**
         * Applies a boolean test to the publish resource and returns the result.<p>
         *
         * @param res the publish resource
         * @return the result
         */
        boolean check(CmsPublishResource res);

    }

    /** The original publish groups. */
    private List<CmsPublishGroup> m_groups;

    /** The structure ids for each group. */
    private List<List<CmsUUID>> m_idsByGroup = Lists.newArrayList();

    /** The publish resources indexed by UUID. */
    private Map<CmsUUID, CmsPublishResource> m_publishResources = Maps.newHashMap();

    /** The publish resources indexed by path. */
    private Map<String, CmsPublishResource> m_publishResourcesByPath = Maps.newHashMap();

    /** Map from uuids of publish resources to uuids of their related resources. */
    private Multimap<CmsUUID, CmsUUID> m_relatedIds = ArrayListMultimap.create();

    /** Map containing the related publish resources. */
    private Map<CmsUUID, CmsPublishResource> m_relatedPublishResources = Maps.newHashMap();

    /** The action to execute when the selection changes. */
    private Runnable m_selectionChangeAction;

    /** The item status bean, indexed by structure id. */
    private Map<CmsUUID, CmsPublishItemStatus> m_status = Maps.newHashMap();

    /**
     * Creates and initializes a new publish resource data model from a list of publish groups.<p>
     *
     * @param publishGroups the original publish groups
     * @param handler the handler which should be notified of state changes
     */
    public CmsPublishDataModel(List<CmsPublishGroup> publishGroups, I_CmsPublishItemStatusUpdateHandler handler) {

        m_groups = publishGroups;
        for (CmsPublishGroup group : publishGroups) {
            List<CmsUUID> idList = new ArrayList<CmsUUID>();
            m_idsByGroup.add(idList);
            for (CmsPublishResource res : group.getResources()) {
                CmsPublishItemStatus status = new CmsPublishItemStatus(
                    res.getId(),
                    State.normal,
                    hasProblems(res),
                    handler);
                m_status.put(res.getId(), status);
                m_publishResources.put(res.getId(), res);
                m_publishResourcesByPath.put(res.getName(), res);
                idList.add(res.getId());
                for (CmsPublishResource related : res.getRelated()) {
                    m_relatedIds.put(res.getId(), related.getId());
                    m_relatedPublishResources.put(related.getId(), related);
                }

            }
        }
    }

    /**
     * Returns if the given publish resource has problems preventing it from being published.<p>
     *
     * @param publishResource the publish resource
     *
     * @return <code>true</code> if the publish resource has problems
     */
    public static boolean hasProblems(CmsPublishResource publishResource) {

        return (publishResource.getInfo() != null) && publishResource.getInfo().hasProblemType();
    }

    /**
     * Collects group selection states.<p>
     *
     * @return the group selection states
     */
    public Map<Integer, CmsPublishItemStateSummary> computeGroupSelectionStates() {

        Map<Integer, CmsPublishItemStateSummary> stateMap = Maps.newHashMap();

        CmsPublishItemStateSummary allStates = new CmsPublishItemStateSummary();
        int i = 0;
        for (CmsPublishGroup group : m_groups) {
            CmsPublishItemStateSummary groupStates = new CmsPublishItemStateSummary();
            for (CmsPublishResource res : group.getResources()) {
                CmsPublishItemStatus item = m_status.get(res.getId());
                CmsPublishItemStatus.State stateToAdd;
                if (item.isDisabled()) {
                    // a disabled item should have no influence on the select/deselect all checkboxes,
                    // just as an item which is marked to be removed
                    stateToAdd = CmsPublishItemStatus.State.remove;
                } else {
                    stateToAdd = item.getState();
                }
                groupStates.addState(stateToAdd);
                allStates.addState(stateToAdd);

            }
            stateMap.put(Integer.valueOf(i), groupStates);
            i += 1;
        }
        stateMap.put(Integer.valueOf(-1), allStates);
        return stateMap;
    }

    /**
     * Counts the resources which have problems.<p>
     *
     * @return the number of resources which have problems
     */
    public int countProblems() {

        return countResources(new HasProblems());
    }

    /**
     * Counts the resources which pass a given check.<p>
     *
     * @param check the check to apply
     *
     * @return the number of resources which passed the check
     */
    public int countResources(I_CmsPublishResourceCheck check) {

        int count = 0;
        for (CmsPublishGroup group : m_groups) {
            count += countResourcesInGroup(check, group.getResources());
        }
        return count;
    }

    /**
     * Counts the resources of a group which pass a given check.<p>
     *
     * @param check the check to apply
     * @param group the group of publish resources
     *
     * @return the number of resources in that group which passed the check
     */
    public int countResourcesInGroup(I_CmsPublishResourceCheck check, List<CmsPublishResource> group) {

        int result = 0;
        for (CmsPublishResource res : group) {
            if (check.check(res)) {
                result += 1;
            }
        }
        return result;

    }

    /**
     * Gets the list of publish groups.<p>
     *
     * @return the list of publish groups
     */
    public List<CmsPublishGroup> getGroups() {

        return m_groups;
    }

    /**
     * Gets the ids for a given publish group.<p>
     *
     * @param groupNum the index of the group
     *
     * @return the UUIDs for that group
     */
    public List<CmsUUID> getIdsForGroup(int groupNum) {

        return m_idsByGroup.get(groupNum);
    }

    /**
     * Returns the id's of all already published resources.<p>
     *
     * @return the id's of the already published resources
     */
    public List<CmsUUID> getIdsOfAlreadyPublishedResources() {

        List<CmsUUID> alreadyPublished = new ArrayList<CmsUUID>();
        List<CmsPublishResource> allResources = new ArrayList<CmsPublishResource>();
        for (CmsPublishGroup group : m_groups) {

            for (CmsPublishResource resource : group.getResources()) {
                allResources.add(resource);
                for (CmsPublishResource related : resource.getRelated()) {
                    allResources.add(related);
                }
            }

        }
        for (CmsPublishResource resource : allResources) {
            if ((resource.getInfo() != null) && (resource.getInfo().getType() == Type.PUBLISHED)) {
                alreadyPublished.add(resource.getId());
            }
        }
        return alreadyPublished;
    }

    /**
     * Returns the ids of publish resources which should be published.<p>
     *
     * @return the ids of publish resources which should be published
     */
    public Set<CmsUUID> getPublishIds() {

        Set<CmsUUID> toPublish = new HashSet<CmsUUID>();
        for (Map.Entry<CmsUUID, CmsPublishItemStatus> entry : m_status.entrySet()) {
            CmsUUID key = entry.getKey();
            CmsPublishItemStatus status = entry.getValue();
            if (status.getState() == State.publish) {
                toPublish.add(key);
                for (CmsUUID relatedId : m_relatedIds.get(key)) {
                    CmsPublishResource relatedResource = m_relatedPublishResources.get(relatedId);
                    if ((relatedResource != null) && !hasProblems(relatedResource)) {
                        toPublish.add(relatedId);
                    }
                }
            }
        }
        return toPublish;
    }

    /**
     * Returns the list of all publish resources.<p>
     *
     * @return the list of all publish resources
     */
    public Map<CmsUUID, CmsPublishResource> getPublishResources() {

        return m_publishResources;
    }

    /**
     * Returns the map of publish resources by path.<p>
     *
     * @return the map of publish resources by path
     */
    public Map<String, CmsPublishResource> getPublishResourcesByPath() {

        return m_publishResourcesByPath;
    }

    /**
     * Returns the ids of publish resources which should be removed.<p>
     *
     * @return the ids of publish resources which should be removed
     */
    public List<CmsUUID> getRemoveIds() {

        List<CmsUUID> toRemove = new ArrayList<CmsUUID>();
        for (Map.Entry<CmsUUID, CmsPublishItemStatus> entry : m_status.entrySet()) {
            CmsUUID key = entry.getKey();
            CmsPublishItemStatus status = entry.getValue();
            if (status.getState() == State.remove) {
                toRemove.add(key);
            }
        }
        return toRemove;
    }

    /**
     * Returns the status for a given publish resource id.<p>
     *
     * @param id the publish resource's structure id
     * @return the status for that publish resource
     */
    public CmsPublishItemStatus getStatus(CmsUUID id) {

        return m_status.get(id);
    }

    /**
     * Checks if there is only a single group of resources.<p>
     *
     * @return true if there is only a single group of resources
     */
    public boolean hasSingleGroup() {

        return m_groups.size() == 1;
    }

    /**
     * Checks if there are any publish resources.<p>
     *
     * @return true if there are no publish resources at all
     */
    public boolean isEmpty() {

        return m_status.isEmpty();
    }

    /**
     * Sets the action which should be executed when the selection changes.<p>
     *
     * @param action the action to run when the selection changes
     */
    public void setSelectionChangeAction(Runnable action) {

        m_selectionChangeAction = action;
    }

    /**
     * Sends a signal to a publish item status bean with the given id.<p>
     *
     * @param signal the signal
     * @param id the structure id
     */
    public void signal(Signal signal, CmsUUID id) {

        getStatus(id).handleSignal(signal);
        runSelectionChangeAction();
    }

    /**
     * Sends a signal to all publish item status beans.<p>
     *
     * @param signal the signal
     */
    public void signalAll(Signal signal) {

        for (Map.Entry<CmsUUID, CmsPublishItemStatus> entry : m_status.entrySet()) {
            entry.getValue().handleSignal(signal);
        }
        runSelectionChangeAction();
    }

    /**
     * Sends a signal to all publish items in a given group.<p>
     *
     * @param signal the signal to send
     * @param groupNum the group index
     */
    public void signalGroup(Signal signal, int groupNum) {

        CmsPublishGroup group = m_groups.get(groupNum);
        for (CmsPublishResource res : group.getResources()) {
            CmsUUID id = res.getId();
            m_status.get(id).handleSignal(signal);
        }
        runSelectionChangeAction();
    }

    /**
     * Executes the action defined for selection changes.<p>
     */
    private void runSelectionChangeAction() {

        if (m_selectionChangeAction != null) {
            m_selectionChangeAction.run();
        }
    }

}
