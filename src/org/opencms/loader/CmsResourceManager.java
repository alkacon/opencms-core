/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/loader/CmsResourceManager.java,v $
 * Date   : $Date: 2004/06/28 07:47:32 $
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

package org.opencms.loader;

import org.opencms.configuration.CmsConfigurationException;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Collects all available resource loaders and resource types at startup and provides
 * methods to access them during OpenCms runtime.<p> 
 *
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * 
 * @version $Revision: 1.3 $
 * @since 5.1
 */
public class CmsResourceManager {

    /** The default mimetype. */
    private static final String C_DEFAULT_MIMETYPE = "text/html";

    /** Indicates if the configuration is finalized (frozen). */
    private boolean m_frozen;

    /** Contains all loader extensions to the include process. */
    private List m_includeExtensions;
    
    /** A list that contains all initialized resource loaders. */
    private List m_loaderList;

    /** All initialized resource loaders, mapped to their id. */
    private I_CmsResourceLoader[] m_loaders;

    /** The OpenCms map of configured mime types. */
    private Map m_mimeTypes;
    
    /** A list that contains all initialized resource types. */
    private List m_resourceTypeList;

    /** Hashtable with resource types. */
    private I_CmsResourceType[] m_resourceTypes;
    
    /**
     * Creates a new instance for the resource manager, 
     * will be called by the vfs configuration manager.<p>
     */
    public CmsResourceManager() {

        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Loader configuration : starting");
        }
        
        m_loaders = new I_CmsResourceLoader[16];
        m_resourceTypes = new I_CmsResourceType[100];
        m_resourceTypeList = new ArrayList();
        m_loaderList = new ArrayList();
        m_includeExtensions = new ArrayList();

