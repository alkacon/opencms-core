/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/configuration/CmsVfsConfiguration.java,v $
 * Date   : $Date: 2004/03/08 07:29:48 $
 * Version: $Revision: 1.4 $
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
import org.opencms.loader.I_CmsResourceLoader;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsResourceTranslator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.digester.Digester;

import org.dom4j.Element;

/**
 * VFS master configuration class.<p>
 * 
 * @author Alexander Kandzior (a.kandzior@alkacon.com)
 * @since 5.3
 */
public class CmsVfsConfiguration extends A_CmsXmlConfiguration implements I_CmsXmlConfiguration {
    
    /** File translationd node name */
    protected static final String N_FILETRANSLATIONS = "filetranslations";

    /** Folder translations node name */
    protected static final String N_FOLDERTRANSLATIONS = "foldertranslations";
    
    /** The node name of an individual resource loader */
    protected static final String N_LOADER = "loader";
    
    /** The resource loaders node name */
    protected static final String N_RESOURCELOADERS = "resourceloaders";

    /** The resource loaders node name */
    protected static final String N_RESOURCETYPES = "resourcetypes";    
    
    /** Individual translation node name */
    protected static final String N_TRANSLATION = "translation";
    
    /** The translations master node name */
    protected static final String N_TRANSLATIONS = "translations";

    /** The node name of an individual resource type */
    protected static final String N_TYPE = "type";       
    
    /** The man configuration node name */
    protected static final String N_VFS = "vfs";
    
    /** Controls if file translation is enabled */
    private boolean m_fileTranslationEnabled;
    
    /** The list of file translation */
    private List m_fileTranslations;
    
    /** Controls if folder translation is enabled */
    private boolean m_folderTranslationEnabled;
    
    /** The list of folder translations */
    private List m_folderTranslations;
    
    /** The configured loader manager */
    private CmsLoaderManager m_loaderManager;
    
    /** The configured resource types */
    private List m_resourceTypes; 
    
