/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/util/Attic/I_CmsIdentifiableObjectContainer.java,v $
 * Date   : $Date: 2005/06/23 10:47:10 $
 * Version: $Revision: 1.4 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (c) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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
 * Container for identifiable objects.<p>
 * 
 * @author Michael Moossen  
 * 
 * @version $Revision: 1.4 $ 
 * 
 * @since 6.0.0 
 */
public interface I_CmsIdentifiableObjectContainer {

    /**
     * Appends the specified object to the end of this container. <p>
     * 
     * @param id the object identifier
     * @param object the object add to the container
     * 
     * @see java.util.List#add(Object)
     */
    void addIdentifiableObject(String id, Object object);

    /**
     * Inserts the specified object at the specified position in this container.<p>
     * 
     * Shifts the object currently at that position (if any) and any subsequent 
     * objects to the right (adds one to their indices).<p>
     * 
     * @param id the object identifier
     * @param object the object add to the container
     * @param position the insertion point
     * 
     * @see java.util.List#add(int, Object)
     */
    void addIdentifiableObject(String id, Object object, float position);

    /**
     * Resets the container.<p>
     */
    void clear();

    /**
     * Returns the list of objects.<p>
     *
     * @return the a list of <code>{@link Object}</code>s.
     */
    List elementList();

    /**
     * Returns an object by id.<p>
     * 
     * @param id the id of the object
     * 
     * @return the object if found, or <code>null</code>
     * 
     * @see java.util.Map#get(Object)
     */
    Object getObject(String id);
}