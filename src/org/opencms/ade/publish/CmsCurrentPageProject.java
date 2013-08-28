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
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.loader.CmsResourceManager;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
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
            String title = Messages.get().getBundle(OpenCms.getWorkplaceManager().getWorkplaceLocale(m_cms)).key(
                Messages.GUI_CURRENTPAGE_PROJECT_0);
            if ((pageId != null) || (elementId != null)) {
                CmsProjectBean bean = new CmsProjectBean(ID, 0, title, title);
                bean.setRank(100);
                bean.setDefaultGroupName("");
                if (pageId != null) {
                    try {
                        CmsResource page = m_cms.readResource(new CmsUUID(pageId), CmsResourceFilter.IGNORE_EXPIRATION);
                        CmsProperty titleProp = m_cms.readPropertyObject(
                            page,
                            CmsPropertyDefinition.PROPERTY_TITLE,
                            true);
                        if (titleProp.isNullProperty()) {
                            bean.setDefaultGroupName(m_cms.getSitePath(page));
                        } else {
                            bean.setDefaultGroupName(titleProp.getValue());
                        }
                    } catch (Exception e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
                return bean;
            } else {
                return null;
            }
        }

        /**
         * @see org.opencms.ade.publish.I_CmsVirtualProject.I_Context#getResources()
         */
        public List<CmsResource> getResources() throws CmsException {

            String containerpageId = m_params.get(CmsPublishOptions.PARAM_CONTAINERPAGE);
            String elementId = m_params.get(CmsPublishOptions.PARAM_CONTENT);
            List<CmsResource> result = new ArrayList<CmsResource>();
            if (containerpageId != null) {
                return filterChanged(getPageElementResources(containerpageId, elementId));
            } else if (elementId != null) {
                return filterChanged(getElementResources(elementId));
            }
            return result;
        }

        /**
         * @see org.opencms.ade.publish.I_CmsVirtualProject.I_Context#preSort(java.util.List)
         */
        public void preSort(List<CmsPublishResource> publishResources) {

            Collections.sort(publishResources, new Comparator<CmsPublishResource>() {

                public int compare(CmsPublishResource first, CmsPublishResource second) {

                    return ComparisonChain.start().compareTrueFirst(isNotPage(first), isNotPage(second)).compare(
                        second.getDateLastModified(),
                        first.getDateLastModified()).result();
                }

                private boolean isNotPage(CmsPublishResource res) {

                    String idStr = "" + res.getId();
                    String page = m_params.get(CmsPublishOptions.PARAM_CONTAINERPAGE);
                    return Objects.equal(idStr, page);
                }

            });
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

        /**
         * Gets the resources for the non-containerpage case.<p>
         *  
         * @param elementId the element structure id 
         * @return the list of publish resources for the edited element 
         * 
         * @throws CmsException if something goes wrong 
         */
        private Set<CmsResource> getElementResources(String elementId) throws CmsException {

            CmsUUID pageId = new CmsUUID(elementId);
            Set<CmsResource> result = new HashSet<CmsResource>();
            CmsResource content = m_cms.readResource(pageId, CmsResourceFilter.IGNORE_EXPIRATION);
            result.add(content);
            List<CmsRelation> rels = m_cms.readRelations(CmsRelationFilter.relationsFromStructureId(pageId));
            for (CmsRelation rel : rels) {
                CmsResource target = rel.getTarget(m_cms, CmsResourceFilter.IGNORE_EXPIRATION);
                if (CmsResourceTypeXmlContainerPage.isContainerPage(target)) {
                    continue;
                }
                result.add(target);
            }
            return result;

        }

        /**
         * Gets the resources for the containerpage case.<p>
         * 
         * @param containerpageId the container page structure id 
         * @param elementId the content element's structure id 
         * @return the list of publish resources for the edited container page 
         * 
         * @throws CmsException if soemthing goes wrong 
         */
        private Set<CmsResource> getPageElementResources(String containerpageId, String elementId) throws CmsException {

            CmsUUID pageId = new CmsUUID(containerpageId);
            CmsResourceManager resMan = OpenCms.getResourceManager();
            boolean foundElement = false;
            Set<CmsResource> result = new HashSet<CmsResource>();
            CmsResource page = m_cms.readResource(pageId, CmsResourceFilter.IGNORE_EXPIRATION);
            result.add(page);
            List<CmsRelation> rels = m_cms.readRelations(CmsRelationFilter.relationsFromStructureId(pageId));
            for (CmsRelation rel : rels) {
                CmsResource target = rel.getTarget(m_cms, CmsResourceFilter.IGNORE_EXPIRATION);
                // do not include element/inheritance groups because they may affect multiple pages 
                if (resMan.matchResourceType(
                    CmsResourceTypeXmlContainerPage.GROUP_CONTAINER_TYPE_NAME,
                    target.getTypeId())
                    || resMan.matchResourceType(
                        CmsResourceTypeXmlContainerPage.INHERIT_CONTAINER_TYPE_NAME,
                        target.getTypeId())) {
                    continue;
                }
                foundElement |= (("" + target.getStructureId()).equals(elementId));
                result.add(target);
                List<CmsRelation> rels2 = m_cms.readRelations(CmsRelationFilter.relationsFromStructureId(target.getStructureId()));
                for (CmsRelation rel2 : rels2) {
                    CmsResource target2 = rel2.getTarget(m_cms, CmsResourceFilter.IGNORE_EXPIRATION);
                    if (CmsResourceTypeXmlContainerPage.isContainerPage(target)) {
                        continue;
                    }
                    result.add(target2);
                }
            }
            if ((elementId != null) && !foundElement) {
                // no element with the given id was found while going through the relations of the container page, so it's probaby a resource from a collector: add it and its related resources separately 
                result.addAll(getElementResources(elementId));
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
