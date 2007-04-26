/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/file/history/I_CmsHistoryResource.java,v $
 * Date   : $Date: 2007/04/26 14:31:12 $
 * Version: $Revision: 1.1.2.1 $
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
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

package org.opencms.file.history;

import org.opencms.file.CmsResource;
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
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1.2.1 $
 * 
 * @since 6.9.1
 */
public interface I_CmsHistoryResource extends Cloneable, Serializable, Comparable {

    /**
     * Returns the history id of this historical resource.<p>
     *
     * @return the history id of this historical resource
     * 
     * @deprecated this field has been removed
     */
    CmsUUID getBackupId();

    /**
     * Returns the user name of the creator of this historical resource.<p>
     *
     * @return the user name of the creator of this historical resource
     */
    String getCreatedByName();

    /**
     * Returns the name of the user who last changed this historical resource.<p>
     *
     * @return the name of the user who last changed this historical resource
     */
    String getLastModifiedByName();

    /**
     * Returns the publish tag of this historical resource.<p>
     *
     * @return the publish tag of this historical resource
     */
    int getPublishTag();

    /**
     * Returns the publish tag of this historical resource.<p>
     *
     * @return the publish tag of this historical resource
     * 
     * @deprecated use {@link #getPublishTag()} instead
     */
    int getPublishTagId();

    /**
     * Gets a resource with all version relevant data.<p>
     * 
     * @return a resource with all version relevant data
     */
    CmsResource getResource();

    /**
     * Returns the version number of this historical resource.<p>
     *
     * @return the version number of this historical resource
     */
    int getVersion();
}
