/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/loader/Attic/CmsLoaderManager.java,v $
 * Date   : $Date: 2003/07/18 12:44:46 $
 * Version: $Revision: 1.1 $
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

package org.opencms.loader;

import com.opencms.boot.I_CmsLogChannels;
import com.opencms.core.A_OpenCms;
import com.opencms.core.CmsException;
import com.opencms.launcher.I_CmsLauncher;

import java.util.Iterator;
import java.util.List;

/**
 * Collects all available resource loaders at startup and provides
 * a method for looking up the appropriate loader class for a
 * given loader id.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.1 $
 * @since 5.1
 */
public class CmsLoaderManager {

    private I_CmsLauncher[] m_loaders;

    /**
     * Collects all available resource loaders from the registry at startup.<p>
     * 
     * @param openCms the initialized OpenCms object
     * @throws CmsException if something goes wrong
     */
    public CmsLoaderManager(A_OpenCms openCms) throws CmsException {
        List loaders = A_OpenCms.getRegistry().getResourceLoaders();

        if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INIT)) {
            A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Launcher package     : " + this.getClass().getPackage());
        }

        m_loaders = new I_CmsLauncher[16];
        String loaderName = null;
        Iterator i = loaders.iterator();
        while (i.hasNext()) {
            try {
                loaderName = (String)i.next();
                I_CmsLauncher loaderInstance = (I_CmsLauncher)Class.forName(loaderName).newInstance();
                loaderInstance.setOpenCms(openCms);                
                addLoader(loaderInstance);
                if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_INIT)) {
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_INIT, ". Launcher loaded      : " + loaderName + " with id " + loaderInstance.getLauncherId());
                }                
            } catch (Throwable e) {
                // loader class not found, ignore class
                if (I_CmsLogChannels.C_LOGGING && A_OpenCms.isLogging(I_CmsLogChannels.C_OPENCMS_CRITICAL)) {
                    String errorMessage = "Error while initializing loader \"" + loaderName + "\". Ignoring.";
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[CmsLoaderManager] " + errorMessage);
                    A_OpenCms.log(I_CmsLogChannels.C_OPENCMS_CRITICAL, "[CmsLoaderManager] " + e);
                }
            }
        }
    }

    /**
     * Returns the loader class instance for the given loader id.<p>
     * 
     * @param launcherId the id of the launcher to return
     * @return the loader class instance for the given loader id
     */
    public I_CmsLauncher getLauncher(int launcherId) {
        return (I_CmsLauncher)m_loaders[launcherId];
    }

    /**
     * Clears all loader caches.<p>
     */
    public void clearCaches() {
        for (int i=0; i<m_loaders.length; i++) {
            I_CmsLauncher l = m_loaders[i];
            if (l != null) l.clearCache();
        }
    }

    /**
     * Adds a new loader to the internal list of loaded loaders.<p>
     *
     * @param loader the loader to add
     */
    private void addLoader(I_CmsLauncher loader) {
        int pos = loader.getLauncherId();
        if (pos > m_loaders.length) {
            I_CmsLauncher[] buffer = new I_CmsLauncher[pos * 2];
            System.arraycopy(m_loaders, 0, buffer, 0, m_loaders.length);
            m_loaders = buffer;
        }
        m_loaders[pos] = loader;
    }
}
