/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.acacia.client;

import org.opencms.acacia.shared.CmsContentDefinition;
import org.opencms.acacia.shared.CmsValidationResult;
import org.opencms.util.CmsPair;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The validation context. Keeps track of valid and invalid entity id's as well as of entity ids with warnings attached.<p>
 */
public class CmsValidationContext {

    /** The invalid entity id's with error path information. */
    private Map<String, Set<String>> m_invalidEntityIds;

    /** The warning entity id's with warning path information. */
    private Map<String, Set<String>> m_warningEntityIds;

    /** The valid entity id's. */
    private Set<String> m_validEntityIds;

    /** The synchronized paths. */
    private Set<String> m_synchronizedPaths;

    /**
     * Constructor.<p>
     */
    public CmsValidationContext() {

        m_invalidEntityIds = new HashMap<>();
        m_validEntityIds = new HashSet<>();
        m_warningEntityIds = new HashMap<>();
    }

    /**
     * Constructor, starting with an initial validation result.
     * @param validationResult the validation result to initialize the context with.
     * @param synchronizedPaths paths that should be synchronized.
     */
    public CmsValidationContext(CmsValidationResult validationResult, Set<String> synchronizedPaths) {

        m_invalidEntityIds = new HashMap<>();
        m_validEntityIds = new HashSet<>();
        m_warningEntityIds = new HashMap<>();

        for (String entityId : validationResult.getWarnings().keySet()) {
            if (validationResult.hasWarnings(entityId)) {
                setWarningEntity(entityId, validationResult.getWarnings(entityId));
            }
        }
        for (String entityId : validationResult.getErrors().keySet()) {
            if (validationResult.hasErrors(entityId)) {
                setInvalidEntity(entityId, validationResult.getErrors(entityId));
            }
        }

        m_synchronizedPaths = synchronizedPaths;

    }

    /**
     * Clears a warning entity id.<p>
     *
     * @param entityId the entity id
     */
    public void clearWarningEntity(String entityId) {

        Set<String> currentPaths = m_warningEntityIds.get(entityId);
        updateSyncWarnings(currentPaths, null);
        m_warningEntityIds.remove(entityId);
    }

    /**
     * Returns the invalid entity id's.<p>
     *
     * @return the invalid entity id's
     */
    public Set<String> getInvalidEntityIds() {

        return m_invalidEntityIds.keySet();
    }

    /**
     * Returns the valid entity id's.<p>
     *
     * @return the valid entity id's
     */
    public Set<String> getValidEntityIds() {

        return m_validEntityIds;
    }

    /**
     * Returns the warning entity id's.<p>
     *
     * @return the warning entity id's
     */
    public Set<String> getWarningEntityIds() {

        return m_warningEntityIds.keySet();
    }

    /**
     * Returns if there are any invalid entities.<p>
     *
     * @return <code>true</code>  if there are any invalid entities
     */
    public boolean hasValidationErrors() {

        return !m_invalidEntityIds.isEmpty();
    }

    /**
     * Returns if there are any warning entities.<p>
     *
     * @return <code>true</code>  if there are any warning entities
     */
    public boolean hasValidationWarnings() {

        return !m_warningEntityIds.isEmpty();
    }

    /**
     * Removes the given entity id, use when validating the entity is no longer required.<p>
     *
     * @param entityId the entity id
     */
    public void removeEntityId(String entityId) {

        m_invalidEntityIds.remove(entityId);
        m_validEntityIds.remove(entityId);
        m_warningEntityIds.remove(entityId);
    }

    /**
     * Sets an entity as invalid and adds the error paths.<p>
     *
     * @param entityId the entity id
     * @param errors the errors for the entity
     */
    public void setInvalidEntity(String entityId, Map<String[], CmsPair<String, String>> errors) {

        Set<String> newPaths = extractPaths(errors);
        updateSyncErrors(m_invalidEntityIds.get(entityId), newPaths);
        m_validEntityIds.remove(entityId);
        m_invalidEntityIds.put(entityId, newPaths);
    }

