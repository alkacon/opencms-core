/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/configuration/CmsVfsConfiguration.java,v $
 * Date   : $Date: 2004/03/02 21:51:02 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2004 Alkacon Software (http://www.alkacon.com)
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

package org.opencms.configuration;

import org.opencms.file.I_CmsResourceType;
import org.opencms.loader.CmsLoaderManager;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.digester.Digester;

import org.dom4j.Element;

/**
 * VFS master configuration class.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.3
 */
public class CmsVfsConfiguration extends A_CmsXmlConfiguration implements I_CmsXmlConfiguration {
    
    /** The node name of an individual resource loader */
    protected static final String N_LOADER = "loader";
    
    /** The resource loaders node name */
    protected static final String N_RESOURCELOADERS = "resourceloaders";

    /** The resource loaders node name */
    protected static final String N_RESOURCETYPES = "resourcetypes";    

    /** The node name of an individual resource type */
    protected static final String N_TYPE = "type";    
    
    /** The man configuration node name */
    protected static final String N_VFS = "vfs";
    
    /** The configured loader manager */
    private CmsLoaderManager m_loaderManager;
    
    /** The configured resource types */
    private List m_resourceTypes; 
    
    /**
     * Public constructor, will be called by configuration manager.<p> 
     */
    public CmsVfsConfiguration() {
        // noop
    }
    
    /**
     * Adds a resource type to the list of configured resource types.<p>
     * 
     * @param resourceType the resource type to add
     */
    public void addResourceType(I_CmsResourceType resourceType) {
        m_resourceTypes.add(resourceType);
    }

    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#addXmlDigesterRules(org.apache.commons.digester.Digester)
     */
    public void addXmlDigesterRules(Digester digester) {        
        // add factory create method for "real" instance creation
        digester.addFactoryCreate("*/" + N_VFS, CmsVfsConfiguration.class);
        digester.addCallMethod("*/" + N_VFS, "initializeFinished");    
        
        // add rules for resource loaders
        digester.addObjectCreate("*/" + N_VFS + "/" + N_RESOURCELOADERS, CmsLoaderManager.class);
        digester.addCallMethod("*/" + N_VFS + "/" + N_RESOURCELOADERS + "/" + N_LOADER, "addLoader", 1);
        digester.addCallParam("*/" + N_VFS + "/" + N_RESOURCELOADERS + "/" + N_LOADER, 0, A_CLASS);        
        digester.addSetNext("*/" + N_VFS + "/" + N_RESOURCELOADERS, "setLoaderManager");
        
        // add rules for resource types
        digester.addObjectCreate("*/" + N_VFS + "/" + N_RESOURCETYPES + "/" + N_TYPE, null, A_CLASS);
        digester.addSetNext("*/" + N_VFS + "/" + N_RESOURCETYPES + "/" + N_TYPE, "addResourceType");
        
        // add this configuration object to the calling configuration after is has been processed
        digester.addSetNext("*/" + N_VFS, "addConfiguration");    
    }
    
    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#generateXml(org.dom4j.Element)
     */
    public Element generateXml(Element parent) {
        // generate vfs node and subnodes
        Element vfs = parent.addElement(N_VFS);
        
        Element resourceloadersElement = vfs.addElement(N_RESOURCELOADERS);
        Object[] loaders = m_loaderManager.getLoaders();
        for (int i=0; i<loaders.length; i++) {
            if (loaders[i] == null) {
                // not all positions might be occupied
                continue;
            }
            // add the loader node
            Element loader = resourceloadersElement.addElement(N_LOADER);
            loader.addAttribute(A_CLASS, loaders[i].getClass().getName());
        }
        
        Element resourcetypesElement = vfs.addElement(N_RESOURCETYPES);
        Iterator it = m_resourceTypes.iterator();
        while (it.hasNext()) {
            I_CmsResourceType resourceType = (I_CmsResourceType)it.next();
            resourcetypesElement.addElement(N_TYPE).addAttribute(A_CLASS, resourceType.getClass().getName());
        }
        
        // return the vfs node
        return vfs;
    }
    
    /**
     * Returns the initialized loader manager.<p>
     * 
     * @return the initialized loader manager
     */
    public CmsLoaderManager getLoaderManager() {
        return m_loaderManager;
    }
    
    /**
     * Returns the list of initialized resource types.<p>
     * 
     * @return the list of initialized resource types
     */ 
    public List getResourceTypes() {
        return m_resourceTypes;
    }
    
    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#initialize()
     */
    public void initialize() {
        m_resourceTypes = new ArrayList();
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". VFS configuration    : starting");
        }           
    }
    
    /**
     * Will be called when configuration of this object is finished.<p> 
     */
    public void initializeFinished() {
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". VFS configuration    : finished");
        }            
    }   
    
    /**
     * Sets the generated loader manager.<p>
     * 
     * @param manager the loader manager to set
     */
    public void setLoaderManager(CmsLoaderManager manager) {
        m_loaderManager = manager;
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". ResourceLoader init  : finished");
        }
    }
}
