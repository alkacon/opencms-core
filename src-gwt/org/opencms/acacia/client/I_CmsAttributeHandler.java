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

package org.opencms.acacia.client;

/**
 * Attribute handler interface.<p>
 */
public interface I_CmsAttributeHandler {

    /**
     * Returns the simple path.<p>
     * 
     * @param childHandler the child attribute handler
     * 
     * @return the simple path
     */
    String collectSimplePath(I_CmsAttributeHandler childHandler);

    /**
     * Returns the attribute name.<p>
     * 
     * @return the attribute name
     */
    String getAttributeName();

    /**
     * Returns the handler for the given attribute at the given index.<p>
     * 
     * @param attributeName the attribute name
     * @param index the value index
     * 
     * @return the handler
     */
    CmsAttributeHandler getChildHandler(String attributeName, int index);

    /**
     * Returns the child handler by simple name.<p>
     * 
     * @param name the name
     * @param index the value index
     * 
     * @return the child handler if present
     */
    CmsAttributeHandler getChildHandlerBySimpleName(String name, int index);

    /**
     * Inserts a handler map at the given index.<p>
     * 
     * @param index the value index
     */
    void insertHandlers(int index);

    /**
     * Removes the handlers at the given index.<p>
     * 
     * @param index the value index
     */
    void removeHandlers(int index);

    /**
     * Sets a child attribute handler.<p>
     * 
     * @param index the value index
     * @param attributeName the attribute name
     * @param handler the handler
     */
    void setHandler(int index, String attributeName, CmsAttributeHandler handler);

    /**
     * Sets the handler by id.<p>
     * 
     * @param attributeName the attribute name
     * @param handler the handler
     */
    void setHandlerById(String attributeName, CmsAttributeHandler handler);
}
