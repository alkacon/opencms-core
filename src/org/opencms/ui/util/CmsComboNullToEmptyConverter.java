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

package org.opencms.ui.util;

import java.util.Locale;

import com.vaadin.data.util.converter.Converter;

/**
 * Converts null values to an empty string for the input widgets.<p>
 */
public class CmsComboNullToEmptyConverter implements Converter<Object, String> {

    /** Serial version id. */
    private static final long serialVersionUID = 1L;

    /**
     * @see com.vaadin.data.util.converter.Converter#convertToModel(java.lang.Object, java.lang.Class, java.util.Locale)
     */
    public String convertToModel(Object value, Class<? extends String> targetType, Locale locale)
    throws com.vaadin.data.util.converter.Converter.ConversionException {

        return (String)value;
    }

    /**
     * @see com.vaadin.data.util.converter.Converter#convertToPresentation(java.lang.Object, java.lang.Class, java.util.Locale)
     */
    public Object convertToPresentation(String value, Class<? extends Object> targetType, Locale locale)
    throws com.vaadin.data.util.converter.Converter.ConversionException {

        if (value == null) {
            return "";
        }
        return value;

    }

    /**
     * @see com.vaadin.data.util.converter.Converter#getModelType()
     */
    public Class<String> getModelType() {

        return String.class;
    }

    /**
     * @see com.vaadin.data.util.converter.Converter#getPresentationType()
     */
    public Class<Object> getPresentationType() {

        return Object.class;
    }

}