        Properties mimeTypes = new Properties();
        try {
            // first try: read mime types from default package
            mimeTypes.load(getClass().getClassLoader().getResourceAsStream("mimetypes.properties"));
        } catch (Throwable t) {
            try {
                // second try: read mime types from loader package
                mimeTypes.load(getClass().getClassLoader().getResourceAsStream(
                    "org/opencms/loader/mimetypes.properties"));
            } catch (Throwable t2) {
                if (OpenCms.getLog(this).isErrorEnabled()) {
                    OpenCms.getLog(this).error("Could not read mimetypes from class path", t);
                }
            }
        }
        // initalize the Map with all available mimetypes
        m_mimeTypes = new HashMap(mimeTypes.size());
        Iterator i = mimeTypes.keySet().iterator();
        while (i.hasNext()) {
            // ensure all mime type entries are lower case
            String key = (String)i.next();
            String value = (String)mimeTypes.get(key);
            value = value.toLowerCase(Locale.ENGLISH);
            m_mimeTypes.put(key, value);
        }
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Found mime types     : " + m_mimeTypes.size() + " entrys");
        }
    }

    /**
     * Adds a new loader to the internal list of loaded loaders.<p>
     *
     * @param loader the loader to add
     * @throws CmsConfigurationException in case the resource manager configuration is already initialized
     */
    public void addLoader(I_CmsResourceLoader loader) throws CmsConfigurationException {

        // check if new loaders can still be added
        if (m_frozen) {
            throw new CmsConfigurationException("Resource manager configuration only possibule during system startup!");
        }
        
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
            m_includeExtensions.add(loader);
        }
        m_loaderList.add(loader);
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(
                ". Loader init          : Adding " + loader.getClass().getName() + " with id " + pos);
        }
    }

    /**
     * Adds a new resource type to the internal list of loaded resource types.<p>
     *
     * @param resourceType the resource type to add
     * @throws CmsConfigurationException in case the resource manager configuration is already initialized
     */
    public void addResourceType(I_CmsResourceType resourceType) throws CmsConfigurationException {

        // check if new resource types can still be added
        if (m_frozen) {
            throw new CmsConfigurationException("Resource manager configuration only possibule during system startup!");
        }
        
        // add the loader to the internal list of loaders
        int pos = resourceType.getTypeId();
        if (pos > m_resourceTypes.length) {
            I_CmsResourceType[] buffer = new I_CmsResourceType[pos * 2];
            System.arraycopy(m_resourceTypes, 0, buffer, 0, m_resourceTypes.length);
            m_resourceTypes = buffer;
        }
        m_resourceTypes[pos] = resourceType;
        m_resourceTypeList.add(resourceType);
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(
                ". Resource type init   : Adding " + resourceType.getClass().getName() + " with id " + pos);
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
     * Returns the (unmodifyable array) list with all initialized resource loaders.<p>
     * 
     * @return the (unmodifyable array) list with all initialized resource loaders
     */    
    public List getLoaders() {

        return m_loaderList;
    }

    /**
     * Returns the mime type for a specified file name.<p>
     * 
     * If an encoding parameter that is not <code>null</code> is provided,
     * the returned mime type is extended with a <code>; charset={encoding}</code> setting.<p> 
     * 
     * @param filename the file name to check the mime type for
     * @param encoding the default encoding (charset) in case of mime types is of type "text"
     * @return the mime type for a specified file
     */
    public String getMimeType(String filename, String encoding) {

        String mimetype = null;
        int lastDot = filename.lastIndexOf('.');
        // check the mime type for the file extension 
        if ((lastDot > 0) && (lastDot < (filename.length() - 1))) {
            mimetype = (String)m_mimeTypes.get(filename.substring(lastDot + 1));
        }
        if (mimetype == null) {
            mimetype = C_DEFAULT_MIMETYPE;
        }
        StringBuffer result = new StringBuffer(mimetype);
        if ((encoding != null) && mimetype.startsWith("text") && (mimetype.indexOf("charset") == -1)) {
            result.append("; charset=");
            result.append(encoding);
        }
        return result.toString();
    }

    /**
     * Returns the initialized resource type instance for the given id.<p>
     * 
     * @param typeId the id of the resource type to get
     * @return the initialized resource type instance for the given id
     * @throws CmsLoaderException if no resource type is vaialble for the given id
     */
    public I_CmsResourceType getResourceType(int typeId) throws CmsLoaderException {

        I_CmsResourceType result = null;
        if (typeId < m_resourceTypes.length) {
            result = m_resourceTypes[typeId];
        }
        if (result == null) {
            throw new CmsLoaderException(
                "Unknown resource type id requested: " + typeId, 
                CmsLoaderException.C_LOADER_UNKNOWN_RESOURCE_TYPE);            
        }
        return result;
    }

    /**
     * Returns the initialized resource type instance for the given resource type name.<p>
     * 
     * @param typeName the name of the resource type to get
     * @return the initialized resource type instance for the given name
     * @throws CmsLoaderException if no resource type is vaialble for the given name
     */
    public I_CmsResourceType getResourceType(String typeName) throws CmsLoaderException {

        for (int i = 0; i < m_resourceTypeList.size(); i++) {
            I_CmsResourceType type = (I_CmsResourceType)m_resourceTypeList.get(i);
            if (type.getTypeName().equals(typeName)) {
                return type;
            }
        }
        throw new CmsLoaderException(
            "Unknown resource type name requested: " + typeName, 
            CmsLoaderException.C_LOADER_UNKNOWN_RESOURCE_TYPE);
    }
    
    /**
     * Returns the (unmodifyable array) list with all initialized resource types.<p>
     * 
     * @return the (unmodifyable array) list with all initialized resource types
     */    
    public List getResourceTypes() {
        
        // return the list of resource types
        return m_resourceTypeList;
    }

    /**
     * Returns a template loader facade for the given file.<p>
     * @param cms the current cms context
     * @param resource the requested file
     * @param templateProperty the property to read for the template
     * 
     * @return a resource loader facade for the given file
     * @throws CmsException if something goes wrong
     */
    public CmsTemplateLoaderFacade getTemplateLoaderFacade(
        CmsObject cms, 
        CmsResource resource, 
        String templateProperty
    ) throws CmsException {

        String absolutePath = cms.getSitePath(resource);

        String templateProp = cms.readPropertyObject(absolutePath, templateProperty, true).getValue();

        if (templateProp == null) {
            // no template property defined, this is a must for facade loaders
            throw new CmsLoaderException("Property '" + templateProperty + "' undefined for file " + absolutePath);
        }

        CmsResource template = cms.readFile(templateProp, CmsResourceFilter.IGNORE_EXPIRATION);
        return new CmsTemplateLoaderFacade(getLoader(template.getLoaderId()), resource, template);
    }
    
    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() {
        
        // freeze the current configuration
        m_frozen = true;
        m_resourceTypeList = Collections.unmodifiableList(m_resourceTypeList);
        m_loaderList = Collections.unmodifiableList(m_loaderList);
        
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Loader configuration : finished");
        }  
    }       

    /**    
     * Loads the requested resource and writes the contents to the response stream.<p>
     * 
     * @param req the current http request
     * @param res the current http response
     * @param cms the curren cms context
     * @param resource the requested resource
     * @throws ServletException if something goes wrong
     * @throws IOException if something goes wrong
     * @throws CmsException if something goes wrong
     */
    public void loadResource(
        CmsObject cms, 
        CmsResource resource, 
        HttpServletRequest req, 
        HttpServletResponse res
    ) throws ServletException, IOException, CmsException {

        res.setContentType(getMimeType(resource.getName(), cms.getRequestContext().getEncoding()));
        I_CmsResourceLoader loader = getLoader(resource.getLoaderId());
        loader.load(cms, resource, req, res);
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
    public String resolveIncludeExtensions(
        String target,
        String element,
        boolean editable,
        Map paramMap,
        ServletRequest req,
        ServletResponse res
    ) throws CmsException {

        if (m_includeExtensions == null) {
            return target;
        }
        String result = target;
        for (int i=0; i<m_includeExtensions.size(); i++) {
            // offer the element to every include extension
            I_CmsLoaderIncludeExtension loader = (I_CmsLoaderIncludeExtension)m_includeExtensions.get(i);
            result = loader.includeExtension(target, element, editable, paramMap, req, res);
        }
        return result;
    }
}