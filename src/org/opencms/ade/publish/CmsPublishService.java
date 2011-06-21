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
import org.opencms.ade.publish.shared.CmsPublishData;
import org.opencms.ade.publish.shared.CmsPublishGroup;
import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.ade.publish.shared.CmsPublishResource;
import org.opencms.ade.publish.shared.rpc.I_CmsPublishService;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.CmsRpcException;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.security.CmsRole;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

/**
 * The implementation of the publish service.<p>
 * 
 * @since 8.0.0
 * 
 */
public class CmsPublishService extends CmsGwtService implements I_CmsPublishService {

    /** The version id for serialization. */
    private static final long serialVersionUID = 3852074177607037076L;

    /** Session attribute name constant. */
    private static final String SESSION_ATTR_ADE_PUB_OPTS_CACHE = "__OCMS_ADE_PUB_OPTS_CACHE__";

    /**
     * @see org.opencms.ade.publish.shared.rpc.I_CmsPublishService#getInitData()
     */
    public CmsPublishData getInitData() throws CmsRpcException {

        CmsPublishData result = null;
        try {
            CmsPublishOptions options = getCachedOptions();
            result = new CmsPublishData(
                options,
                getProjects(),
                getPublishGroups(options),
                canPublishBrokenRelations(getCmsObject()));
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.publish.shared.rpc.I_CmsPublishService#getProjects()
     */
    public List<CmsProjectBean> getProjects() throws CmsRpcException {

        List<CmsProjectBean> result = null;
        try {
            result = new CmsPublish(getCmsObject()).getManageableProjects();
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * @see org.opencms.ade.publish.shared.rpc.I_CmsPublishService#getPublishGroups(org.opencms.ade.publish.shared.CmsPublishOptions)
     */
    public List<CmsPublishGroup> getPublishGroups(CmsPublishOptions options) throws CmsRpcException {

        List<CmsPublishGroup> results = null;
        try {
            CmsPublish pub = new CmsPublish(getCmsObject(), options);
            setCachedOptions(options);
            results = pub.getPublishGroups();
        } catch (Throwable e) {
            error(e);
        }
        return results;
    }

    /**
     * @see org.opencms.ade.publish.shared.rpc.I_CmsPublishService#getPublishOptions()
     */
    public CmsPublishOptions getPublishOptions() throws CmsRpcException {

        CmsPublishOptions result = null;
        try {
            result = getCachedOptions();
        } catch (Throwable e) {
            error(e);
        }
        return result;
    }

    /**
     * 
     * @see org.opencms.ade.publish.shared.rpc.I_CmsPublishService#publishResources(java.util.List, java.util.List, boolean)
     */
    public List<CmsPublishResource> publishResources(List<CmsUUID> toPublish, List<CmsUUID> toRemove, boolean force)
    throws CmsRpcException {

        List<CmsPublishResource> brokenLinkBeans = null;
        try {
            CmsObject cms = getCmsObject();
            CmsPublish pub = new CmsPublish(cms, getCachedOptions());
            List<CmsResource> publishResources = idsToResources(cms, toPublish);
            brokenLinkBeans = force ? new ArrayList<CmsPublishResource>() : pub.getBrokenResources(publishResources);
            if (brokenLinkBeans.size() == 0) {
                pub.publishResources(publishResources);
                pub.removeResourcesFromPublishList(toRemove);
            }
        } catch (Throwable e) {
            error(e);
        }
        return brokenLinkBeans;
    }

    /**
     * Checks whether the current user can publish resources even if it would break relations.<p>
     * 
     * @param cms the CmsObject for which the user should be checked
     * 
     * @return true if the user can publish resources even if it breaks relations 
     */
    private boolean canPublishBrokenRelations(CmsObject cms) {

        return OpenCms.getWorkplaceManager().getDefaultUserSettings().isAllowBrokenRelations()
            || OpenCms.getRoleManager().hasRole(cms, CmsRole.VFS_MANAGER);
    }

    /**
     * Returns the cached publish options, creating it if it doesn't already exist.<p>
     * 
     * @return the cached publish options
     */
    private CmsPublishOptions getCachedOptions() {

        CmsPublishOptions cache = (CmsPublishOptions)getRequest().getSession().getAttribute(
            SESSION_ATTR_ADE_PUB_OPTS_CACHE);
        if (cache == null) {
            cache = new CmsPublishOptions();
            getRequest().getSession().setAttribute(SESSION_ATTR_ADE_PUB_OPTS_CACHE, cache);
        }
        return cache;

    }

    /**
     * Converts a list of IDs to resources.<p>
     * 
     * @param cms the CmObject used for reading the resources 
     * @param ids the list of IDs
     * 
     * @return a list of resources 
     */
    private List<CmsResource> idsToResources(CmsObject cms, List<CmsUUID> ids) {

        List<CmsResource> result = new ArrayList<CmsResource>();
        for (CmsUUID id : ids) {
            try {
                CmsResource resource = cms.readResource(id, CmsResourceFilter.ALL);
                result.add(resource);
            } catch (CmsException e) {
                // should never happen
                logError(e);
            }
        }
        return result;
    }

    /**
     * Saves the given options to the session.<p>
     * 
     * @param options the options to save
     */
    private void setCachedOptions(CmsPublishOptions options) {

        getRequest().getSession().setAttribute(SESSION_ATTR_ADE_PUB_OPTS_CACHE, options);
    }
}