    /**
     * Sets an entity as valid.<p>
     *
     * @param entityId the entity id
     */
    public void setValidEntity(String entityId) {

        updateSyncErrors(m_invalidEntityIds.get(entityId), null);
        m_invalidEntityIds.remove(entityId);
        m_validEntityIds.add(entityId);
    }

    /**
     * Sets warnings for an entity id.<p>
     *
     * @param entityId the entity id
     * @param warnings the warnings for the entity
     */
    public void setWarningEntity(String entityId, Map<String[], CmsPair<String, String>> warnings) {

        Set<String> newPaths = extractPaths(warnings);
        Set<String> currentPaths = m_warningEntityIds.get(entityId);
        updateSyncWarnings(currentPaths, newPaths);
        m_warningEntityIds.put(entityId, newPaths);
    }

    /**
     * Extracts the path information for synchronization comparison from the warning/error information.
     * @param issues the warnings/errors as returned from the validation result.
     * @return the path information. I.e., paths without indexes.
     */
    private Set<String> extractPaths(Map<String[], CmsPair<String, String>> issues) {

        Set<String> result = new HashSet<>(issues.keySet().size());
        for (String[] e : issues.keySet()) {
            String path = "";
            for (int i = 0; i < e.length; i++) {
                String ep = e[i];
                if (ep != "ATTRIBUTE_CHOICE") {
                    path += CmsContentDefinition.removeIndex(ep.substring(ep.lastIndexOf('/')));
                }
            }
            result.add(path.substring(1));
        }
        return result;
    }

    /**
     * Returns the paths to remove from all entities due to synchronization, when the provided current
     * issue paths are updated to the new ones.
     * @param currentPaths the current issue paths
     * @param newPaths the new issue paths, replacing the current ones
     * @return the removed paths for which synchronization is needed.
     */
    private Set<String> getPathsToSyncRemovedIssues(Set<String> currentPaths, Set<String> newPaths) {

        if ((m_synchronizedPaths != null) && !m_synchronizedPaths.isEmpty()) {
            Set<String> result = new HashSet<>(m_synchronizedPaths.size());
            if ((currentPaths != null) && !currentPaths.isEmpty()) {
                Set<String> intersection = new HashSet<>(m_synchronizedPaths);
                intersection.retainAll(currentPaths);
                for (String p : intersection) {
                    if ((null == newPaths) || !newPaths.contains(p)) {
                        result.add(p);
                    }
                }
            }
            return result;
        }
        return Collections.emptySet();
    }

    /**
     * Removes errors for all entities, if an error for a synchronized element is removed.
     * @param currentPaths the current error paths
     * @param newPaths the error paths that replace the current ones
     */
    private void updateSyncErrors(Set<String> currentPaths, Set<String> newPaths) {

        Set<String> pathsToUpdate = getPathsToSyncRemovedIssues(currentPaths, newPaths);
        if (!pathsToUpdate.isEmpty()) {
            Set<String> invalidEntities = new HashSet<>(m_invalidEntityIds.keySet());
            for (String entity : invalidEntities) {
                Set<String> paths = m_invalidEntityIds.get(entity);
                paths.removeAll(pathsToUpdate);
                if (paths.isEmpty()) {
                    m_invalidEntityIds.remove(entity);
                    m_validEntityIds.add(entity);
                }
            }
        }
    }

    /**
     * Removes warnings for all entities, if a warning for a synchronized element is removed.
     * @param currentPaths the current error paths
     * @param newPaths the error paths that replace the current ones
     */
    private void updateSyncWarnings(Set<String> currentPaths, Set<String> newPaths) {

        Set<String> pathsToUpdate = getPathsToSyncRemovedIssues(currentPaths, newPaths);
        if (!pathsToUpdate.isEmpty()) {
            Set<String> warningEntities = new HashSet<>(m_warningEntityIds.keySet());
            for (String entity : warningEntities) {
                Set<String> paths = m_warningEntityIds.get(entity);
                paths.removeAll(pathsToUpdate);
                if (paths.isEmpty()) {
                    m_warningEntityIds.remove(entity);
                }
            }
        }
    }
}
