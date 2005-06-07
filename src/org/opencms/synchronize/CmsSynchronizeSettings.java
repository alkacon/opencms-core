/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/synchronize/CmsSynchronizeSettings.java,v $
 * Date   : $Date: 2005/06/07 16:14:31 $
 * Version: $Revision: 1.7 $
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

package org.opencms.synchronize;

import org.opencms.util.CmsStringUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains the settings for the synchronization.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.7 $
 * @since 5.3
 */
public class CmsSynchronizeSettings implements Serializable {

    /** The destination path of the synchronization in the "real" file system. */
    private String m_destinationPathInRfs;

    /** Indicates if the synchronization is enabled or not. */
    private boolean m_enabled;

    /** The source path list of the synchronization in the OpenCms VFS. */
    private List m_sourceListInVfs;

    /**
     * Empty constructor, called from the configuration.<p>
     */
    public CmsSynchronizeSettings() {

        m_sourceListInVfs = new ArrayList();
    }

    /**
     * Returns the destination path of the synchronization in the "real" file system.<p>
     * 
     * @return the destination path of the synchronization in the "real" file system
     */
    public String getDestinationPathInRfs() {

        return m_destinationPathInRfs;
    }

    /**
     * Returns the source path list of the synchronization in the OpenCms VFS.<p>
     * 
     * The objects in the list are of type <code>{@link String}</code>.
     * 
     * @return the source path list of the synchronization in the OpenCms VFS
     */
    public List getSourceListInVfs() {

        return m_sourceListInVfs;
    }

    /**
     * Returns the enabled flag which indicates if this synchronize settings are enabled or not.<p>
     *
     * @return the enabled flag
     */
    public boolean isEnabled() {

        return m_enabled;
    }

    /**
     * Returns <code>true</code> if the synchonization is enabled.<p>
     * 
     * The synchonization is enabled if both source and destination 
     * path is set, and also the enabled flag is true.<p>
     * 
     * @return <code>true</code> if the synchonization is enabled
     */
    public boolean isSyncEnabled() {

        return isEnabled() && (m_sourceListInVfs != null) && (m_destinationPathInRfs != null);
    }

    /**
     * Sets the destination path of the synchronization in the "real" file system.<p>
     * 
     * @param destinationPathInRfs the destination path of the synchronization in the "real" file system to set
     */
    public void setDestinationPathInRfs(String destinationPathInRfs) {

        if (CmsStringUtil.isEmptyOrWhitespaceOnly(destinationPathInRfs)) {
            m_destinationPathInRfs = null;
        } else {
            m_destinationPathInRfs = destinationPathInRfs.trim();
        }
    }

    /**
     * Sets the enabled flag which indicates if this synchronize settings are enabled or not.<p>
     *
     * @param enabled the enabled flag to set
     */
    public void setEnabled(boolean enabled) {

        m_enabled = enabled;
    }

    /**
     * Sets the source path list of the synchronization in the OpenCms VFS.<p>
     * 
     * The objects in the list must be of type <code>{@link String}</code>.
     * 
     * @param sourceListInVfs the source path list of the synchronization in the OpenCms VFS to set
     */
    public void setSourceListInVfs(List sourceListInVfs) {

        if (sourceListInVfs == null) {
            m_sourceListInVfs = new ArrayList();
        } else {
            m_sourceListInVfs = sourceListInVfs;
        }
    }
}