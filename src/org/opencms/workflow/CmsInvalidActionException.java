/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (https://www.alkacon.com)
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

package org.opencms.workflow;

import org.opencms.main.CmsException;

/**
 * An exception which indicates that a user tried to execute an invalid workflow action.<p>
 */
public class CmsInvalidActionException extends CmsException {

    /** The serial version uid. */
    private static final long serialVersionUID = -2768811398590156214L;

    /**
     * Creates a new exception instance for a given action string.<p>
     *
     * @param action the action string
     */
    public CmsInvalidActionException(String action) {

        super(Messages.get().container(Messages.ERR_INVALID_WORKFLOW_ACTION_1, action));
    }

}
