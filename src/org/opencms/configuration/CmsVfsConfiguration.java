/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/configuration/CmsVfsConfiguration.java,v $
 * Date   : $Date: 2004/06/21 11:43:07 $
 * Version: $Revision: 1.10 $
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

import org.opencms.loader.CmsResourceManager;
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
    
    /** The name of the DTD for this configuration. */
    private static final String C_CONFIGURATION_DTD_NAME = "opencms-vfs.dtd";
    
    /** The name of the default XML file for this configuration. */
    private static final String C_DEFAULT_XML_FILE_NAME = "opencms-vfs.xml";   
    
    /** File translations node name. */
    protected static final String N_FILETRANSLATIONS = "filetranslations";

    /** Folder translations node name. */
    protected static final String N_FOLDERTRANSLATIONS = "foldertranslations";
    
    /** The node name of an individual resource loader. */
    protected static final String N_LOADER = "loader";
    
    /** The main resource node name. */
    protected static final String N_RESOURCES = "resources";
    
    /** The resource loaders node name. */
    protected static final String N_RESOURCELOADERS = "resourceloaders";

    /** The resource types node name. */
    protected static final String N_RESOURCETYPES = "resourcetypes";    
    
    /** Individual translation node name. */
    protected static final String N_TRANSLATION = "translation";
    
    /** The translations master node name. */
    protected static final String N_TRANSLATIONS = "translations";

    /** The node name of an individual resource type. */
    protected static final String N_TYPE = "type";       
    
    /** The node name for the version history. */
    protected static final String N_VERSIONHISTORY = "versionhistory";
    
    /** The main vfs configuration node name. */
    protected static final String N_VFS = "vfs";
    
    /** Controls if file translation is enabled. */
    private boolean m_fileTranslationEnabled;
    
    /** The list of file translations. */
    private List m_fileTranslations;
    
    /** Controls if folder translation is enabled. */
    private boolean m_folderTranslationEnabled;
    
    /** The list of folder translations. */
    private List m_folderTranslations;
    
    /** The configured resource manager. */
    private CmsResourceManager m_resourceManager;
    
    /**
     * Public constructor, will be called by configuration manager.<p> 
     */
    public CmsVfsConfiguration() {
        setXmlFileName(C_DEFAULT_XML_FILE_NAME);
        m_fileTranslations = new ArrayList();
        m_folderTranslations = new ArrayList();
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". VFS configuration    : initialized");
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
     * @see org.opencms.configuration.I_CmsXmlConfiguration#addXmlDigesterRules(org.apache.commons.digester.Digester)
     */
    public void addXmlDigesterRules(Digester digester) {        
        // add finish rule
        digester.addCallMethod("*/" + N_VFS, "initializeFinished");    
        
        // creation of the resource manager
        digester.addObjectCreate("*/" + N_VFS + "/" + N_RESOURCES, CmsResourceManager.class);
        digester.addCallMethod("*/" + N_VFS + "/" + N_RESOURCES, I_CmsConfigurationParameterHandler.C_INIT_CONFIGURATION_METHOD);
        digester.addSetNext("*/" + N_VFS + "/" + N_RESOURCES, "setResourceManager");

        // add rules for resource loaders
        digester.addObjectCreate("*/" + N_VFS + "/" + N_RESOURCES + "/" + N_RESOURCELOADERS + "/" + N_LOADER, A_CLASS, CmsConfigurationException.class);
        digester.addCallMethod("*/" + N_VFS + "/" + N_RESOURCES + "/" + N_RESOURCELOADERS + "/" + N_LOADER, I_CmsConfigurationParameterHandler.C_INIT_CONFIGURATION_METHOD);
        digester.addSetNext("*/" + N_VFS + "/" + N_RESOURCES + "/" + N_RESOURCELOADERS + "/" + N_LOADER, "addLoader");  

        // add rules for resource types
        digester.addObjectCreate("*/" + N_VFS + "/" + N_RESOURCES + "/" + N_RESOURCETYPES + "/" + N_TYPE, A_CLASS, CmsConfigurationException.class);
        digester.addCallMethod("*/" + N_VFS + "/" + N_RESOURCES + "/" + N_RESOURCETYPES + "/" + N_TYPE, I_CmsConfigurationParameterHandler.C_INIT_CONFIGURATION_METHOD);
        digester.addSetNext("*/" + N_VFS + "/" + N_RESOURCES + "/" + N_RESOURCETYPES + "/" + N_TYPE, "addResourceType");   
        
        // generic <param> parameter rules
        digester.addCallMethod("*/" + I_CmsXmlConfiguration.N_PARAM, I_CmsConfigurationParameterHandler.C_ADD_PARAMETER_METHOD, 2);
        digester.addCallParam ("*/" +  I_CmsXmlConfiguration.N_PARAM, 0, I_CmsXmlConfiguration.A_NAME);
        digester.addCallParam ("*/" +  I_CmsXmlConfiguration.N_PARAM, 1); 
                
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
        
        // add resources main element
        Element resources = vfs.addElement(N_RESOURCES);
        
        // add resource loader
        Element resourceloadersElement = resources.addElement(N_RESOURCELOADERS);
        List loaders = m_resourceManager.getLoaders();
        for (int i=0; i<loaders.size(); i++) {
            I_CmsResourceLoader loader = (I_CmsResourceLoader)loaders.get(i);        
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
        Element resourcetypesElement = resources.addElement(N_RESOURCETYPES);
        List resourceTypes = m_resourceManager.getResourceTypes();
        for (int i=0; i<resourceTypes.size(); i++) {
            resourcetypesElement.addElement(N_TYPE).addAttribute(A_CLASS, resourceTypes.get(i).getClass().getName());
        }
        
        // add translation rules
        Element translationsElement = vfs.addElement(N_TRANSLATIONS);
        
        // file translation rules
        Element fileTransElement = 
            translationsElement.addElement(N_FILETRANSLATIONS)
                .addAttribute(A_ENABLED, new Boolean(m_fileTranslationEnabled).toString());        
        Iterator it = m_fileTranslations.iterator();
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
     * @see org.opencms.configuration.I_CmsXmlConfiguration#getDtdFilename()
     */
    public String getDtdFilename() {
        return C_CONFIGURATION_DTD_NAME;
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
     * Returns the initialized resource manager.<p>
     * 
     * @return the initialized resource manager
     */
    public CmsResourceManager getResourceManager() {
        return m_resourceManager;
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
     * Sets the generated resource manager.<p>
     * 
     * @param manager the resource manager to set
     */
    public void setResourceManager(CmsResourceManager manager) {
        m_resourceManager = manager;
    }
}
