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

package org.opencms.acacia.client.export;

import org.opencms.acacia.shared.I_Entity;
import org.opencms.acacia.shared.I_EntityAttribute;

import java.util.List;

/**
 * Utility class with helper methods for wrapping objects.<p>
 */
public class WrapperUtils {

    /**
     * Creates an array of entity attribute wrappers for a list of entity attributes.<p>
     * 
     * @param attributes the list of attributes 
     * @return the array of attribute wrappers 
     */
    public static EntityAttributeWrapper[] arrayFromEntityAttributeList(List<I_EntityAttribute> attributes) {

        EntityAttributeWrapper[] result = new EntityAttributeWrapper[attributes.size()];
        for (int i = 0; i < attributes.size(); i++) {
            result[i] = new EntityAttributeWrapper(attributes.get(i));
        }
        return result;
    }

    /**
     * Creates an array of entity wrappers for a list of entities.<p> 
     * 
     * @param entities the list of entities 
     * @return the array of entity wrappers 
     */
    public static EntityWrapper[] arrayFromEntityList(List<I_Entity> entities) {

        EntityWrapper[] result = new EntityWrapper[entities.size()];
        for (int i = 0; i < entities.size(); i++) {
            result[i] = new EntityWrapper(entities.get(i));
        }
        return result;
    }

    /**
     * Converts a list of strings to an array.<p>
     * 
     * @param strings the string list 
     * 
     * @return the array of strings 
     */
    public static String[] arrayFromStringList(List<String> strings) {

        String[] result = new String[strings.size()];
        for (int i = 0; i < strings.size(); i++) {
            result[i] = strings.get(i);
        }
        return result;
    }

}
