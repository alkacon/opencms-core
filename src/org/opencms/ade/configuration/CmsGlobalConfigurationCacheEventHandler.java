/*
 * File   : $Source$
 * Date   : $Date$
 * Version: $Revision$
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2011 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.configuration;

import org.opencms.db.CmsDriverManager;
import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.util.CmsCollectionsGenericWrapper;
import org.opencms.util.CmsUUID;

import java.util.List;

import org.apache.commons.logging.Log;

/**
 * This event handler class is intended to be used with two instances of a class implementing the {@link I_CmsGlobalConfigurationCache} interface,
 * one for the offline project and one for the online project. It listens to OpenCms events regarding modifications of
 * resources and notifies those underlying cache instances when resources need to be updated or removed from the cache.<p>
 * 
 * Note that *all* changed resources will get passed to the underlying cache instances, so those instances will need to check
 * whether the resource passed into the update or remove methods is actually a resource with which the cache instance is concerned.<p>
 * 
 * This class should be used if you have an indefinite number of configuration files at locations in the VFS which 
 * can only be known at runtime, like e.g. the ADE configuration and the inherited container configurations,
 * and you need to know the state of the whole set of existing configurations. In this situation, using e.g. CmsVfsMemoryObjectCache
 * is unsatisfactory because it can't distinguish between a configuration not being cached and a configuration which 
 * is known to not exist.<p>  
 */
public class CmsGlobalConfigurationCacheEventHandler implements I_CmsEventListener {

    private static final Log LOG = CmsLog.getLog(CmsGlobalConfigurationCacheEventHandler.class);
    private I_CmsGlobalConfigurationCache m_offlineCache;

    private I_CmsGlobalConfigurationCache m_onlineCache;
    private CmsObject m_onlineCms;

    public CmsGlobalConfigurationCacheEventHandler(
        I_CmsGlobalConfigurationCache offlineCache,
        I_CmsGlobalConfigurationCache onlineCache,
        CmsObject onlineCms) {

        m_offlineCache = offlineCache;
        m_onlineCache = onlineCache;
        m_onlineCms = onlineCms;
    }

    /**
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {

        CmsResource resource = null;
        List<CmsResource> resources = null;
        //System.out.println();
        switch (event.getType()) {
            case I_CmsEventListener.EVENT_RESOURCE_AND_PROPERTIES_MODIFIED:
            case I_CmsEventListener.EVENT_RESOURCE_MODIFIED:
            case I_CmsEventListener.EVENT_RESOURCE_CREATED:
                //System.out.print(getEventName(event.getType()));
                Object change = event.getData().get(I_CmsEventListener.KEY_CHANGE);
                if ((change != null) && change.equals(new Integer(CmsDriverManager.NOTHING_CHANGED))) {
                    // skip lock & unlock
                    return;
                }
                resource = (CmsResource)event.getData().get(I_CmsEventListener.KEY_RESOURCE);
                m_offlineCache.update(resource);
                //System.out.print(" " + resource.getRootPath());
                break;
            case I_CmsEventListener.EVENT_RESOURCES_AND_PROPERTIES_MODIFIED:
                // a list of resources and all of their properties have been modified
                //System.out.print(getEventName(event.getType()));
                resources = CmsCollectionsGenericWrapper.list(event.getData().get(I_CmsEventListener.KEY_RESOURCES));
                for (CmsResource res : resources) {
                    m_offlineCache.update(res);
                    //System.out.print(" " + res.getRootPath());
                }
                break;

            case I_CmsEventListener.EVENT_RESOURCE_MOVED:
                resources = CmsCollectionsGenericWrapper.list(event.getData().get(I_CmsEventListener.KEY_RESOURCES));
                // source, source folder, dest, dest folder 
                // - OR -  
                // source, dest, dest folder
                m_offlineCache.remove(resources.get(0));
                m_offlineCache.update(resources.get(resources.size() - 2));
                break;

            case I_CmsEventListener.EVENT_RESOURCE_DELETED:
                resources = CmsCollectionsGenericWrapper.list(event.getData().get(I_CmsEventListener.KEY_RESOURCES));
                for (CmsResource res : resources) {
                    m_offlineCache.remove(res);
                }
                break;
            case I_CmsEventListener.EVENT_RESOURCES_MODIFIED:
                //System.out.print(getEventName(event.getType()));
                // a list of resources has been modified
                resources = CmsCollectionsGenericWrapper.list(event.getData().get(I_CmsEventListener.KEY_RESOURCES));
                for (CmsResource res : resources) {
                    m_offlineCache.update(res);
                }
                break;
            case I_CmsEventListener.EVENT_CLEAR_ONLINE_CACHES:
                m_onlineCache.clear();
                break;
            case I_CmsEventListener.EVENT_PUBLISH_PROJECT:
                //System.out.print(getEventName(event.getType()));
                String publishIdStr = (String)event.getData().get(I_CmsEventListener.KEY_PUBLISHID);
                if (publishIdStr != null) {
                    CmsUUID publishId = new CmsUUID(publishIdStr);
                    try {
                        List<CmsPublishedResource> publishedResources = m_onlineCms.readPublishedResources(publishId);
                        if (publishedResources.isEmpty()) {
                            // normally, the list of published resources should not be empty.
                            // If it is, the publish event is not coming from a normal publish process,
                            // so we re-initialize the whole cache to be on the safe side.
                            m_onlineCache.clear();
                        } else {
                            for (CmsPublishedResource res : publishedResources) {
                                if (res.getState().isDeleted()) {
                                    m_onlineCache.remove(res);
                                } else {
                                    m_onlineCache.update(res);
                                }
                            }
                        }
                    } catch (CmsException e) {
                        LOG.error(e.getLocalizedMessage(), e);
                    }
                }
                break;
            case I_CmsEventListener.EVENT_CLEAR_CACHES:
                //System.out.print(getEventName(event.getType()));
                m_offlineCache.clear();
                m_onlineCache.clear();
                break;
            case I_CmsEventListener.EVENT_CLEAR_OFFLINE_CACHES:
                //System.out.print(getEventName(event.getType()));
                m_offlineCache.clear();
                break;
            default:
                // noop
                break;
        }
    }
}
