/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/containerpage/Attic/I_CmsContainerElementBean.java,v $
 * Date   : $Date: 2009/10/13 11:59:41 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml.containerpage;

import org.opencms.file.CmsProperty;
import org.opencms.file.CmsResource;

import java.util.Map;

/**
 * Describes the API to access a single element in a container of a container page.<p>
 *
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.1 $ 
 * 
 * @since 7.9.2 
 */
public interface I_CmsContainerElementBean {

    /**
     * Returns the client side id including the property-hash.<p>
     * 
     * @return the id
     */
    String getClientId();

    /**
     * Returns the container element used to save favorite and recent-list entries.<p>
     * 
     * @return the CmsContainerElement representing this element bean
     */
    CmsContainerListElement getContainerElement();

    /**
     * Returns the element.<p>
     *
     * @return the element
     */
    CmsResource getElement();

    /**
     * Returns the formatter.<p>
     *
     * @return the formatter
     */
    CmsResource getFormatter();

    /**
     * Returns the properties. If no properties are set, an empty Map will be returned.<p>
     *
     * @return the properties
     */
    Map<String, CmsProperty> getProperties();

    /**
     * Returns a hash-code of all properties.<p>
     * 
     * @return the hash-code (0 if no properties are set)
     */
    int getPropertyHash();
}