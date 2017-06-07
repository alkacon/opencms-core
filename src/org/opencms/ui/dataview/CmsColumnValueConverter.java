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

package org.opencms.ui.dataview;

import org.opencms.widgets.dataview.CmsDataViewColumn;
import org.opencms.widgets.dataview.CmsDataViewColumn.Type;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Component;
import com.vaadin.ui.Image;

/**
 * Converts column values to the correct types.<p>
 */
public final class CmsColumnValueConverter {

    /**
     * Hidden default constructor.<p>
     */
    private CmsColumnValueConverter() {
        // do nothing
    }

    /**
     * Gets the actual value class which the given type enum represents.<p>
     *
     * @param type the type enum
     * @return the actual value class to use
     */
    public static Class<?> getColumnClass(CmsDataViewColumn.Type type) {

        if (type == Type.imageType) {
            return Component.class;
        } else {
            return type.getValueClass();
        }
    }

    /**
     * Gets the actual column value for the given data value.<p>
     *
     * @param value the data value
     * @param type the column type enum
     *
     * @return the actual column value to use
     */
    public static Object getColumnValue(Object value, CmsDataViewColumn.Type type) {

        if (type == Type.imageType) {
            Image image = new Image("", new ExternalResource((String)value));
            image.addStyleName("o-table-image");
            return image;
        } else {
            return value;
        }
    }

}
