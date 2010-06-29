/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/galleries/shared/Attic/CmsVfsEntryBean.java,v $
 * Date   : $Date: 2010/06/29 09:38:46 $
 * Version: $Revision: 1.1 $
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

package org.opencms.ade.galleries.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Represents a single VFS resource entry for use by the VFS tab of the galleries.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 */
public class CmsVfsEntryBean implements IsSerializable {

    /** Flag indicating whether this is entry should be displayed at the top level of the tree. */
    private boolean m_isRoot;

    /** The site path of this VFS entry. */
    private String m_sitePath;

    /**
     * Creates a new VFS entry bean.<p>
     * 
     * @param sitePath
     * @param isRoot
     */
    public CmsVfsEntryBean(String sitePath, boolean isRoot) {

        m_sitePath = sitePath;
        m_isRoot = isRoot;
    }

    /**
     * Hidden default constructor.<p>
     */
    protected CmsVfsEntryBean() {

        // do nothing 
    }

    /**
     * Gets the name which should be displayed in the widget representing this VFS entry.<p>
     * 
     * @return the name to display
     */
    public String getDisplayName() {

        if (m_isRoot) {
            return m_sitePath;
        } else {
            String fixedPath = m_sitePath.replaceFirst("/$", "");
            int lastSlash = fixedPath.lastIndexOf('/');
            if (lastSlash == -1) {
                return fixedPath;
            }
            return fixedPath.substring(lastSlash + 1);
        }
    }

    /**
     * Returns the site path of this VFS tree. 
     * 
     * @return the site path 
     */
    public String getSitePath() {

        return m_sitePath;
    }

    /**
     * Returns true if this entry is a top-level entry.<p>
     * 
     * @return true if this is a top-level entry 
     */
    public boolean isRoot() {

        return m_isRoot;
    }

}
