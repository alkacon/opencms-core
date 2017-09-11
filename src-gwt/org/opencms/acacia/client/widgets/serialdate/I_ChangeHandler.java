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

package org.opencms.acacia.client.widgets.serialdate;

import org.opencms.acacia.client.widgets.serialdate.CmsSerialDateController.PatternDefaultValues;

import com.google.gwt.user.client.Command;

/**
 * Interface for a change handler that reacts on changes triggered by the serial date widget.
 *
 * @since 11.0
 *
 */

interface I_ChangeHandler {

    /**
     * Call this method in case of potential exception changes. This will show a confirmation dialog before a value change takes place
     * if there are exceptions defined currently and the condition <code>showDialog</code> holds.
     *
     * @param cmd command to execute the change, if it really should be executed.
     * @param showDialog flag, indicating if the dialog should really be shown.
     */
    void conditionallyRemoveExceptionsOnChange(Command cmd, boolean showDialog);

    /**
     * Returns the default values to set for patterns.
     * @return the default values to set for patterns.
     */
    PatternDefaultValues getPatternDefaultValues();

    /**
     * Call this method in case of potential exception changes. This will show a confirmation dialog before a value change takes place
     * if there are exceptions defined currently.
     *
     * @param cmd command to execute the change, if it really should be executed.
     */
    void removeExceptionsOnChange(Command cmd);

    /**
     * Call this method, if the size of the widget changes.
     */
    void sizeChanged();

    /**
     * The method to call when the value changes.
     */
    void valueChanged();
}