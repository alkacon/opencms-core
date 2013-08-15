/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.sitemap;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsUser;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * A class used to keep track of which user is editing the alias table from which sites.<p>
 * 
 * It consists of a table from site roots to users. The entries of the table will be removed 
 * if they haven't been updated in a given interval of time, but can also be removed manually.<p>
 * 
 */
public class CmsAliasEditorLockTable {

    /** 
     * The interval after which a lock which has not been updated will be removed.<p>
     */
    public static final long TIMEOUT_INTERVAL = 60 * 1000;

    /**
     * Internal map from site roots to users.
     */
    private Cache<String, CmsUser> m_map;

    /**
     * Creates a new lock table instance.<p>
     */
    public CmsAliasEditorLockTable() {

        CacheBuilder<Object, Object> mm = CacheBuilder.newBuilder().expireAfterWrite(
            TIMEOUT_INTERVAL,
            TimeUnit.MILLISECONDS);
        m_map = mm.build();
    }

    /**
     * Clears the entry for a given site root, but only if the user from the given CMS context matches the user in the entry.<p>
     * 
     * @param cms the current CMS context  
     * @param siteRoot the site root for which the entry should be cleared 
     */
    public void clear(CmsObject cms, String siteRoot) {

        CmsUser originalUser = m_map.getIfPresent(siteRoot);
        if ((originalUser == null) || !originalUser.equals(cms.getRequestContext().getCurrentUser())) {
            return;
        }
        m_map.invalidate(siteRoot);
    }

    /**
     * Tries to update or create an entry for the given user/site root combination.<p>
     * 
     * If this method succeeds, it will return null, but if another user has created an entry for the site root,
     * it will return that user.<p>
     * 
     * @param cms the current CMS context 
     * @param siteRoot the site root 
     * 
     * @return null of the user who has already created an entry 
     */
    public CmsUser update(CmsObject cms, String siteRoot) {

        CmsUser originalUser = m_map.getIfPresent(siteRoot);
        if ((originalUser == null) || originalUser.equals(cms.getRequestContext().getCurrentUser())) {
            m_map.put(siteRoot, cms.getRequestContext().getCurrentUser());
            return null;
        }
        return originalUser;
    }
}
