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

package org.opencms.ade.sitemap;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsProperty;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.lock.CmsLock;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.util.Arrays;

import org.apache.commons.logging.Log;

/**
 * Helper methods for sitemap-related functionality.
 */
public final class CmsSitemapUtil {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSitemapUtil.class);

    /** Private constructor for utility class. */
    private CmsSitemapUtil() {

        // do nothing
    }

    /**
     * Ensures that the containerpage.format property is set on a folder according with the default setting in opencms-workplace.xml.
     *
     * If the property is already set on the folder, this method does not change it.
     *
     * @param cms the CMS context
     * @param folder a folder
     *
     * @throws CmsException if something goes wrong
     */
    public static void updatePageFormatProperty(CmsObject cms, CmsResource folder) throws CmsException {

        CmsProperty prop = cms.readPropertyObject(folder, CmsPropertyDefinition.PROPERTY_CONTAINERPAGE_FORMAT, false);
        if (prop.isNullProperty()) {
            CmsLock lock = cms.getLock(folder);
            boolean needToUnlock = false;
            if (!lock.isOwnedBy(cms.getRequestContext().getCurrentUser())) {
                cms.lockResourceShallow(folder);
                needToUnlock = true;
            }
            try {
                String defaultFormat = "1";
                if (OpenCms.getWorkplaceManager().isUseFormatterKeysForNewSites()) {
                    defaultFormat = "2";
                }
                LOG.info("Setting page format on " + folder.getRootPath() + " to " + defaultFormat);
                cms.writePropertyObjects(
                    folder,
                    Arrays.asList(
                        new CmsProperty(CmsPropertyDefinition.PROPERTY_CONTAINERPAGE_FORMAT, defaultFormat, null)));

            } finally {
                if (needToUnlock) {
                    cms.unlockResource(folder);
                }
            }
        } else {
            LOG.info(
                "Not setting container page format on "
                    + folder.getRootPath()
                    + " because it is already set to "
                    + prop.getValue());
        }
    }

}
