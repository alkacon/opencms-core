/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/loader/Attic/CmsLoaderManager.java,v $
 * Date   : $Date: 2003/12/12 12:16:42 $
 * Version: $Revision: 1.13 $
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

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import com.opencms.core.CmsException;
import com.opencms.core.I_CmsConstants;
import com.opencms.file.CmsFile;
import com.opencms.file.CmsObject;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.ExtendedProperties;

/**
 * Collects all available resource loaders at startup and provides
 * a method for looking up the appropriate loader class for a
 * given loader id.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.13 $
 * @since 5.1
 */
public class CmsLoaderManager {

    private I_CmsResourceLoader[] m_loaders;

    /**
     * Collects all available resource loaders from the registry at startup.<p>
     * 
     * @param configuration the OpenCms configuration 
     */
    public CmsLoaderManager(ExtendedProperties configuration) {
        List loaders = OpenCms.getRegistry().getResourceLoaders();

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". ResourceLoader init  : " + this.getClass().getPackage());
        }

        m_loaders = new I_CmsResourceLoader[16];
        String loaderName = null;
        Iterator i = loaders.iterator();
        while (i.hasNext()) {
            try {
                loaderName = (String)i.next();
                I_CmsResourceLoader loaderInstance = (I_CmsResourceLoader)Class.forName(loaderName).newInstance();
                loaderInstance.init(configuration);                
                addLoader(loaderInstance);
                if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                    OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". ResourceLoader loaded: " + loaderName + " with id " + loaderInstance.getLoaderId());
                }                
            } catch (Throwable e) {
                // loader class not found, ignore class
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    String errorMessage = "Error while initializing loader \"" + loaderName + "\". Ignoring.";
                    OpenCms.getLog(this).error(errorMessage, e);
                }
            }
        }
    }

    /**
     * Returns the loader class instance for the given loader id.<p>
     * 
     * @param id the id of the loader to return
     * @return the loader class instance for the given loader id
     */
    public I_CmsResourceLoader getLoader(int id) {
        return m_loaders[id];
    }

    /**
     * Returns a resource loader facade for the given file.<p>
     * 
     * @param cms the current cms context
     * @param file the requested file
     * @return a resource loader facade for the given file
     * @throws CmsException if something goes wrong
     */
    public CmsResourceLoaderFacade getLoaderFacade(CmsObject cms, CmsFile file) throws CmsException {        
        String absolutePath = cms.readAbsolutePath(file);        
        String templateProp = cms.readProperty(absolutePath, I_CmsConstants.C_PROPERTY_TEMPLATE);       

        if (templateProp == null) {
            // no template property defined, throw exception
            throw new CmsException("Property '" + I_CmsConstants.C_PROPERTY_TEMPLATE + "' undefined for page file " + absolutePath);
        }        

        CmsFile templateFile = cms.readFile(templateProp);
        
        if (templateFile.getLoaderId() == CmsJspLoader.C_RESOURCE_LOADER_ID) {
            return new CmsResourceLoaderFacade(getLoader(CmsJspLoader.C_RESOURCE_LOADER_ID), templateFile);
        } else {
            return new CmsResourceLoaderFacade(getLoader(CmsXmlTemplateLoader.C_RESOURCE_LOADER_ID), file);
        }
    }
    
    /**
     * Adds a new loader to the internal list of loaded loaders.<p>
     *
     * @param loader the loader to add
     */
    private void addLoader(I_CmsResourceLoader loader) {
        int pos = loader.getLoaderId();
        if (pos > m_loaders.length) {
            I_CmsResourceLoader[] buffer = new I_CmsResourceLoader[pos * 2];
            System.arraycopy(m_loaders, 0, buffer, 0, m_loaders.length);
            m_loaders = buffer;
        }
        m_loaders[pos] = loader;
    }
}
