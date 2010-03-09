/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/gwt/client/ui/input/Attic/I_CmsFormWidget.java,v $
 * Date   : $Date: 2010/03/09 09:03:53 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

/**
 * Basic interface for all widgets that can be used for form fields.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.2 $ 
 * 
 * @since 8.0.0
 */
public interface I_CmsFormWidget {

    /**
     * Field type constants.<p>
     * 
     */
    public enum FieldType {
        /** 
         * Field type constant for booleans (Java type: Boolean) 
         **/
        BOOLEAN,
        /**
         * Field type constant for numbers (Java type: Double)
         */
        NUMBER,
        /**
         * Field type constant for strings (Java type: String)
         */
        STRING,
        /**
         * Field type constant for lists of strings (Java type: List<String>)
         */
        STRING_LIST
    }

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
     * Resets the widget to its default state.
     */
    void reset();

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
     * Sets the value of the widget.<p>
     * 
     * @param value the new value 
     */
    void setFormValue(Object value);

}
