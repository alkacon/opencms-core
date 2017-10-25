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

package org.opencms.ui.actions;

import java.util.Map;

/**
 * Interface for actions used within the ADE context.<p>
 */
public interface I_CmsADEAction {

    /**
     * Returns the client side command to execute.<p>
     *
     * @return the client side command
     */
    String getCommandClassName();

    /**
     * Returns the optional dialog JSP.<p>
     *
     * @return the dialog JSP
     */
    String getJspPath();

    /**
     * Returns the optional command parameters.<p>
     *
     * @return the command parameters
     */
    Map<String, String> getParams();

    /**
     * Returns whether the ADE context is supported.<p>
     *
     * @return <code>true</code> in case the ADE context is supported
     */
    boolean isAdeSupported();
}
