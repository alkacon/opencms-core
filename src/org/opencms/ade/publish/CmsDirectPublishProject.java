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
import org.opencms.file.CmsResourceFilter;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.common.collect.Sets;

/**
 *  Virtual project for 'direct publishing' of resources.<p>
 *
 *  This virtual project gets the names of the resources to publish from the publish parameter map.
 *  If the 'add contents' mode is enabled (which is also determined from the publish parameters), the
 *  contents of folders are added to the list of publish resources. This virtual project is only available
 *  if any file names are passed via the
 */
public class CmsDirectPublishProject implements I_CmsVirtualProject {

    /** The ID of this virtual project. */
    public static final CmsUUID ID = CmsUUID.getConstantUUID("" + CmsDirectPublishProject.class);

    /** The logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsDirectPublishProject.class);

    /**
     * @see org.opencms.ade.publish.I_CmsVirtualProject#getProjectBean(org.opencms.file.CmsObject, java.util.Map)
     */
    public CmsProjectBean getProjectBean(CmsObject cms, Map<String, String> params) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(params.get(CmsPublishOptions.PARAM_FILES))) {
            return null;
        }

        Locale locale = OpenCms.getWorkplaceManager().getWorkplaceLocale(cms);
        String name = Messages.get().getBundle(locale).key(Messages.GUI_PROJECT_DIRECT_PUBLISH_0);
        CmsProjectBean bean = new CmsProjectBean(ID, 0, name, name);

        bean.setRank(150);
        bean.setDefaultGroupName(name);
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
    public List<CmsResource> getResources(CmsObject cms, Map<String, String> params, String workflowId)
    throws CmsException {

        Set<String> paths = getPaths(params);
        boolean includeContents = shouldIncludeContents(params);
        Set<CmsResource> result = Sets.newHashSet();
        for (String path : paths) {
            try {
                result.add(cms.readResource(path, CmsResourceFilter.ALL));
            } catch (CmsException e) {
                LOG.error(e.getLocalizedMessage(), e);
            }
        }
        if (includeContents) {
            addSubResources(cms, result);
        }
        List<CmsResource> resultList = new ArrayList<CmsResource>();
        for (CmsResource res : result) {
            if (!res.getState().isUnchanged()) {
                resultList.add(res);
            }
        }
        return resultList;
    }

    /**
     * @see org.opencms.ade.publish.I_CmsVirtualProject#isAutoSelectable()
     */
    public boolean isAutoSelectable() {

        return true;
    }

    /**
     * Adds contents of folders to a list of resources.<p>
     *
     * @param cms the CMS context to use
     * @param resources the resource list to which to add the folder contents
     * @throws CmsException if something goes wrong
     */
    protected void addSubResources(CmsObject cms, Set<CmsResource> resources) throws CmsException {

        List<CmsResource> subResources = new ArrayList<CmsResource>();
        CmsObject rootCms = OpenCms.initCmsObject(cms);
        rootCms.getRequestContext().setSiteRoot("");
        for (CmsResource res : resources) {
            if (res.isFolder()) {
                try {
                    List<CmsResource> childrenOfCurrentResource = rootCms.readResources(
                        res.getRootPath(),
                        CmsResourceFilter.ALL,
                        true);
                    subResources.addAll(childrenOfCurrentResource);
                } catch (CmsException e) {
                    LOG.error(e.getLocalizedMessage(), e);
                }
            }
        }
        resources.addAll(subResources);
    }

    /**
     * Returns true if the folder contents should be included.<p>
     *
     * @param params the publish parameters
     * @return true if the folder contents should be included
     */
    protected boolean shouldIncludeContents(Map<String, String> params) {

        String includeContentsStr = params.get(CmsPublishOptions.PARAM_INCLUDE_CONTENTS);
        boolean includeContents = false;
        try {
            includeContents = Boolean.parseBoolean(includeContentsStr);
        } catch (Exception e) {
            // ignore; includeContents remains the default value
        }
        return includeContents;
    }

    /**
     * Gets the set of site paths from the publish parameters.<p>
     *
     * @param params the publish parameters
     *
     * @return the set of site paths
     */
    private Set<String> getPaths(Map<String, String> params) {

        Set<String> result = Sets.newHashSet();
        String paths = params.get(CmsPublishOptions.PARAM_FILES);
        if (paths != null) {
            result.addAll(CmsStringUtil.splitAsList(paths, "|"));
        }
        return result;
    }

}
