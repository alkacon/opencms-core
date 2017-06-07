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

package org.opencms.ade.galleries.client.preview;

import java.util.Map;

import com.google.gwt.user.client.Command;

/**
 * The properties tab handler of the preview dialog.<p>
 *
 * This class receives event information from the properties tab and
 * delegates it to the preview controller.
 *
 * @since 8.0.0
 */
public interface I_CmsPropertiesHandler {

    /**
     * Saves the properties.<p>
     *
     * @param properties the properties to save
     * @param afterSaveCommand the command to execute after saving the properties
     */
    void saveProperties(Map<String, String> properties, Command afterSaveCommand);

    /**
     * Selects the current resource and sets its path into the xml-content field or editor link, depending on the gallery mode.<p>
     */
    void selectResource();

}