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

package org.opencms.gwt.shared.property;

import java.util.HashMap;
import java.util.Map;

/**
 * A lazy initialized map of properties. Will return a property object for any key.<p>
 */
public class CmsLazyPropertyMap extends HashMap<String, CmsClientProperty> {

    /** Serial version id. */
    private static final long serialVersionUID = -404780265142470052L;

    /**
     * Constructor.<p>
     *
     * @param original properties that will be added to this map
     */
    public CmsLazyPropertyMap(Map<String, CmsClientProperty> original) {

        super(original);
    }

    /**
     * Returns the property with the given name.<p>
     *
     * @param key the property name
     *
     * @return the property
     */
    @Override
    public CmsClientProperty get(Object key) {

        CmsClientProperty result = super.get(key);
        if (result == null) {
            result = new CmsClientProperty((String)key, "", "");
            put((String)key, result);
        }
        return result;
    }
}