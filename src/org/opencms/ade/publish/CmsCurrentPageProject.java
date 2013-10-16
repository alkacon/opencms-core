/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.publish;

import org.opencms.ade.publish.shared.CmsProjectBean;
import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.ade.publish.shared.CmsPublishResource;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeBinary;
import org.opencms.file.types.CmsResourceTypeImage;
import org.opencms.file.types.CmsResourceTypePlain;
import org.opencms.file.types.CmsResourceTypePointer;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.security.CmsPermissionViolationException;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;

/**
 * Virtual project which includes the currently edited resource and all its related resources.
 */
public class CmsCurrentPageProject implements I_CmsVirtualProject {

    /**
     * Context class for the 'current page' project.<p>
     */
    public class Context implements I_CmsVirtualProject.I_Context {

        /** The publish parameters. */
        protected Map<String, String> m_params;

        /** The current CMS context. */
        private CmsObject m_cms;

        /**
         * Creates a new context instance.<p>
         * 
         * @param cms the current CMS conte 
         * @param params
         */
        public Context(CmsObject cms, Map<String, String> params) {

            m_cms = cms;
            m_params = params;
        }

        /**
         * @see org.opencms.ade.publish.I_CmsVirtualProject.I_Context#getProjectBean()
         */
        public CmsProjectBean getProjectBean() {

            String pageId = m_params.get(CmsPublishOptions.PARAM_CONTAINERPAGE);
            String elementId = m_params.get(CmsPublishOptions.PARAM_CONTENT);
            Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms);
            String title = Messages.get().getBundle(locale).key(Messages.GUI_CURRENTPAGE_PROJECT_0);
            CmsUUID structureIdForTitle;
            if ((pageId == null) && (elementId == null)) {
                return null;
            } else {
                structureIdForTitle = pageId != null ? new CmsUUID(pageId) : new CmsUUID(elementId);
            }

            CmsProjectBean bean = new CmsProjectBean(ID, 0, title, title);
            bean.setRank(100);
            bean.setDefaultGroupName("");
            try {
                CmsResource titleResource = m_cms.readResource(structureIdForTitle, CmsResourceFilter.IGNORE_EXPIRATION);
                CmsProperty titleProp = m_cms.readPropertyObject(
                    titleResource,
                    CmsPropertyDefinition.PROPERTY_TITLE,
                    true);
                String rawName;
                if (titleProp.isNullProperty()) {
                    rawName = m_cms.getSitePath(titleResource);
                } else {
                    rawName = titleProp.getValue();
                }
                bean.setDefaultGroupName(Messages.get().getBundle(locale).key(Messages.GUI_PAGE_1, rawName));
            } catch (Exception e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
            return bean;
        }

        /**
         * @see org.opencms.ade.publish.I_CmsVirtualProject.I_Context#getResources()
         */
        public List<CmsResource> getResources() {

            String containerpageId = m_params.get(CmsPublishOptions.PARAM_CONTAINERPAGE);
            String elementId = m_params.get(CmsPublishOptions.PARAM_CONTENT);
            String detailId = m_params.get(CmsPublishOptions.PARAM_DETAIL);
            List<CmsUUID> startIds = new ArrayList<CmsUUID>();
            for (String id : new String[] {containerpageId, elementId, detailId}) {
                if (CmsUUID.isValidUUID(id)) {
                    startIds.add(new CmsUUID(id));
                }
            }
            return collectResources(startIds);
        }

        /**
         * @see org.opencms.ade.publish.I_CmsVirtualProject.I_Context#preSort(java.util.List)
         */
        public void preSort(List<CmsPublishResource> publishResources) {

            Collections.sort(publishResources, new Comparator<CmsPublishResource>() {

                public int compare(CmsPublishResource first, CmsPublishResource second) {

                    return ComparisonChain.start().compareTrueFirst(isNotPage(first), isNotPage(second)).compare(
                        second.getSortDate(),
                        first.getSortDate()).result();
                }

                private boolean isNotPage(CmsPublishResource res) {

                    String idStr = "" + res.getId();
                    String page = m_params.get(CmsPublishOptions.PARAM_CONTAINERPAGE);
                    return Objects.equal(idStr, page);
                }

            });
        }

