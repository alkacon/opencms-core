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

package org.opencms.cmis;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;

/**
 * Interface for dynamic CMIS properties.<p>
 *
 * Dynamic properties don't actually exist in the VFS, instead they are read and written by calling
 * methods on the subclasses implementing this interface which have been configured in the repository configuration.<p>
 *
 */
public interface I_CmsPropertyProvider {

    /**
     * Returns the name of the dynamic property.<p>
     *
     * The id of the property will consist of the name combined with an opencms-dynamic: prefix.<p>
     *
     * @return the name of the property
     */
    String getName();

    /**
     * Reads the property value.<p>
     *
     * @param cms the current CMS context
     * @param resource the resource for which the property should be read
     *
     * @return the property value
     *
     * @throws CmsException if something goes wrong
     */
    String getPropertyValue(CmsObject cms, CmsResource resource) throws CmsException;

    /**
     * Returns true if this dynamic property is writable.<p>
     *
     * @return true if the property is writable
     */
    boolean isWritable();

    /**
     * Writes the property value.<p>
     *
     * @param cms the current CMS context.<p>
     *
     * @param resource the resource for which to write the property
     * @param valueToSet the value to write
     *
     * @throws CmsException if something goes wrong
     */
    void setPropertyValue(CmsObject cms, CmsResource resource, String valueToSet) throws CmsException;
}