    /**
     * Public constructor, will be called by configuration manager.<p> 
     */
    public CmsVfsConfiguration() {
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Empty constructor called on " + this);
        }     
    }
    
    /**
     * Adds one file translation rule.<p>
     * 
     * @param translation the file translation rule to add
     */
    public void addFileTranslation(String translation) {
        m_fileTranslations.add(translation);
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". File translation     : adding rule [" + translation + "]");
        }          
    }
    
    /**
     * Adds one foler translation rule.<p>
     * 
     * @param translation the folder translation rule to add
     */
    public void addFolderTranslation(String translation) {
        m_folderTranslations.add(translation);
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Folder translation   : adding rule [" + translation + "]");
        }                
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
        
        // add this configuration object to the calling configuration after is has been processed
        digester.addSetNext("*/" + N_VFS, "addConfiguration"); 
        
        // creation of the loader manager
        digester.addObjectCreate("*/" + N_VFS + "/" + N_RESOURCELOADERS, CmsLoaderManager.class);
        // add rules for resource loaders
        digester.addObjectCreate("*/" + N_VFS + "/" + N_RESOURCELOADERS + "/" + N_LOADER, A_CLASS, CmsConfigurationException.class);
        digester.addCallMethod("*/" + N_VFS + "/" + N_RESOURCELOADERS + "/" + N_LOADER, "initialize");
        digester.addSetNext("*/" + N_VFS + "/" + N_RESOURCELOADERS + "/" + N_LOADER, "addLoader");  
        // loader manager finished
        digester.addSetNext("*/" + N_VFS + "/" + N_RESOURCELOADERS, "setLoaderManager");
        
        // add rules for resource types
        digester.addObjectCreate("*/" + N_VFS + "/" + N_RESOURCETYPES + "/" + N_TYPE, null, A_CLASS);
        digester.addSetNext("*/" + N_VFS + "/" + N_RESOURCETYPES + "/" + N_TYPE, "addResourceType");
        
        // add rules for file translations
        digester.addCallMethod("*/" + N_VFS + "/" + N_TRANSLATIONS + "/" + N_FILETRANSLATIONS + "/" + N_TRANSLATION, "addFileTranslation", 0);
        digester.addCallMethod("*/" + N_VFS + "/" + N_TRANSLATIONS + "/" + N_FILETRANSLATIONS, "setFileTranslationEnabled", 1);
        digester.addCallParam("*/" + N_VFS + "/" + N_TRANSLATIONS + "/" + N_FILETRANSLATIONS, 0, A_ENABLED);        
        
        // add rules for file translations
        digester.addCallMethod("*/" + N_VFS + "/" + N_TRANSLATIONS + "/" + N_FOLDERTRANSLATIONS + "/" + N_TRANSLATION, "addFolderTranslation", 0);
        digester.addCallMethod("*/" + N_VFS + "/" + N_TRANSLATIONS + "/" + N_FOLDERTRANSLATIONS, "setFolderTranslationEnabled", 1);
        digester.addCallParam("*/" + N_VFS + "/" + N_TRANSLATIONS + "/" + N_FOLDERTRANSLATIONS, 0, A_ENABLED);        
    }
    
    /**
     * @see org.opencms.configuration.I_CmsXmlConfiguration#generateXml(org.dom4j.Element)
     */
    public Element generateXml(Element parent) {
        // generate vfs node and subnodes
        Element vfs = parent.addElement(N_VFS);
        
        // add resource loader
        Element resourceloadersElement = vfs.addElement(N_RESOURCELOADERS);
        Object[] loaders = m_loaderManager.getLoaders();
        for (int i=0; i<loaders.length; i++) {
            if (loaders[i] == null) {
                // not all positions might be occupied
                continue;
            }
            I_CmsResourceLoader loader = (I_CmsResourceLoader)loaders[i];            
            // add the loader node
            Element loaderNode = resourceloadersElement.addElement(N_LOADER);
            loaderNode.addAttribute(A_CLASS, loader.getClass().getName());
            ExtendedProperties loaderConfiguratrion = loader.getConfiguration();
            if (loaderConfiguratrion != null) {
                Iterator it = loaderConfiguratrion.getKeys();
                while (it.hasNext()) {
                    String name = (String)it.next();
                    String value = loaderConfiguratrion.get(name).toString();
                    Element paramNode = loaderNode.addElement(N_PARAM);
                    paramNode.addAttribute(A_NAME, name);
                    paramNode.addText(value);
                }
            }
        }
        
        // add resource types
        Element resourcetypesElement = vfs.addElement(N_RESOURCETYPES);
        Iterator it = m_resourceTypes.iterator();
        while (it.hasNext()) {
            I_CmsResourceType resourceType = (I_CmsResourceType)it.next();
            resourcetypesElement.addElement(N_TYPE).addAttribute(A_CLASS, resourceType.getClass().getName());
        }
        
        // add translation rules
        Element translationsElement = vfs.addElement(N_TRANSLATIONS);
        
        // file translation rules
        Element fileTransElement = 
            translationsElement.addElement(N_FILETRANSLATIONS)
                .addAttribute(A_ENABLED, new Boolean(m_fileTranslationEnabled).toString());        
        it = m_fileTranslations.iterator();
        while (it.hasNext()) {
            fileTransElement.addElement(N_TRANSLATION).setText(it.next().toString());
        }
        
        // folder translation rules
        Element folderTransElement = 
            translationsElement.addElement(N_FOLDERTRANSLATIONS)
                .addAttribute(A_ENABLED, new Boolean(m_folderTranslationEnabled).toString());        
        it = m_folderTranslations.iterator();
        while (it.hasNext()) {
            folderTransElement.addElement(N_TRANSLATION).setText(it.next().toString());
        }               
        
        // return the vfs node
        return vfs;
    }

    /**
     * Returns the file resource translator that has been initialized
     * with the configured file translation rules.<p>
     * 
     * @return the file resource translator 
     */
    public CmsResourceTranslator getFileTranslator() {
        String[] array = m_fileTranslationEnabled?new String[m_fileTranslations.size()]:new String[0];
        for (int i = 0; i < m_fileTranslations.size(); i++) {
            array[i] = (String)m_fileTranslations.get(i);
        }
        return new CmsResourceTranslator(array, true);
    }
    
    /**
     * Returns the folder resource translator that has been initialized
     * with the configured folder translation rules.<p>
     * 
     * @return the folder resource translator 
     */
    public CmsResourceTranslator getFolderTranslator() {
        String[] array = m_folderTranslationEnabled?new String[m_folderTranslations.size()]:new String[0];
        for (int i = 0; i < m_folderTranslations.size(); i++) {
            array[i] = (String)m_folderTranslations.get(i);
        }
        return new CmsResourceTranslator(array, false);
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
        m_fileTranslations = new ArrayList();
        m_folderTranslations = new ArrayList();
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
     * Enables or disables the file translation rules.<p>
     * 
     * @param value if "true", file translation is enabled, otherwise it is disabled
     */
    public void setFileTranslationEnabled(String value) {
        m_fileTranslationEnabled = Boolean.valueOf(value).booleanValue();     
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". File translation     : " + (m_fileTranslationEnabled ? "enabled" : "disabled"));
        }          
    }
        
    /**
     * Enables or disables the folder translation rules.<p>
     * 
     * @param value if "true", folder translation is enabled, otherwise it is disabled
     */
    public void setFolderTranslationEnabled(String value) {
        m_folderTranslationEnabled = Boolean.valueOf(value).booleanValue();
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Folder translation   : " + (m_folderTranslationEnabled ? "enabled" : "disabled"));
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
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Loader configuration : finished");
        }
    }
}
