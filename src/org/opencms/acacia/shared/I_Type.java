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
 * Interface describing an attribute type.<p>
 */
public interface I_Type {

    /**
     * Adds an attribute to the type.<p>
     * 
     * @param attributeName the attribute name
     * @param attributeType the attribute type
     * @param minOccurrence the minimum occurrence of this attribute
     * @param maxOccurrence the axnimum occurrence of this attribute
     */
    void addAttribute(String attributeName, String attributeType, int minOccurrence, int maxOccurrence);

    /**
     * Returns the maximum occurrence of the given attribute.<p>
     * 
     * @param attributeName the attribute name
     * 
     * @return the maximum occurrence
     */
    int getAttributeMaxOccurrence(String attributeName);

    /**
     * Returns the minimum occurrence of the given attribute.<p>
     * 
     * @param attributeName the attribute name
     * 
     * @return the minimum occurrence
     */
    int getAttributeMinOccurrence(String attributeName);

    /**
     * The names of the attributes of this type.<p>
     * 
     * @return the attribute names
     */
    List<String> getAttributeNames();

    /**
     * Returns the type of the given attribute.<p>
     * 
     * @param attributeName the attribute name
     * 
     * @return the type of the given attribute
     */
    I_Type getAttributeType(String attributeName);

    /**
     * Returns the type name of the given attribute.<p>
     * 
     * @param attributeName the attribute name
     * 
     * @return the type name of the given attribute
     */
    String getAttributeTypeName(String attributeName);

    /**
     * Returns the maximum choice occurrence.<p>
     * 
     * @return the maximum choice occurrence
     */
    int getChoiceMaxOccurrence();

    /**
     * Returns the name of the type.<p>
     * 
     * @return the name of the type
     */
    String getId();

    /**
     * Returns if this is a choice type.<p>
     * 
     * @return <code>true</code> if this is a choice type
     */
    boolean isChoice();

    /**
     * Returns if this is a simple type. Simple types have no attributes.<p>
     * 
     * @return <code>true</code> if this is a simple type
     */
    boolean isSimpleType();

    /**
     * Sets the maximum choice occurrence.<p>
     * 
     * @param choiceMaxOccurrence the maximum choice occurrence
     */
    void setChoiceMaxOccurrence(int choiceMaxOccurrence);
}
