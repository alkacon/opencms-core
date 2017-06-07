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

package org.opencms.workflow;

import org.opencms.ade.publish.CmsRealProjectVirtualWrapper;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.util.CmsUUID;

import java.util.List;
import java.util.Map;

/**
 * Project wrapper which specially handles the 'release' workflow by reading resources of a project regardless of publish permissions.
 */
public class CmsExtendedRealProjectWrapper extends CmsRealProjectVirtualWrapper {

    /**
     * Creates a new wrapper instance.<p>
     *
     * @param id the project id
     */
    public CmsExtendedRealProjectWrapper(CmsUUID id) {

        super(id);
    }

    /**
     * @see org.opencms.ade.publish.CmsRealProjectVirtualWrapper#getResources(org.opencms.file.CmsObject, java.util.Map, java.lang.String)
     */
    @Override
    public List<CmsResource> getResources(CmsObject cms, Map<String, String> params, String workflowId)
    throws CmsException {

        if (CmsExtendedWorkflowManager.WORKFLOW_RELEASE.equals(workflowId)) {
            // use readProjectView because it doesn't check for publish permissions on the resources
            return cms.readProjectView(getProjectId(), CmsResource.STATE_KEEP);
        } else {
            return super.getResources(cms, params, workflowId);
        }
    }

}
