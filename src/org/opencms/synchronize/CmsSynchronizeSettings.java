/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/synchronize/CmsSynchronizeSettings.java,v $
 * Date   : $Date: 2005/06/22 14:58:54 $
 * Version: $Revision: 1.11 $
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

import org.opencms.file.CmsObject;
import org.opencms.main.CmsException;
import org.opencms.main.CmsIllegalArgumentException;
import org.opencms.util.CmsStringUtil;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Contains the settings for the synchronization.<p>
 * 
 * @author Alexander Kandzior 
 * 
 * @version $Revision: 1.11 $ 
 * 
 * @since 6.0.0 
 */
public class CmsSynchronizeSettings implements Serializable {

    /** use well defined serialVersionUID to avoid issues with serialization. */
    private static final long serialVersionUID = 3713893787290111758L;

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
     * Performs a check if the values that have been set are valid.<p>
     * 
     * @param cms the current users OpenCms context
     * 
     * @throws CmsException in case the values are not valid
     */
    public void checkValues(CmsObject cms) throws CmsException {

        if (isEnabled() && (m_destinationPathInRfs == null)) {
            // if enabled, it's required to have RFS destination folder available
            throw new CmsSynchronizeException(Messages.get().container(Messages.ERR_NO_RFS_DESTINATION_0));
        }
        if (isEnabled() && ((m_sourceListInVfs == null) || (m_sourceListInVfs.size() == 0))) {
            // if enabled, it's required to have at last one source folder
            throw new CmsSynchronizeException(Messages.get().container(Messages.ERR_NO_VFS_SOURCE_0));
        }
        Iterator i = m_sourceListInVfs.iterator();
        // store the current site root
        String currentSite = cms.getRequestContext().getSiteRoot();
        // switch to root site
        cms.getRequestContext().setSiteRoot("");
        try {
            while (i.hasNext()) {
                // try to read all given resources, this will cause an error if the resource does not exist
                cms.readResource((String)i.next());
            }
        } finally {
            // reset to current site root
            cms.getRequestContext().setSiteRoot(currentSite);
        }
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

        String destination;
        if (CmsStringUtil.isEmptyOrWhitespaceOnly(destinationPathInRfs)) {
            destination = null;
        } else {
            destination = destinationPathInRfs.trim();
        }
        if (destination != null) {
            File destinationFolder = new File(destination);
            if (!destinationFolder.exists() || !destinationFolder.isDirectory()) {
                // destination folder does not exist
                throw new CmsIllegalArgumentException(Messages.get().container(
                    Messages.ERR_RFS_DESTINATION_NOT_THERE_1,
                    destination));
            }
            if (!destinationFolder.canWrite()) {
                // destination folder can't be written to
                throw new CmsIllegalArgumentException(Messages.get().container(
                    Messages.ERR_RFS_DESTINATION_NO_WRITE_1,
                    destination));
            }
            destination = destinationFolder.getAbsolutePath();
            if (destination.endsWith(File.separator)) {
                // ensure that the destination folder DOES NOT end with a file separator
                destination = destination.substring(0, destination.length() - 1);
            }
        }
        m_destinationPathInRfs = destination;
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
            m_sourceListInVfs = optimizeSourceList(sourceListInVfs);
        }
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {

        StringBuffer result = new StringBuffer();
        result.append("[");
        result.append(this.getClass().getName());
        result.append(", enabled: ");
        result.append(m_enabled);
        result.append(", RFS destination path: ");
        result.append(m_destinationPathInRfs);
        result.append(", VFS source path list: ");
        if (m_sourceListInVfs == null) {
            result.append(m_sourceListInVfs);
        } else {
            Iterator i = m_sourceListInVfs.iterator();
            while (i.hasNext()) {
                String path = (String)i.next();
                result.append(path);
                if (i.hasNext()) {
                    result.append(", ");
                }
            }
        }
        result.append("]");
        return result.toString();
    }

    /**
     * Optimizes the list of VFS source files by removing all resources that 
     * have a parent resource already included in the list.<p> 
     * 
     * @param sourceListInVfs the list of VFS resources to optimize
     * @return the optimized result list
     */
    protected List optimizeSourceList(List sourceListInVfs) {

        // input should be sorted but may be immutable
        List input = new ArrayList(sourceListInVfs);
        Collections.sort(input);

        List result = new ArrayList();
        Iterator i = input.iterator();
        while (i.hasNext()) {
            // check all sources in the list
            String sourceInVfs = (String)i.next();
            if (CmsStringUtil.isEmpty(sourceInVfs)) {
                // skip empty strings
                continue;
            }
            boolean found = false;
            for (int j = (result.size() - 1); j >= 0; j--) {
                // check if this source is indirectly contained because a parent folder is contained
                String check = (String)result.get(j);
                if (sourceInVfs.startsWith(check)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                // the source is not already contained in the result
                result.add(sourceInVfs);
            }
        }

        return result;
    }
}