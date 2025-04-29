/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.configuration;

import org.opencms.file.CmsObject;

/**
 * Interface for plugin classes that provide extra information to be dynamically injected into the sitemap configuration.
 */
public interface I_CmsSitemapExtraInfoProvider {

    /**
     * Gets the information to be injected into the sitemap configuration.
     *
     * @param cms an Admin CMS context (does not contain the current request context, but is in the Online/Offline project depending on whether the online/offline sitemap configuration is being evaluated)
     *
     * @return the extra information to be injected into the sitemap configuration
     */
    I_CmsSitemapExtraInfo getExtraInfo(CmsObject cms);

    /**
     * Gets the order number.
     *
     * <p>Implementations of this interface are used in ascending order, so the ones with higher order can override information from ones with a lower order.
     *
     * @return the order number
     */
    int getOrder();

}
