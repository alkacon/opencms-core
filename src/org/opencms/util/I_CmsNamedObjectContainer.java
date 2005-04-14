/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/Attic/I_CmsNamedObjectContainer.java,v $
 * Date   : $Date: 2005/04/14 11:47:43 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.util;

import java.util.List;

/**
 * Container for named objects.<p>
 * 
 * @author Michael Moossen (m.moossen@alkacon.com) 
 * @version $Revision: 1.3 $
 * @since 5.7.3
 */
public interface I_CmsNamedObjectContainer {

    /**
     * Appends the specified object to the end of this container. <p>
     * 
     * @param namedObject the object to add to the container
     * 
     * @see java.util.List#add(Object)
     */
    void addNamedObject(I_CmsNamedObject namedObject);

    /**
     * Inserts the specified object at the specified position in this container.<p>
     * 
     * Shifts the object currently at that position (if any) and any subsequent 
     * objects to the right (adds one to their indices).<p>
     * 
     * @param namedObject the object add to the container
     * @param position the insertion point
     * 
     * @see java.util.List#add(int, Object)
     */
    void addNamedObject(I_CmsNamedObject namedObject, float position);

    /**
     * Resets the container.<p>
     */
    void clear();

    /**
     * Returns the list of objects.<p>
     *
     * @return the a list of <code>{@link I_CmsNamedObject}</code>s.
     */
    List elementList();

    /**
     * Returns a list of objects of a given class type.<p>
     * 
     * @param type the class type for filtering
     *
     * @return the a list of given class type objects.
     */
    List elementList(Class type);

    /**
     * Returns an object by name.<p>
     * 
     * @param name the name of the object
     * 
     * @return the object if found, or <code>null</code>
     * 
     * @see java.util.Map#get(Object)
     */
    I_CmsNamedObject getObject(String name);
}