/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.relations;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.CmsVfsException;
import org.opencms.file.CmsVfsResourceNotFoundException;
import org.opencms.main.CmsException;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Read-only category validation against the current and projected VFS trees for one move. */
class CmsCategoryMoveCheck {

    /** A view maps future paths back to existing resources, without changing those resources. */
    private class View {

        /** Whether to project the move. */
        private boolean m_projected;

        /** Resource lookups, including missing resources. */
        private Map<String, CmsResource> m_resources = new HashMap<>();

        /** Repository lookups by reference path. */
        private Map<String, List<String>> m_repositories = new HashMap<>();

        /**
         * @param projected whether to project the move
         */
        View(boolean projected) {

            m_projected = projected;
        }

        /**
         * @param reference the reference root path
         * @param category the logical category path
         * @return whether the category resolves in this view
         * @throws CmsException if lookup fails
         */
        boolean contains(String reference, String category) throws CmsException {

            for (String repository : repositories(reference)) {
                CmsResource resource = read(CmsStringUtil.joinPaths(repository, category));
                if ((resource != null) && CmsResourceFilter.DEFAULT.isValid(m_cms.getRequestContext(), resource)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Reads a resource with ALL, preserving deleted resources for property inheritance.
         *
         * @param path the root path in this view
         * @return the original resource, or null if absent
         * @throws CmsException if lookup fails for a reason other than absence
         */
        CmsResource read(String path) throws CmsException {

            if (!m_resources.containsKey(path)) {
                String original = path;
                if (m_projected) {
                    if (inSubtree(path, m_destination)) {
                        original = m_source + path.substring(m_destination.length());
                    } else if (inSubtree(path, m_source)) {
                        m_resources.put(path, null);
                        return null;
                    }
                }
                CmsResource resource = null;
                try {
                    resource = m_cms.readResource(original, CmsResourceFilter.ALL);
                } catch (CmsVfsResourceNotFoundException e) {
                    // Absence is expected when evaluating a prospective destination.
                }
                m_resources.put(path, resource);
            }
            return m_resources.get(path);
        }

        /**
         * @param reference the reference root path
         * @return available repositories, in the category service's resolution order
         * @throws CmsException if lookup fails
         */
        List<String> repositories(String reference) throws CmsException {

            if (!m_repositories.containsKey(reference)) {
                List<String> result = new ArrayList<>();
                for (String path : m_service.getCategoryRepositoryCandidates(
                    reference,
                    m_baseFolder,
                    repositoryProperty(reference))) {
                    if (path.equals(CmsCategoryService.CENTRALIZED_REPOSITORY)) {
                        result.add(path);
                    } else {
                        CmsResource resource = read(path);
                        if ((resource != null)
                            && CmsResourceFilter.DEFAULT.isValid(m_cms.getRequestContext(), resource)) {
                            result.add(path);
                        }
                    }
                }
                m_repositories.put(reference, result);
            }
            return m_repositories.get(reference);
        }

        /**
         * Resolves the inherited property along the ancestry of this view.
         *
         * @param reference the reference root path
         * @return the inherited repository value, or null
         * @throws CmsException if lookup fails
         */
        String repositoryProperty(String reference) throws CmsException {

            for (String path = reference; path != null; path = CmsResource.getParentFolder(path)) {
                CmsResource resource = read(path);
                if (resource != null) {
                    CmsProperty property = m_cms.readPropertyObject(
                        resource,
                        CmsPropertyDefinition.PROPERTY_CATEGORY_REPOSITORY,
                        false);
                    if (!property.isNullProperty()) {
                        return property.getValue();
                    }
                }
            }
            return null;
        }
    }

    /** CMS context, rooted at the VFS root. */
    private CmsObject m_cms;

    /** Category service. */
    private CmsCategoryService m_service;

    /** The move root resource used for the bulk relation query. */
    private CmsResource m_moveRoot;

    /** Source root path. */
    private String m_source;

    /** Destination root path. */
    private String m_destination;

    /** Configured category repository folder name. */
    private String m_baseFolder;

    /** Whether the move root is a folder. */
    private boolean m_folder;

    /**
     * @param cms the CMS context with empty site root
     * @param service the category service
     * @param source the move root
     * @param destination the destination root path
     */
    CmsCategoryMoveCheck(CmsObject cms, CmsCategoryService service, CmsResource source, String destination) {

        m_cms = cms;
        m_service = service;
        m_moveRoot = source;
        m_source = source.getRootPath();
        m_folder = source.isFolder();
        m_destination = m_folder && !destination.endsWith("/") ? destination + "/" : destination;
        m_baseFolder = service.getRepositoryBaseFolderName(cms);
    }

    /**
     * @param resources the complete move subtree
     * @throws CmsException if a valid assigned category would be lost
     */
    void check(List<CmsResource> resources) throws CmsException {

        CmsRelationFilter filter = CmsRelationFilter.TARGETS.filterType(CmsRelationType.CATEGORY);
        if (m_folder) {
            filter = filter.filterIncludeChildren();
        }
        List<CmsRelation> relations = m_cms.getRelationsForResource(m_moveRoot, filter);
        if (relations.isEmpty()) {
            return;
        }
        Map<CmsUUID, List<CmsRelation>> relationsBySource = new HashMap<>();
        for (CmsRelation relation : relations) {
            relationsBySource.computeIfAbsent(relation.getSourceId(), id -> new ArrayList<>()).add(relation);
        }
        View before = new View(false);
        View after = new View(true);
        List<CmsResource> ordered = new ArrayList<>();
        // Reuse the complete driver traversal. Descendants need not be readable by
        // the moving user, so looking them up again through a filtered API is unsafe.
        for (CmsResource resource : resources) {
            String path = resource.getRootPath();
            before.m_resources.put(path, resource);
            after.m_resources.put(m_destination + path.substring(m_source.length()), resource);
            if (relationsBySource.containsKey(resource.getStructureId())) {
                ordered.add(resource);
            }
        }
        Collections.sort(ordered, (a, b) -> a.getRootPath().compareTo(b.getRootPath()));
        for (CmsResource resource : ordered) {
            String sourcePath = resource.getRootPath();
            String targetPath = m_destination + sourcePath.substring(m_source.length());
            for (CmsRelation relation : relationsBySource.get(resource.getStructureId())) {
                CmsResource category;
                try {
                    category = relation.getTarget(m_cms, CmsResourceFilter.DEFAULT_FOLDERS);
                } catch (CmsVfsResourceNotFoundException e) {
                    // A broken assignment is not a category lost by this move.
                    continue;
                }
                String rootPath = category.getRootPath();
                if (!rootPath.startsWith(CmsCategoryService.CENTRALIZED_REPOSITORY)
                    && !rootPath.contains(m_baseFolder)) {
                    // The relation target no longer belongs to a category repository.
                    continue;
                }
                String categoryPath = CmsCategory.getCategoryPath(rootPath, m_baseFolder);
                if (before.contains(sourcePath, categoryPath) && !after.contains(targetPath, categoryPath)) {
                    throw new CmsVfsException(
                        Messages.get().container(
                            Messages.ERR_MOVE_CATEGORY_LOST_3,
                            sourcePath,
                            targetPath,
                            categoryPath));
                }
            }
        }
    }

    /**
     * @param path a root path
     * @param root a move root
     * @return whether the path is moved with this root
     */
    private boolean inSubtree(String path, String root) {

        return path.equals(root) || (m_folder && path.startsWith(root));
    }
}
