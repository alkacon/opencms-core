/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/loader/Attic/CmsLoaderManager.java,v $
 * Date   : $Date: 2004/03/02 21:51:02 $
 * Version: $Revision: 1.18 $
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

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.OpenCms;
import org.opencms.main.OpenCmsCore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Collects all available resource loaders at startup and provides
 * a method for looking up the appropriate loader class for a
 * given loader id.<p>
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.18 $
 * @since 5.1
 */
public class CmsLoaderManager {
    
    /** Contains all loader extensions to the include process */
    private List m_includeExtensions;

    /** All initialized resource loaders, mapped to their ID */
    private I_CmsResourceLoader[] m_loaders;

    /**
     * Creates a new instance for the loader manager, will be called by the vfs configuration manager.<p>
     * 
     * @see org.opencms.configuration.CmsVfsConfiguration#addXmlDigesterRules(org.apache.commons.digester.Digester)
     */
    public CmsLoaderManager() {
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". ResourceLoader init  : starting");
        }
        m_loaders = new I_CmsResourceLoader[16];
    }
    
    /**
     * Adds a resource loader from the XML configuration.<p>
     * 
     * @param clazz the class name of the resource loader to add
     */
    public void addLoader(String clazz) {
        try {
            I_CmsResourceLoader loaderInstance = (I_CmsResourceLoader)Class.forName(clazz).newInstance();
            // HACK: I need this for loader init as long as loaders still need properties
            loaderInstance.init(OpenCmsCore.getInstance().getConfiguration());                
            addLoader(loaderInstance);
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". ResourceLoader loaded: " + clazz + " with id " + loaderInstance.getLoaderId());
            }                
        } catch (Throwable e) {
            // loader class not found, ignore class
            if (OpenCms.getLog(this).isErrorEnabled()) {
                String errorMessage = "Error while initializing loader \"" + clazz + "\". Ignoring.";
                OpenCms.getLog(this).error(errorMessage, e);
            }
        }        
    }

//    /**
//     * Collects all available resource loaders from the registry at startup.<p>
//     * 
//     * @param configuration the OpenCms configuration 
//     */
//    public CmsLoaderManager(ExtendedProperties configuration) {
//        List loaders = OpenCms.getRegistry().getResourceLoaders();
//
//        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
//            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". ResourceLoader init  : " + this.getClass().getPackage());
//        }
//
//        m_loaders = new I_CmsResourceLoader[16];
//        String loaderName = null;
//        Iterator i = loaders.iterator();
//        while (i.hasNext()) {
//            try {
//                loaderName = (String)i.next();
//                I_CmsResourceLoader loaderInstance = (I_CmsResourceLoader)Class.forName(loaderName).newInstance();
//                loaderInstance.init(configuration);                
//                addLoader(loaderInstance);
//                if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
//                    OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". ResourceLoader loaded: " + loaderName + " with id " + loaderInstance.getLoaderId());
//                }                
//            } catch (Throwable e) {
//                // loader class not found, ignore class
//                if (OpenCms.getLog(this).isErrorEnabled()) {
//                    String errorMessage = "Error while initializing loader \"" + loaderName + "\". Ignoring.";
//                    OpenCms.getLog(this).error(errorMessage, e);
//                }
//            }
//        }
//    }

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
     * Returns all configured loader instances.<p>
     * 
     * @return all configured loader instances
     */
    public I_CmsResourceLoader[] getLoaders() {
        // ensure only a clone is returned so that the original array can not be modified
        return (I_CmsResourceLoader[])m_loaders.clone();
    }
    
    /**
     * Returns a template loader facade for the given file.<p>
     * 
     * @param cms the current cms context
     * @param resource the requested file
     * @return a resource loader facade for the given file
     * @throws CmsException if something goes wrong
     */
    public CmsTemplateLoaderFacade getTemplateLoaderFacade(CmsObject cms, CmsResource resource) throws CmsException {        
        String absolutePath = cms.readAbsolutePath(resource);        
        String templateProp = cms.readProperty(absolutePath, I_CmsConstants.C_PROPERTY_TEMPLATE);       

        if (templateProp == null) {
            // no template property defined, this is a must for facade loaders
            throw new CmsLoaderException("Property '" + I_CmsConstants.C_PROPERTY_TEMPLATE + "' undefined for file " + absolutePath);
        }        

        CmsResource template = cms.readFile(templateProp);
        return new CmsTemplateLoaderFacade(getLoader(template.getLoaderId()), resource, template);
    }
    
    /**
     * Extension method for handling special, loader depended actions during the include process.<p>
     * 
     * Note: If you have multiple loaders configured that require include extensions, 
     * all loaders are called in the order they are configured in.<p> 
     * 
     * @param target the target for the include, might be <code>null</code>
     * @param element the element to select form the target might be <code>null</code>
     * @param editable the flag to indicate if the target is editable
     * @param paramMap a map of parameters for the include, can be modified, might be <code>null</code>
     * @param req the current request
     * @param res the current response
     * @throws CmsException in case something goes wrong
     * @return the modified target URI
     */
    public String resolveIncludeExtensions(String target, String element, boolean editable, Map paramMap, ServletRequest req, ServletResponse res) throws CmsException {
        if (m_includeExtensions == null) {
            return target;
        }
        String result = target;
        Iterator i = m_includeExtensions.iterator();
        while (i.hasNext()) {
            // offer the element to every include extension
            I_CmsLoaderIncludeExtension loader = (I_CmsLoaderIncludeExtension)i.next();
            result = loader.includeExtension(target, element, editable, paramMap, req, res);
        }
        return result;
    }
    
    /**
     * Adds a new loader to the internal list of loaded loaders.<p>
     *
     * @param loader the loader to add
     */
    private void addLoader(I_CmsResourceLoader loader) {
        // add the loader to the internal list of loaders
        int pos = loader.getLoaderId();
        if (pos > m_loaders.length) {
            I_CmsResourceLoader[] buffer = new I_CmsResourceLoader[pos * 2];
            System.arraycopy(m_loaders, 0, buffer, 0, m_loaders.length);
            m_loaders = buffer;
        }
        m_loaders[pos] = loader;
        if (loader instanceof I_CmsLoaderIncludeExtension) {
            // this loader requires special processing during the include process
            if (m_includeExtensions == null) {
                m_includeExtensions = new ArrayList();
            }
            m_includeExtensions.add(loader);
        }
    }
}
