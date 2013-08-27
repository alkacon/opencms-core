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
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.CmsResourceTypeXmlContainerPage;
import org.opencms.loader.CmsResourceManager;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.relations.CmsRelation;
import org.opencms.relations.CmsRelationFilter;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Virtual project which includes the currently edited resource and all its related resources.
 */
public class CmsCurrentPageProject implements I_CmsVirtualProject {

    /** The uuid of this virtual project. */
    public static final CmsUUID ID = CmsUUID.getConstantUUID("currentpage");

    /** A static instance of this class. */
    public static final CmsCurrentPageProject INSTANCE = new CmsCurrentPageProject();

    /**
     * @see org.opencms.ade.publish.I_CmsVirtualProject#getProjectBean(org.opencms.file.CmsObject, java.util.Map)
     */
    public CmsProjectBean getProjectBean(CmsObject cms, Map<String, String> params) {

        String pageId = params.get(CmsPublishOptions.PARAM_CONTAINERPAGE);
        String elementId = params.get(CmsPublishOptions.PARAM_CONTENT);
        String title = Messages.get().getBundle(OpenCms.getWorkplaceManager().getWorkplaceLocale(cms)).key(
            Messages.GUI_CURRENTPAGE_PROJECT_0);
        if ((pageId != null) || (elementId != null)) {
            CmsProjectBean bean = new CmsProjectBean(ID, 0, title, title);
            bean.setRank(100);
            return bean;

        } else {
            return null;
        }
    }

    /**
     * @see org.opencms.ade.publish.I_CmsVirtualProject#getProjectId()
     */
    public CmsUUID getProjectId() {

        return ID;
    }

    /**
     * @see org.opencms.ade.publish.I_CmsVirtualProject#getResources(org.opencms.file.CmsObject, java.util.Map)
     */
    public List<CmsResource> getResources(CmsObject cms, Map<String, String> params) throws CmsException {

        String containerpageId = params.get(CmsPublishOptions.PARAM_CONTAINERPAGE);
        String elementId = params.get(CmsPublishOptions.PARAM_CONTENT);
        List<CmsResource> result = new ArrayList<CmsResource>();
        if (containerpageId != null) {
            return filterChanged(getPageElementResources(cms, containerpageId, elementId));
        } else if (elementId != null) {
            return filterChanged(getElementResources(cms, elementId));
        }
        return result;
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
     * @param cms the CMS context 
     * @param elementId the element structure id 
     * @return the list of publish resources for the edited element 
     * 
     * @throws CmsException if something goes wrong 
     */
    private Set<CmsResource> getElementResources(CmsObject cms, String elementId) throws CmsException {

        CmsUUID pageId = new CmsUUID(elementId);
        Set<CmsResource> result = new HashSet<CmsResource>();
        CmsResource content = cms.readResource(pageId, CmsResourceFilter.IGNORE_EXPIRATION);
        result.add(content);
        List<CmsRelation> rels = cms.readRelations(CmsRelationFilter.relationsFromStructureId(pageId));
        for (CmsRelation rel : rels) {
            CmsResource target = rel.getTarget(cms, CmsResourceFilter.IGNORE_EXPIRATION);
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
     * @param cms the CMS context 
     * @param containerpageId the container page structure id 
     * @param elementId the content element's structure id 
     * @return the list of publish resources for the edited container page 
     * 
     * @throws CmsException if soemthing goes wrong 
     */
    private Set<CmsResource> getPageElementResources(CmsObject cms, String containerpageId, String elementId)
    throws CmsException {

        CmsUUID pageId = new CmsUUID(containerpageId);
        CmsResourceManager resMan = OpenCms.getResourceManager();
        boolean foundElement = false;
        Set<CmsResource> result = new HashSet<CmsResource>();
        CmsResource page = cms.readResource(pageId, CmsResourceFilter.IGNORE_EXPIRATION);
        result.add(page);
        List<CmsRelation> rels = cms.readRelations(CmsRelationFilter.relationsFromStructureId(pageId));
        for (CmsRelation rel : rels) {
            CmsResource target = rel.getTarget(cms, CmsResourceFilter.IGNORE_EXPIRATION);
            // do not include element/inheritance groups because they may affect multiple pages 
            if (resMan.matchResourceType(CmsResourceTypeXmlContainerPage.GROUP_CONTAINER_TYPE_NAME, target.getTypeId())
                || resMan.matchResourceType(
                    CmsResourceTypeXmlContainerPage.INHERIT_CONTAINER_TYPE_NAME,
                    target.getTypeId())) {
                continue;
            }
            foundElement |= (("" + target.getStructureId()).equals(elementId));
            result.add(target);
            List<CmsRelation> rels2 = cms.readRelations(CmsRelationFilter.relationsFromStructureId(target.getStructureId()));
            for (CmsRelation rel2 : rels2) {
                CmsResource target2 = rel2.getTarget(cms, CmsResourceFilter.IGNORE_EXPIRATION);
                if (CmsResourceTypeXmlContainerPage.isContainerPage(target)) {
                    continue;
                }
                result.add(target2);
            }
        }
        if ((elementId != null) && !foundElement) {
            // no element with the given id was found while going through the relations of the container page, so it's probaby a resource from a collector: add it and its related resources separately 
            result.addAll(getElementResources(cms, elementId));
        }
        return result;
    }

}
