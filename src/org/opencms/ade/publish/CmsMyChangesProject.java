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
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.google.common.collect.Lists;

/**
 * Virtual project for the "My changes" mode in the publish dialog.<p>
 */
public class CmsMyChangesProject implements I_CmsVirtualProject {

    /** The project id. */
    public static final CmsUUID ID = CmsUUID.getNullUUID();

    /** The logger for this class. */
    private static final Log LOG = CmsLog.getLog(CmsMyChangesProject.class);

    /**
     * @see org.opencms.ade.publish.I_CmsVirtualProject#getProjectBean(org.opencms.file.CmsObject, java.util.Map)
     */
    public CmsProjectBean getProjectBean(CmsObject cms, Map<String, String> params) {

        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        String title = Messages.get().getBundle(locale).key(Messages.GUI_MYCHANGES_PROJECT_0);
        CmsProjectBean bean = new CmsProjectBean(ID, 0, title, title);
        bean.setRank(200);
        bean.setDefaultGroupName("");
        return bean;
    }

    /**
     * @see org.opencms.ade.publish.I_CmsVirtualProject#getProjectId()
     */
    public CmsUUID getProjectId() {

        return ID;
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
    public List<CmsResource> getResources(CmsObject cms, Map<String, String> params, String workflowId) {

        try {
            return Lists.newArrayList(OpenCms.getPublishManager().getUsersPubList(cms));
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            return Lists.newArrayList();
        }
    }

    /**
     * @see org.opencms.ade.publish.I_CmsVirtualProject#isAutoSelectable()
     */
    public boolean isAutoSelectable() {

        return true;
    }

}
