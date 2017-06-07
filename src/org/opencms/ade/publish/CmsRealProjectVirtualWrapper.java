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

package org.opencms.ade.publish;

import org.opencms.ade.publish.shared.CmsProjectBean;
import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.workflow.CmsDefaultWorkflowManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Wrapper to use real OpenCms projects through the I_CmsVirtualProject interface.<p>
 */
public class CmsRealProjectVirtualWrapper implements I_CmsVirtualProject {

    /** The project id. */
    private CmsUUID m_projectId;

    /**
     * Creates a new wrapper instance.<p>
     *
     * @param id the project id
     */
    public CmsRealProjectVirtualWrapper(CmsUUID id) {

        m_projectId = id;

    }

    /**
     * @see org.opencms.ade.publish.I_CmsVirtualProject#getProjectBean(org.opencms.file.CmsObject, java.util.Map)
     */
    public CmsProjectBean getProjectBean(CmsObject cms, Map<String, String> params) {

        try {
            CmsProject project = cms.readProject(getProjectId());
            CmsProjectBean result = CmsDefaultWorkflowManager.createProjectBeanFromProject(cms, project);
            Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
            String name = Messages.get().getBundle(locale).key(Messages.GUI_NORMAL_PROJECT_1, project.getName());
            result.setDefaultGroupName(name);
            return result;
        } catch (CmsException e) {
            return null;
        }
    }

    /**
     * @see org.opencms.ade.publish.I_CmsVirtualProject#getProjectId()
     */
    public CmsUUID getProjectId() {

        return m_projectId;
    }

    /**
     * @see org.opencms.ade.publish.I_CmsVirtualProject#getRelatedResourceProvider(org.opencms.file.CmsObject, org.opencms.ade.publish.shared.CmsPublishOptions)
     */
    public I_CmsPublishRelatedResourceProvider getRelatedResourceProvider(
        CmsObject cmsObject,
        CmsPublishOptions options) {

        return CmsDummyRelatedResourceProvider.INSTANCE;
    }

    /**
     * @see org.opencms.ade.publish.I_CmsVirtualProject#getResources(org.opencms.file.CmsObject, java.util.Map, java.lang.String)
     */
    public List<CmsResource> getResources(CmsObject cms, Map<String, String> params, String workflowId)
    throws CmsException {

        List<CmsResource> rawResourceList = new ArrayList<CmsResource>();
        CmsProject project = cms.getRequestContext().getCurrentProject();
        try {
            project = cms.readProject(getProjectId());
        } catch (CmsException e) {
            // ignore
        }
        // get the project publish list
        CmsProject originalProject = cms.getRequestContext().getCurrentProject();
        try {
            cms.getRequestContext().setCurrentProject(project);
            rawResourceList.addAll(OpenCms.getPublishManager().getPublishList(cms).getAllResources());
        } finally {
            cms.getRequestContext().setCurrentProject(originalProject);
        }
        return rawResourceList;
    }

    /**
     * @see org.opencms.ade.publish.I_CmsVirtualProject#isAutoSelectable()
     */
    public boolean isAutoSelectable() {

        return false;
    }

}
