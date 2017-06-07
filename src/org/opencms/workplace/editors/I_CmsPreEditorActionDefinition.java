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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.workplace.editors;

import org.opencms.configuration.I_CmsConfigurationParameterHandler;
import org.opencms.file.CmsResource;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.workplace.CmsDialog;

/**
 * Defines an action to be performed before the workplace editor is opened for the first time.<p>
 *
 * @since 6.5.4
 */
public interface I_CmsPreEditorActionDefinition extends I_CmsConfigurationParameterHandler {

    /**
     * Returns if an action has to be performed before opening the editor depending on the resource to edit
     * and eventual request parameter values.<p>
     *
     * @param resource the resource to be edited
     * @param dialog the dialog instance
     * @param originalParams the original request parameters as String passed to the editor
     * @return true if an action has to be performed before opening the editor
     * @throws Exception if something goes wrong
     */
    boolean doPreAction(CmsResource resource, CmsDialog dialog, String originalParams) throws Exception;

    /**
     * Returns the resource type for which the action should be performed.<p>
     *
     * @return the resource type
     */
    I_CmsResourceType getResourceType();

    /**
     * Returns the resource type name for which the action should be performed.<p>
     *
     * @return the resource type name
     */
    String getResourceTypeName();

    /**
     * Sets the resource type name for which the action should be performed.<p>
     *
     * @param resourceTypeName the resource type name
     */
    void setResourceTypeName(String resourceTypeName);
}
