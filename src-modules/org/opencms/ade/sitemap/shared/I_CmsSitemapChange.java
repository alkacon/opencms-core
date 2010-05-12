/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/shared/Attic/I_CmsSitemapChange.java,v $
 * Date   : $Date: 2010/05/12 10:14:06 $
 * Version: $Revision: 1.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.ade.sitemap.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Stores one change to the sitemap.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.2 $
 * 
 * @since 8.0.0
 */
public interface I_CmsSitemapChange extends IsSerializable {

    /**
     * The change type.<p>
     */
    public enum Type {

        /** Delete entry. */
        DELETE,

        /** Edit entry. */
        EDIT,

        /** Move entry. */
        MOVE,

        /** New entry. */
        NEW;
    }

    /**
     * Returns the change type.<p>
     * 
     * @return the change type
     */
    Type getType();

    /**
     * Returns the revert of this change for undoing.<p>
     * 
     * @return the revert change
     */
    I_CmsSitemapChange revert();
}