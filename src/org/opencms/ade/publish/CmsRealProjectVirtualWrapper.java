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
import org.opencms.ade.publish.shared.CmsPublishResource;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsProject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ComparisonChain;

/**
 * Wrapper to use real OpenCms projects through the I_CmsVirtualProject interface.<p>
 */
public class CmsRealProjectVirtualWrapper implements I_CmsVirtualProject {

    /** 
     * The context for this project class.<p>
     */
    public class Context implements I_CmsVirtualProject.I_Context {

        /** The CMS context. */
        private CmsObject m_cms;

        /**
         * Creates a new context.<p>
         * 
         * @param cms the CMS context 
         */
        public Context(CmsObject cms) {

            m_cms = cms;
        }

        /**
         * @see org.opencms.ade.publish.I_CmsVirtualProject.I_Context#getProjectBean()
         */
        public CmsProjectBean getProjectBean() {

            try {
                CmsProject project = m_cms.readProject(getProjectId());
                CmsProjectBean result = CmsPublish.createProjectBeanFromProject(m_cms, project);
                result.setDefaultGroupName(project.getName());
                return result;
            } catch (CmsException e) {
                return null;
            }
        }

        /**
         * @see org.opencms.ade.publish.I_CmsVirtualProject.I_Context#getResources()
         */
        public List<CmsResource> getResources() throws CmsException {

            List<CmsResource> rawResourceList = new ArrayList<CmsResource>();

            CmsProject project = m_cms.getRequestContext().getCurrentProject();
            try {
                project = m_cms.readProject(getProjectId());
            } catch (CmsException e) {
                // ignore 
            }
            // get the project publish list
            CmsProject originalProject = m_cms.getRequestContext().getCurrentProject();
            try {
                m_cms.getRequestContext().setCurrentProject(project);
                rawResourceList.addAll(OpenCms.getPublishManager().getPublishList(m_cms).getAllResources());
            } finally {
                m_cms.getRequestContext().setCurrentProject(originalProject);
            }
            return rawResourceList;
        }

        /**
         * @see org.opencms.ade.publish.I_CmsVirtualProject.I_Context#preSort(java.util.List)
         */
        public void preSort(List<CmsPublishResource> publishResources) {

            Collections.sort(publishResources, new Comparator<CmsPublishResource>() {

                public int compare(CmsPublishResource o1, CmsPublishResource o2) {

                    if (o1 == o2) {
                        return 0;
                    }
                    return ComparisonChain.start().compare(o2.getDateLastModified(), o1.getDateLastModified()).result();
                }
            });
        }
    }

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
     * @see org.opencms.ade.publish.I_CmsVirtualProject#createContext(org.opencms.file.CmsObject, java.util.Map)
     */
    public I_Context createContext(CmsObject cms, Map<String, String> params) {

        return new Context(cms);
    }

    /**
     * @see org.opencms.ade.publish.I_CmsVirtualProject#getProjectId()
     */
    public CmsUUID getProjectId() {

        return m_projectId;
    }

}
