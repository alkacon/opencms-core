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

package org.opencms.ui.apps;

import org.opencms.file.CmsObject;

import java.util.Locale;

import com.vaadin.server.Resource;

/**
 * Contains the configuration of a single workplace app.<p>
 */
public interface I_CmsWorkplaceAppConfiguration {

    /** Default priority. */
    public static final int DEFAULT_PRIORIY = 100;

    /**
     * Gets the id of the app category in which this app should be displayed (null for the root category).
     *
     * @return the app category id
     */
    String getAppCategory();

    /**
     * Returns a new app instance.<p>
     *
     * @return a new app instance
     */
    I_CmsWorkplaceApp getAppInstance();

    /**
     * Returns the button style.<p>
     *
     * @return the button style
     */
    String getButtonStyle();

    /**
     * Gets the help text for the app in the given locale.<p>
     *
     * @param locale the locale to use
     *
     * @return the help text
     */
    String getHelpText(Locale locale);

    /**
     * Returns the app icon resource.<p>
     *
     * @return the icon resource
     */
    Resource getIcon();

    /**
     * Returns the unique app id.
     *
     * @return the app id
     */
    String getId();

    /**
     * Returns the display name of the app.<p>
     *
     * @param locale the user locale
     *
     * @return the app name
     */
    String getName(Locale locale);

    /**
     * Gets an integer used to sort apps in a category.<p>
     *
     * @return the integer used as a sort key
     */
    int getOrder();

    /**
     * Gets the priority of the app configuration.<p>
     *
     * Between two apps with the same id and different priorities, the one with the higher priority will override
     * the one with the lower priority.<p>
     *
     * @return the priority
     */
    int getPriority();

    /**
     * Returns the visibility status of the app for the given user context.<p>
     *
     * @param cms the user context
     *
     * @return the visibility status
     */
    CmsAppVisibilityStatus getVisibility(CmsObject cms);

}
