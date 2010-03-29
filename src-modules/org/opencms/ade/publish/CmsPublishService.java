/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/publish/Attic/CmsPublishService.java,v $
 * Date   : $Date: 2010/03/29 08:47:34 $
 * Version: $Revision: 1.1 $
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

import org.opencms.ade.publish.shared.CmsClientPublishOptions;
import org.opencms.ade.publish.shared.CmsClientPublishResourceBean;
import org.opencms.ade.publish.shared.CmsPublishGroups;
import org.opencms.ade.publish.shared.CmsPublishOptionsAndProjects;
import org.opencms.ade.publish.shared.CmsPublishStatus;
import org.opencms.ade.publish.shared.rpc.I_CmsPublishService;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.gwt.CmsGwtService;
import org.opencms.gwt.shared.rpc.CmsRpcException;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.editors.ade.CmsADEPublish;
import org.opencms.workplace.editors.ade.CmsADESessionCache;
import org.opencms.workplace.editors.ade.CmsProjectBean;
import org.opencms.workplace.editors.ade.CmsPublishGroupBean;
import org.opencms.workplace.editors.ade.CmsPublishOptions;
import org.opencms.workplace.editors.ade.CmsPublishResourceBean;
import org.opencms.workplace.editors.ade.CmsPublishResourceInfoBean;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * The implementation of the publish service.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 * 
 */
