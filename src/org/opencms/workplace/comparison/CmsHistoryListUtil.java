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

package org.opencms.workplace.comparison;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.history.CmsHistoryResourceHandler;
import org.opencms.main.CmsException;
import org.opencms.main.CmsRuntimeException;
import org.opencms.util.CmsUUID;
import org.opencms.workplace.commons.Messages;

import java.util.Locale;

/**
 * Utility methods for the history list.<p>
 */
public final class CmsHistoryListUtil {

    /**
     * Hidden default constructor.<p>
     */
    private CmsHistoryListUtil() {
        // nothing
    }

    /**
     * Returns the version number from a version parameter.<p>
     *
     * @param version might be negative for the online version
     * @param locale if the result is for display purposes, the locale has to be <code>!= null</code>
     *
     * @return the display name
     */
    public static String getDisplayVersion(String version, Locale locale) {

        int ver = Integer.parseInt(version);
        if (ver == CmsHistoryResourceHandler.PROJECT_OFFLINE_VERSION) {
            return Messages.get().getBundle(locale).key(Messages.GUI_PROJECT_OFFLINE_0);
        }
        if (ver < 0) {
            ver *= -1;
            if (locale != null) {
                return Messages.get().getBundle(locale).key(Messages.GUI_PROJECT_ONLINE_1, Integer.valueOf(ver));
            }
        }
        return "" + ver;
    }

    /**
     * Returns the link to an historical file.<p>
     *
     * @param cms the cms context
     * @param structureId the structure id of the file
     * @param version the version number of the file
     *
     * @return the link to an historical file
     */
    public static String getHistoryLink(CmsObject cms, CmsUUID structureId, String version) {

        String resourcePath;
        CmsResource resource;
        try {
            resource = cms.readResource(structureId, CmsResourceFilter.ALL);
            resourcePath = resource.getRootPath();
        } catch (CmsException e) {
            throw new CmsRuntimeException(e.getMessageContainer(), e);
        }
        StringBuffer link = new StringBuffer();
        link.append(CmsHistoryResourceHandler.HISTORY_HANDLER);
        link.append(resourcePath);
        link.append('?');
        link.append(CmsHistoryResourceHandler.PARAM_VERSION);
        link.append('=');
        link.append(CmsHistoryListUtil.getVersion("" + version));
        return link.toString();
    }

    /**
     * Returns the version number from a version parameter.<p>
     *
     * @param version might be negative for the online version
     *
     * @return the positive value
     */
    public static int getVersion(String version) {

        int ver = Integer.parseInt(version);
        return Math.abs(ver);
    }

}
