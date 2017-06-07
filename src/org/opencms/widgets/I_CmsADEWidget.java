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

package org.opencms.widgets;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsMessages;
import org.opencms.xml.content.I_CmsXmlContentHandler.DisplayType;
import org.opencms.xml.types.A_CmsXmlContentValue;

import java.util.List;
import java.util.Locale;

/**
 * Widgets to be used with the new XML content editor are required to implement this interface.<p>
 */
public interface I_CmsADEWidget extends I_CmsWidget {

    /**
     * Returns the configuration string for the ADE content editor widget.<p>
     *
     * @param cms the OpenCms context
     * @param contentValue the schema type
     * @param messages the messages
     * @param resource the edited resource
     * @param contentLocale the content locale
     *
     * @return the configuration string
     */
    String getConfiguration(
        CmsObject cms,
        A_CmsXmlContentValue contentValue,
        CmsMessages messages,
        CmsResource resource,
        Locale contentLocale);

    /**
     * Returns a list of CSS resources required by the widget.<p>
     *
     * @param cms the current OpenCms context
     *
     * @return the required CSS resource links
     */
    List<String> getCssResourceLinks(CmsObject cms);

    /**
     * Returns the default display type of this widget.<p>
     *
     * @return the default display type
     */
    DisplayType getDefaultDisplayType();

    /**
     * Returns the java script initialization call.<p>
     *
     * @return the java script initialization call
     */
    String getInitCall();

    /**
     * Returns a list of java script resources required by the widget.<p>
     *
     * @param cms the current OpenCms context
     *
     * @return the required java script resource links
     */
    List<String> getJavaScriptResourceLinks(CmsObject cms);

    /**
     * Returns the class name of the widget.<p>
     *
     * @return the class name
     */
    String getWidgetName();

    /**
     * Returns if this is an internal widget.<p>
     * Only widgets belonging to the OpenCms core should be marked as internal.<p>
     *
     * @return <code>true</code> if this is an internal widget
     */
    boolean isInternal();

}
