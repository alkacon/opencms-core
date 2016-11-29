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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH & Co. KG, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.file.history;

import org.opencms.file.I_CmsResource;
import org.opencms.util.CmsUUID;

import java.io.Serializable;

/**
 * A historical version of a resource in the OpenCms VFS resource history.<p>
 *
 * History resources are resources that contain additional information
 * used to describe the historical state.<p>
 *
 * The historical resource object extends the resource object since it be
 * an history for a file as well as for a folder.<p>
 *
 * History resources contain the names of the users that created or last
 * modified the resource as string obejcts because a user id might have been
 * deleted.<p>
 *
 * @since 6.9.1
 */
public interface I_CmsHistoryResource extends I_CmsResource, Cloneable, Serializable, Comparable<I_CmsResource> {

    /**
     * Returns the structure id of the parent resource.<p>
     *
     * @return the structure id of the parent resource
     */
    CmsUUID getParentId();

    /**
     * Returns the publish tag of this historical resource.<p>
     *
     * @return the publish tag of this historical resource
     */
    int getPublishTag();

    /**
     * Returns the version number of the resource part for this historical resource.<p>
     *
     * @return the version number of the resource part for this historical resource
     */
    int getResourceVersion();

    /**
     * Returns the version number of the structure part for this historical resource.<p>
     *
     * @return the version number of the structure part for this historical resource
     */
    int getStructureVersion();

    /**
     * Returns the version number of this historical resource.<p>
     *
     * @return the version number of this historical resource
     */
    int getVersion();
}