public class CmsPublishService extends CmsGwtService implements I_CmsPublishService {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsPublishService.class);

    /** The version id for serialization. */
    private static final long serialVersionUID = 1L;

    /**
     * 
     * @see org.opencms.ade.publish.shared.rpc.I_CmsPublishService#getProjects()
     */
    public Map<String, String> getProjects() {

        CmsObject cms = getCmsObject();
        CmsADEPublish pub = new CmsADEPublish(cms);
        List<CmsProjectBean> projects = pub.getManageableProjects();
        Map<String, String> result = new LinkedHashMap<String, String>();
        for (CmsProjectBean project : projects) {
            result.put(project.getId().toString(), project.getName());
        }
        return result;
    }

    /**
     * 
     * @see org.opencms.ade.publish.shared.rpc.I_CmsPublishService#getPublishGroups(org.opencms.ade.publish.shared.CmsClientPublishOptions)
     */
    public CmsPublishGroups getPublishGroups(CmsClientPublishOptions options) {

        CmsPublishGroups result = new CmsPublishGroups();
        CmsObject cms = this.getCmsObject();
        CmsADEPublish pub = new CmsADEPublish(cms);
        CmsPublishOptions serverOptions = pub.getOptions();
        serverOptions.setIncludeRelated(options.isIncludeRelated());
        serverOptions.setIncludeSiblings(options.isIncludeSiblings());
        String project = options.getProject();
        CmsUUID projectId = null;
        if ((project != null) && !project.equals("")) {
            projectId = new CmsUUID(project);
        }
        serverOptions.setProjectId(projectId);
        getSessionCache().setCachePublishOptions(serverOptions);
        List<CmsPublishGroupBean> groups = pub.getPublishGroups();
        for (CmsPublishGroupBean group : groups) {
            String groupName = group.getName();
            List<CmsPublishResourceBean> resourceBeans = group.getResources();
            List<CmsClientPublishResourceBean> clientBeans = toClientResourceBeans(resourceBeans);
            result.addGroup(groupName, clientBeans);

        }
        return result;
    }

    /**
     * 
     * @see org.opencms.ade.publish.shared.rpc.I_CmsPublishService#getPublishOptions()
     */
    public CmsClientPublishOptions getPublishOptions() {

        CmsADESessionCache cache = getSessionCache();
        return toClientPublishOptions(cache.getPublishOptions());
    }

    /**
     * @see org.opencms.ade.publish.shared.rpc.I_CmsPublishService#getPublishOptionsAndProjects()
     */
    public CmsPublishOptionsAndProjects getPublishOptionsAndProjects() {

        return new CmsPublishOptionsAndProjects(getPublishOptions(), getProjects());
    }

    /**
     * 
     * @see org.opencms.ade.publish.shared.rpc.I_CmsPublishService#publishResources(java.util.List, java.util.List, boolean)
     */
    public CmsPublishStatus publishResources(List<String> toPublish, List<String> toRemove, boolean force)
    throws CmsRpcException {

        CmsPublishStatus result = new CmsPublishStatus();
        try {
            CmsObject cms = this.getCmsObject();
            CmsADEPublish pub = new CmsADEPublish(cms);
            List<CmsResource> publishResources = uuidStringsToResources(cms, toPublish);
            List<CmsPublishResourceBean> brokenLinkBeans = pub.getBrokenResources(publishResources);
            if (brokenLinkBeans.size() == 0) {
                pub.publishResources(publishResources);
                pub.removeResourcesFromPublishList(uuidStringsToUuids(toRemove));
            } else {
                result.setProblemResources(toClientResourceBeans(brokenLinkBeans));
            }
            return result;
        } catch (CmsException e) {
            LOG.error(e.getLocalizedMessage(), e);
            throw new CmsRpcException(CmsException.getStackTraceAsString(e));
        }
    }

    /**
     * Helper method for returning the session cache, creating it if it doesn't already exist.<p>
     * 
     * @return the session cache
     */
    private CmsADESessionCache getSessionCache() {

        CmsADESessionCache cache = (CmsADESessionCache)this.getRequest().getSession().getAttribute(
            CmsADESessionCache.SESSION_ATTR_ADE_CACHE);
        if (cache == null) {
            cache = new CmsADESessionCache(getCmsObject());
            getRequest().getSession().setAttribute(CmsADESessionCache.SESSION_ATTR_ADE_CACHE, cache);
        }
        return cache;

    }

    /**
     * Helper method for converting server side to client side publish options beans.<p>
     * 
     * @param publishOptions the server side publish option bean
     * @return a client-side publish options bean
     */
    private CmsClientPublishOptions toClientPublishOptions(
        org.opencms.workplace.editors.ade.CmsPublishOptions publishOptions) {

        CmsClientPublishOptions result = new CmsClientPublishOptions();
        CmsUUID projectId = publishOptions.getProjectId();
        result.setProject(projectId != null ? projectId.toString() : "");
        result.setIncludeRelated(publishOptions.isIncludeRelated());
        result.setIncludeSiblings(publishOptions.isIncludeSiblings());
        return result;
    }

    /**
     * Helper method for converting server side to client side resource beans.<p>
     * 
     * @param bean a server-side resource bean
     * 
     * @return a client-side resource bean
     */
    private CmsClientPublishResourceBean toClientResourceBean(CmsPublishResourceBean bean) {

        CmsClientPublishResourceBean result = new CmsClientPublishResourceBean();
        result.setTitle(bean.getTitle());
        result.setName(bean.getName());
        result.setId(bean.getId().toString());
        result.setIcon(bean.getIcon());
        result.setState(bean.getState());

        CmsPublishResourceInfoBean info = bean.getInfo();
        if (info != null) {
            result.setInfoType(info.getType().toString());
            result.setInfoValue(info.getValue());
        }

        for (CmsPublishResourceBean relatedBean : bean.getRelated()) {
            result.addRelated(toClientResourceBean(relatedBean));
        }
        return result;
    }

    /**
     * Helper method for converting multiple server side to client side resource beans.<p>
     * 
     * @param beans a list of server-side resource bean
     * 
     * @return a list of client-side resource beans  
     */
    private List<CmsClientPublishResourceBean> toClientResourceBeans(List<CmsPublishResourceBean> beans) {

        List<CmsClientPublishResourceBean> result = new ArrayList<CmsClientPublishResourceBean>();
        for (CmsPublishResourceBean bean : beans) {
            result.add(toClientResourceBean(bean));
        }
        return result;
    }

    /**
     * Helper method for converting a list of UUID strings to resources.<p>
     * 
     * @param cms the CmObject used for reading the resources 
     * @param uuids the list of uuid strings
     * 
     * @return a list of resources 
     * 
     * @throws CmsException if something goes wrong 
     */
    private List<CmsResource> uuidStringsToResources(CmsObject cms, List<String> uuids) throws CmsException {

        List<CmsResource> result = new ArrayList<CmsResource>();
        for (String id : uuids) {
            CmsUUID uuid = new CmsUUID(id);
            CmsResource resource = cms.readResource(uuid, CmsResourceFilter.ALL);
            result.add(resource);
        }
        return result;
    }

    /**
     * Helper method for converting a list of uuid strings to uuid objects.<p>
     * 
     * @param uuids a list of uuid strings
     *  
     * @return a list of CmsUUIDs
     */
    private List<CmsUUID> uuidStringsToUuids(List<String> uuids) {

        List<CmsUUID> result = new ArrayList<CmsUUID>();
        for (String id : uuids) {
            result.add(new CmsUUID(id));
        }
        return result;
    }
}
