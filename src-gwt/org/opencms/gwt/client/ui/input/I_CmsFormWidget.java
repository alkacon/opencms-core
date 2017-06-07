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

package org.opencms.gwt.client.ui.input;

import org.opencms.gwt.client.ui.I_CmsAutoHider;

/**
 * Basic interface for all widgets that can be used for form fields.<p>
 *
 * @since 8.0.0
 */
public interface I_CmsFormWidget {

    /**
     * Field type constants.<p>
     *
     */
    public enum FieldType {
        /** Field type constant for booleans (Java type: Boolean). */
        BOOLEAN, /** Field type constant for dates (Java type: Date). */
        DATE, /** Field type constant for numbers (Java type: Double). */
        NUMBER, /** Field type constant for strings (Java type: String). */
        STRING, /** Field type constant for lists of strings (Java type: List<String>). */
        STRING_LIST
    }

    /**
     * Returns the "apparent value", i.e. either the real value if available, or else the ghost value if available, or null otherwise.<p>
     *
     * @return the apparent value
     */
    String getApparentValue();

    /**
     * Returns the type of data this widget produces.
     * @return the data type
     */
    FieldType getFieldType();

    /**
     * Gets the selected/entered value from the widget.<p>
     *
     * @return the value
     */
    Object getFormValue();

    /**
     * Gets the current value of the widget as a string.<p>
     *
     * @return the current value of the widget
     */
    String getFormValueAsString();

    /**
     * Returns <code>true</code> if this widget is enabled.<p>
     *
     * @return <code>true</code> if this widget is enabled
     */
    boolean isEnabled();

    /**
     * Resets the widget to its default state.
     */
    void reset();

    /**
     * Call this when auto hiding parents are shown.<p>
     *
     * @param autoHideParent the auto hide parent
     */
    void setAutoHideParent(I_CmsAutoHider autoHideParent);

    /**
     * Enables or disables the widget.<p>
     *
     * @param enabled if true, the widget will be enabled, else disabled
     */
    void setEnabled(boolean enabled);

    /**
     * Sets the error message for this widget.<p>
     *
     * If the error message is null, no  error message will be displayed.
     *
     * @param errorMessage an error message or null
     */
    void setErrorMessage(String errorMessage);

    /**
     * Sets the current value of the widget as a string.<p>
     *
     * @param value the new value of the widget
     */
    void setFormValueAsString(String value);

}
