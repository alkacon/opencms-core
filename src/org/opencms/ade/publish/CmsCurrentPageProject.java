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
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

/**
 * Virtual project which includes the currently edited resource and all its related resources.
 */
public class CmsCurrentPageProject implements I_CmsVirtualProject {

    /** The uuid of this virtual project. */
    public static final CmsUUID ID = CmsUUID.getConstantUUID("currentpage");

    /** The logger for this class. */
    static final Log LOG = CmsLog.getLog(CmsCurrentPageProject.class);

    /**
     * @see org.opencms.ade.publish.I_CmsVirtualProject#getProjectBean(org.opencms.file.CmsObject, java.util.Map)
     */
    public CmsProjectBean getProjectBean(CmsObject cms, Map<String, String> params) {

        String pageId = params.get(CmsPublishOptions.PARAM_CONTAINERPAGE);
        String elementId = params.get(CmsPublishOptions.PARAM_CONTENT);
        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
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
            CmsResource titleResource = cms.readResource(structureIdForTitle, CmsResourceFilter.IGNORE_EXPIRATION);
            CmsProperty titleProp = cms.readPropertyObject(titleResource, CmsPropertyDefinition.PROPERTY_TITLE, true);
            String rawName;
            if (titleProp.isNullProperty()) {
                rawName = cms.getSitePath(titleResource);
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
     * @see org.opencms.ade.publish.I_CmsVirtualProject#getProjectId()
     */
    public CmsUUID getProjectId() {

        return ID;
    }

    /**
     * @see org.opencms.ade.publish.I_CmsVirtualProject#getResources(org.opencms.file.CmsObject, java.util.Map, java.lang.String)
     */
    public List<CmsResource> getResources(CmsObject cms, Map<String, String> params, String workflowId) {

        String containerpageId = params.get(CmsPublishOptions.PARAM_CONTAINERPAGE);
        String elementId = params.get(CmsPublishOptions.PARAM_CONTENT);
        String detailId = params.get(CmsPublishOptions.PARAM_DETAIL);
        Set<CmsResource> resources = new HashSet<CmsResource>();
        for (String id : new String[] {containerpageId, elementId, detailId}) {
            if (CmsUUID.isValidUUID(id)) {
                try {
                    CmsResource resource = cms.readResource(new CmsUUID(id), CmsResourceFilter.ALL);
                    resources.add(resource);
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }

            }
        }
        return Lists.newArrayList(resources);
    }

    /**
     * @see org.opencms.ade.publish.I_CmsVirtualProject#isAutoSelectable()
     */
    public boolean isAutoSelectable() {

        return true;
    }

}
