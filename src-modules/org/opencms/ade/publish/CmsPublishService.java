/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/publish/Attic/CmsPublishService.java,v $
 * Date   : $Date: 2010/04/12 10:24:47 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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
import org.opencms.ade.publish.shared.CmsPublishGroup;
import org.opencms.ade.publish.shared.CmsPublishOptions;
import org.opencms.ade.publish.shared.CmsPublishOptionsAndProjects;
import org.opencms.ade.publish.shared.CmsPublishResource;
import org.opencms.ade.publish.shared.rpc.I_CmsPublishService;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.shared.rpc.CmsRpcException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsUUID;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;

/**
 * The implementation of the publish service.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.4 $
 * 
 * @since 8.0.0
 * 
 */
public class CmsPublishService extends CmsGwtService implements I_CmsPublishService {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPublishService.class);

    /** The version id for serialization. */
    private static final long serialVersionUID = 1L;

    /** Session attribute name constant. */
    private static final String SESSION_ATTR_ADE_PUB_OPTS_CACHE = "__OCMS_ADE_PUB_OPTS_CACHE__";

    /**
     * @see org.opencms.ade.publish.shared.rpc.I_CmsPublishService#getProjects()
     */
    public List<CmsProjectBean> getProjects() throws CmsRpcException {

        try {
            return new CmsPublish(getCmsObject()).getManageableProjects();
        } catch (Throwable e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new CmsRpcException(CmsException.getStackTraceAsString(e));
        }
    }

    /**
     * @see org.opencms.ade.publish.shared.rpc.I_CmsPublishService#getPublishGroups(org.opencms.ade.publish.shared.CmsPublishOptions)
     */
    public List<CmsPublishGroup> getPublishGroups(CmsPublishOptions options) throws CmsRpcException {

        try {
            CmsPublish pub = new CmsPublish(getCmsObject(), options);
            setCachedOptions(options);
            return pub.getPublishGroups();
        } catch (Throwable e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new CmsRpcException(CmsException.getStackTraceAsString(e));
        }
    }

    /**
     * @see org.opencms.ade.publish.shared.rpc.I_CmsPublishService#getPublishOptions()
     */
    public CmsPublishOptions getPublishOptions() throws CmsRpcException {

        try {
            return getCachedOptions();
        } catch (Throwable e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new CmsRpcException(CmsException.getStackTraceAsString(e));
        }
    }

    /**
     * @see org.opencms.ade.publish.shared.rpc.I_CmsPublishService#getPublishOptionsAndProjects()
     */
    public CmsPublishOptionsAndProjects getPublishOptionsAndProjects() throws CmsRpcException {

        try {
            return new CmsPublishOptionsAndProjects(getCachedOptions(), getProjects());
        } catch (Throwable e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new CmsRpcException(CmsException.getStackTraceAsString(e));
        }
    }

    /**
     * 
     * @see org.opencms.ade.publish.shared.rpc.I_CmsPublishService#publishResources(java.util.List, java.util.List, boolean)
     */
    public List<CmsPublishResource> publishResources(List<CmsUUID> toPublish, List<CmsUUID> toRemove, boolean force)
    throws CmsRpcException {

        try {
            CmsObject cms = getCmsObject();
            // TODO: i think we need the publish options here!
            CmsPublish pub = new CmsPublish(cms);
            List<CmsResource> publishResources = idsToResources(cms, toPublish);
            List<CmsPublishResource> brokenLinkBeans = pub.getBrokenResources(publishResources);
            if (brokenLinkBeans.size() == 0) {
                pub.publishResources(publishResources);
                pub.removeResourcesFromPublishList(toRemove);
            }
            return brokenLinkBeans;
        } catch (Throwable e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new CmsRpcException(CmsException.getStackTraceAsString(e));
        }
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
                LOG.error(e.getLocalizedMessage(), e);
                // TODO: do something with these resources. show them to the user?
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
