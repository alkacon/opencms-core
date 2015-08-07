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

package org.opencms.ui.components;

import com.vaadin.data.Validator.InvalidValueException;

/**
 * File table inline edit handler.<p>
 */
public interface I_CmsFilePropertyEditHandler {

    /**
     * Cancels the edit process.<p>
     */
    void cancel();

    /**
     * Saves the value.<p>
     *
     * @param value the value to save
     */
    void save(String value);

    /**
     * Validates the value.<p>
     *
     * @param value the value
     *
     * @throws InvalidValueException in case the value is not valid
     */
    void validate(String value) throws InvalidValueException;

}
