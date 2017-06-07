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

package org.opencms.file.collectors;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;

/**
 * Interface which can be used to add custom code to execute after a user has created a new content
 * via a collector list.<p>
 */
public interface I_CmsCollectorPostCreateHandler {

    /**
     * This is called after the new content has been created (and possibly already been filled with content).<p>
     *
     * @param cms the current user's CMS context
     * @param createdResource the resource which has been created
     * @param copyMode true if the user chose one of the elements in the collector list as a model
     */
    void onCreate(CmsObject cms, CmsResource createdResource, boolean copyMode);
}
