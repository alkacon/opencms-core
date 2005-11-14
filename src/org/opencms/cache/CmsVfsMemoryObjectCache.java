/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/cache/CmsVfsMemoryObjectCache.java,v $
 * Date   : $Date: 2005/11/14 15:12:44 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.cache;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.xml.Messages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;

/**
 * Implements a memory cache, that stores objects related to VFS files, 
 * providing a cache for the "online" and another for the "offline" project.<p>
 * 
 * @author Alexander Kandzior 
 * @author Michael Emmerich
 * 
 * @version $Revision: 1.1.2.2 $
 * 
 * @since 6.1.3
 */
public class CmsVfsMemoryObjectCache implements I_CmsEventListener {

    /** The log object for this class. */
    private static final Log LOG = CmsLog.getLog(CmsVfsMemoryObjectCache.class);

    /** The admin CmsObject for access. */
    private CmsObject m_adminCms;

    /** The cache map. */
    private Map m_cache;

    /**
     * Constructor, creates a new CmsVfsMemoryObjectCache.<p>
     * 
     * @param adminCms admin CmsObject for access
     */
    public CmsVfsMemoryObjectCache(CmsObject adminCms) {

        try {
            m_adminCms = OpenCms.initCmsObject(adminCms);
        } catch (CmsException e) {
            // log something
        }
        m_cache = new HashMap();
        registerEventListener();
    }

    /**
     * @see org.opencms.main.I_CmsEventListener#cmsEvent(org.opencms.main.CmsEvent)
     */
    public void cmsEvent(CmsEvent event) {

        CmsResource resource;
        switch (event.getType()) {
            case I_CmsEventListener.EVENT_PUBLISH_PROJECT:
            case I_CmsEventListener.EVENT_CLEAR_CACHES:
                // flush cache   
                m_cache.clear();
                if (LOG.isDebugEnabled()) {
                    LOG.debug(Messages.get().key(Messages.LOG_ER_FLUSHED_CACHES_0));
                }
                break;
            case I_CmsEventListener.EVENT_RESOURCE_MODIFIED:
                resource = (CmsResource)event.getData().get("resource");
                uncacheSystemId(resource.getRootPath());
                break;
            case I_CmsEventListener.EVENT_RESOURCE_DELETED:
                List resources = (List)event.getData().get("resources");
                for (int i = 0; i < resources.size(); i++) {
                    resource = (CmsResource)resources.get(i);
                    uncacheSystemId(resource.getRootPath());
                }
                break;
            default:
        // no operation
        }
    }

    /**
     * Get anobject from the cache.<p>
     * 
     * @param cms the CmsObject
     * @param rootPath the rootPath of the VFS resource to get the object for 
     * @return object form cache or null
     */
    public Object getCachedObject(CmsObject cms, String rootPath) {

        String key = getCacheKeyForCurrentProject(cms, rootPath);
        return m_cache.get(key);
    }

    /**
     * Puts an object into the cache.<p>
     * 
     * @param cms the CmsObject
     * @param rootPath the rootPath of the VFS resource to store the object for 
     * @param value the object to store
     */
    public void putCachedObject(CmsObject cms, String rootPath, Object value) {

        String key = getCacheKeyForCurrentProject(cms, rootPath);
        m_cache.put(key, value);
    }

    /**
     * Registers all required event listeners.<p>
     */
    protected void registerEventListener() {

        // register this object as event listener
        OpenCms.addCmsEventListener(this, new int[] {
            I_CmsEventListener.EVENT_CLEAR_CACHES,
            I_CmsEventListener.EVENT_PUBLISH_PROJECT,
            I_CmsEventListener.EVENT_RESOURCE_MODIFIED,
            I_CmsEventListener.EVENT_RESOURCE_DELETED});
    }

    /**
     * Returns a cache key for the given system id (filename) based on the status 
     * of the given project flag.<p>
     * 
     * @param systemId the system id (filename) to get the cache key for
     * @param online indicates if this key is generated for the online project
     * 
     * @return the cache key for the system id
     */
    private String getCacheKey(String systemId, boolean online) {

        if (online) {
            return "online_".concat(systemId);
        }
        return "offline_".concat(systemId);
    }

    /**
     * Returns a cache key for the given root path based on the status 
     * of the given CmsObject.<p>
     * 
     * @param systemId the system id (filename) to get the cache key for
     * 
     * @return the cache key for the system id
     */
    private String getCacheKeyForCurrentProject(CmsObject cms, String rootPath) {

        // check the project
        boolean project = (cms != null) ? cms.getRequestContext().currentProject().isOnlineProject() : false;

        return getCacheKey(rootPath, project);
    }

    /**
     * Uncaches a system id (filename) from the internal offline temporary and content defintions caches.<p>
     * 
     * The online resources cached for the online project are only flushed when a project is published.<p>
     * 
     * @param systemId the system id (filename) to uncache
     */
    private void uncacheSystemId(String systemId) {

        Object o;
        o = m_cache.remove(getCacheKey(systemId, false));
        if ((null != o) && LOG.isDebugEnabled()) {
            LOG.debug(Messages.get().key(Messages.LOG_ER_UNCACHED_SYS_ID_1, getCacheKey(systemId, false)));
        }
    }
}