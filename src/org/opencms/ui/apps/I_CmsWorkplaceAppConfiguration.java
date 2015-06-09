/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ui.apps;

import org.opencms.file.CmsObject;

import java.util.Locale;

import com.vaadin.server.Resource;

/**
 * Contains the configuration of a single workplace app.<p>
 */
public interface I_CmsWorkplaceAppConfiguration {

    /**
     * Returns a new app instance.<p>
     * 
     * @return a new app instance
     */
    I_CmsWorkplaceApp getAppInstance();

    /**
     * Returns the app path. Used to dispplay the apps in a hierarchical order and as a unique id.<p>
     * 
     * @return the app path
     */
    String getAppPath();

    String getHelpText(Locale locale);

    /**
     * Returns the app icon resource.<p>
     * 
     * @return the icon resource
     */
    Resource getIcon();

    /**
     * Returns the display name of the app.<p>
     * 
     * @param locale the user locale
     * 
     * @return the app name
     */
    String getName(Locale locale);

    /**
     * Returns the visibility status of the app for the given user context.<p>
     * 
     * @param cms the user context
     * 
     * @return the visibility status
     */
    CmsAppVisibilityStatus getVisibility(CmsObject cms);

}
