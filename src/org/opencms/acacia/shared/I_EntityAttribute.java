/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.acacia.shared;

import java.util.List;

/**
 * Interface describing an entity attribute value.<p>
 */
public interface I_EntityAttribute {

    /**
     * Returns the attribute name.<p>
     * 
     * @return the attribute name
     */
    String getAttributeName();

    /**
     * Returns the first complex value in the list.<p>
     * 
     * @return the first complex value
     */
    I_Entity getComplexValue();

    /**
     * Returns the list of complex values.<p>
     * 
     * @return the list of complex values
     */
    List<I_Entity> getComplexValues();

    /**
     * Returns the first simple value in the list.<p>
     * 
     * @return the first simple value
     */
    String getSimpleValue();

    /**
     * Returns the list of simple values.<p>
     * 
     * @return the list of simple values
     */
    List<String> getSimpleValues();

    /**
     * Returns the number of values set for this attribute.<p>
     * 
     * @return the number of values
     */
    int getValueCount();

    /**
     * Returns if the is a complex type value.<p>
     * 
     * @return <code>true</code> if this is a complex type value
     */
    boolean isComplexValue();

    /**
     * Returns if the is a simple type value.<p>
     * 
     * @return <code>true</code> if this is a simple type value
     */
    boolean isSimpleValue();

    /**
     * Returns if this is a single value attribute.<p>
     * 
     * @return <code>true</code> if this is a single value attribute
     */
    boolean isSingleValue();
}
