/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/synchronize/CmsSynchronizeSettings.java,v $
 * Date   : $Date: 2004/06/14 15:50:09 $
 * Version: $Revision: 1.3 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2003 Alkacon Software (http://www.alkacon.com)
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

import org.opencms.util.CmsStringSubstitution;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the settings for the synchronization.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.3 $
 * @since 5.3
 */
public class CmsSynchronizeSettings {
    
    /** The destination path of the synchronization in the "real" file system. */
    private String m_destinationPathInRfs;
    
    /** The source path of the synchronization in the OpenCms VFS. */
    private String m_sourcePathInVfs;
    
    /** The list of instanciated synchronize modification classes. */
    private List m_synchronizeModifications;
    
    /**
     * Empty constructor, called from the configuration.<p>
     */
    public CmsSynchronizeSettings() {
        m_synchronizeModifications = new ArrayList();
    }
    
    /**
     * Returns <code>true</code> if the synchonization is enabled.<p>
     * 
     * The synchonization is enabled if both source and destination 
     * path is set.<p>
     * 
     * @return <code>true</code> if the synchonization is enabled
     */
    public boolean isSyncEnabled() {
        return (m_sourcePathInVfs != null) && (m_destinationPathInRfs != null);
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
     * Returns the source path of the synchronization in the OpenCms VFS.<p>
     * 
     * @return the source path of the synchronization in the OpenCms VFS
     */
    public String getSourcePathInVfs() {
        return m_sourcePathInVfs;
    }
    
    /**
     * Returns the list of instanciated synchronize modification classes.<p>
     * 
     * @return the list of instanciated synchronize modification classes
     */
    public List getSynchronizeModifications() {
        return m_synchronizeModifications;
    }
    
    /**
     * Sets the destination path of the synchronization in the "real" file system.<p>
     * 
     * @param destinationPathInRfs the destination path of the synchronization in the "real" file system to set
     */
    public void setDestinationPathInRfs(String destinationPathInRfs) {
        m_destinationPathInRfs = destinationPathInRfs;
        if (CmsStringSubstitution.isEmpty(m_destinationPathInRfs)) {
            m_destinationPathInRfs = null;
        }
    }
    
    /**
     * Sets the source path of the synchronization in the OpenCms VFS.<p>
     * 
     * @param sourcePathInVfs the source path of the synchronization in the OpenCms VFS to set
     */
    public void setSourcePathInVfs(String sourcePathInVfs) {
        m_sourcePathInVfs = sourcePathInVfs;
        if (CmsStringSubstitution.isEmpty(m_sourcePathInVfs)) {
            m_sourcePathInVfs = null;
        }        
    }
}
