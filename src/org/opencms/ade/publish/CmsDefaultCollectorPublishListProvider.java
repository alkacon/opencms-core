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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.collectors.I_CmsCollectorPublishListProvider;
import org.opencms.gwt.shared.CmsGwtConstants;
import org.opencms.gwt.shared.I_CmsContentLoadCollectorInfo;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

import java.util.Set;

import org.apache.commons.lang.math.NumberUtils;

/**
 * Default implementation of the I_CmsCollectorPublishListProvider interface.<p>
 *
 * Uses a CmsCollectorPublishListHelper to generate the publish list for a collector.
 */
public class CmsDefaultCollectorPublishListProvider implements I_CmsCollectorPublishListProvider {

    /** The default limit for collector results. */
    public static final int DEFAULT_LIMIT = 200;

    /**
     * @see org.opencms.file.collectors.I_CmsCollectorPublishListProvider#getPublishResources(org.opencms.file.CmsObject, org.opencms.gwt.shared.I_CmsContentLoadCollectorInfo)
     */
    public Set<CmsResource> getPublishResources(CmsObject cms, I_CmsContentLoadCollectorInfo info) throws CmsException {

        int collectorLimit = NumberUtils.toInt(
            OpenCms.getADEManager().getParameters(cms).get(CmsGwtConstants.COLLECTOR_PUBLISH_LIST_LIMIT),
            DEFAULT_LIMIT);
        CmsCollectorPublishListHelper helper = new CmsCollectorPublishListHelper(cms, info, collectorLimit);
        return helper.getPublishListFiles();
    }
}
