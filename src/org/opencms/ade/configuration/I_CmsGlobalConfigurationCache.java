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

import org.opencms.db.CmsPublishedResource;
import org.opencms.file.CmsResource;

/**
 * This is interface is intended to be used in combination with the {@link CmsGlobalConfigurationCacheEventHandler} class.<p>
 *
 * It provides several method which allow the mentioned event handler class to update the cache object implementing this
 * interface.
 */
public interface I_CmsGlobalConfigurationCache {

    /**
     * Clears the cache.<p>
     */
    void clear();

    /**
     * Removes a published resource from the cache.<p>
     *
     * @param pubRes the published resource
     */
    void remove(CmsPublishedResource pubRes);

    /**
     * Removes a resource from the cache.<p>
     *
     * @param resource the resource to remove
     */
    void remove(CmsResource resource);

    /**
     * Updates the cache entry for the given published resource.<p>
     *
     * NOTE: Cache implementations should not directly read the updated resource in this method because it might interfere with other
     * caches. Instead, the resource should be marked as updated and read the next time the cache is queried.
     *
     * @param pubRes a published resource
     */
    void update(CmsPublishedResource pubRes);

    /**
     * Updates the cache entry for the given resource.<p>
     *
     * NOTE: Cache implementations should not directly read the updated resource in this method because it might interfere with other
     * caches. Instead, the resource should be marked as updated and read the next time the cache is queried.
     *
     * @param resource the resource for which the cache entry should be updated
     */
    void update(CmsResource resource);
}