        /**
         * This method collects all resources on which the current page or the current element depends.
         * More precisely, it will return all resources which are reachable from the resources whose structure ids are given in startids 
         * by a chain of relations of which at most the last one in the chain is weak.<p>
         * 
         * @param startIds the structure ids of the resources with which to start 
         * @return the collected resources
         */
        private List<CmsResource> collectResources(Collection<CmsUUID> startIds) {

            // collect all resources reachable by following a chain of relations from the start ids such that either all relations of that 
            // chain are strong, or the last relation is the only weak relation in the chain

            Set<CmsUUID> followedRelations = new HashSet<CmsUUID>();
            LinkedList<CmsUUID> toFollow = new LinkedList<CmsUUID>();
            Set<CmsResource> result = new HashSet<CmsResource>();
            toFollow.addAll(startIds);
            while (toFollow.size() > 0) {
                CmsUUID currentId = toFollow.removeFirst();
                LOG.info("Visiting: " + currentId);
                if (followedRelations.contains(currentId)) {
                    continue;
                }
                followedRelations.add(currentId);
                if (currentId.isNullUUID()) {
                    continue;
                }
                try {
                    CmsResource currentResource = m_cms.readResource(currentId, CmsResourceFilter.ALL);
                    LOG.info("adding resource with root path: " + currentResource.getRootPath());
                    result.add(currentResource);
                    List<CmsRelation> relations = m_cms.readRelations(CmsRelationFilter.relationsFromStructureId(currentId));

                    for (CmsRelation relation : relations) {
                        LOG.info("got relation to " + relation.getTargetPath() + " (" + relation.getTargetId() + ")");
                        if (relation.getType().isStrong()) {
                            LOG.info("(strong relation)");
                            toFollow.add(relation.getTargetId());
                        } else {
                            // original idea was to follow only strong relations, but not everyone may have used strong relations 
                            // for images, etc. in their schemas
                            LOG.info("(weak relation)");
                            try {
                                CmsResource weakResource = relation.getTarget(m_cms, CmsResourceFilter.ALL);
                                for (String typeName : new String[] {
                                    CmsResourceTypePlain.getStaticTypeName(),
                                    CmsResourceTypeImage.getStaticTypeName(),
                                    CmsResourceTypePointer.getStaticTypeName(),
                                    CmsResourceTypeBinary.getStaticTypeName()}) {
                                    if (OpenCms.getResourceManager().matchResourceType(
                                        typeName,
                                        weakResource.getTypeId())) {
                                        LOG.info("Of document type -> adding " + weakResource.getRootPath());
                                        result.add(weakResource);
                                    }
                                }
                            } catch (CmsException e) {
                                LOG.error(e.getLocalizedMessage(), e);
                            }
                        }
                    }
                    Set<CmsResource> changedParentFolders = new HashSet<CmsResource>();
                    for (CmsResource res : result) {
                        if (res.isFile()) {
                            try {
                                CmsResource parentFolder = m_cms.readParentFolder(res.getStructureId());
                                if (!parentFolder.getState().isUnchanged()) {
                                    changedParentFolders.add(parentFolder);
                                }
                            } catch (CmsPermissionViolationException e) {
                                LOG.error(e.getLocalizedMessage(), e);
                            }
                        }
                    }
                    result.addAll(changedParentFolders);
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
            return filterChanged(result);
        }

        /**
         * Creates a new list of resources which does not contain unchanged resources from an original list.<p>
         * 
         * @param resources the original resource list 
         * 
         * @return a new resource list which contains all resources from the original list which are not unchanged 
         */
        private List<CmsResource> filterChanged(Collection<CmsResource> resources) {

            List<CmsResource> result = new ArrayList<CmsResource>();
            for (CmsResource res : resources) {
                if (res.getState().isUnchanged()) {
                    continue;
                }
                result.add(res);
            }
            return result;
        }
    }

    /** The uuid of this virtual project. */
    public static final CmsUUID ID = CmsUUID.getConstantUUID("currentpage");

    /** A static instance of this class. */
    public static final CmsCurrentPageProject INSTANCE = new CmsCurrentPageProject();

    /** The logger for this class. */
    static final Log LOG = CmsLog.getLog(CmsCurrentPageProject.class);

    /**
     * @see org.opencms.ade.publish.I_CmsVirtualProject#createContext(org.opencms.file.CmsObject, java.util.Map)
     */
    public Context createContext(CmsObject cms, Map<String, String> params) {

        return new Context(cms, params);
    }

    /**
     * @see org.opencms.ade.publish.I_CmsVirtualProject#getProjectId()
     */
    public CmsUUID getProjectId() {

        return ID;
    }

}
