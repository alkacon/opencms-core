/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/importexport/CmsImportExportManager.java,v $
 * Date   : $Date: 2004/07/18 16:32:33 $
 * Version: $Revision: 1.13 $
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

package org.opencms.importexport;

import org.opencms.file.CmsObject;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsSecurityException;
import org.opencms.security.I_CmsPrincipal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;

/**
 * Provides information about how to handle imported resources.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.13 $ $Date: 2004/07/18 16:32:33 $
 * @since 5.3
 * @see OpenCms#getImportExportManager()
 */
public class CmsImportExportManager extends Object {

    /** Boolean flag whether imported pages should be converted into XML pages. */
    private boolean m_convertToXmlPage;

    /** List of property keys that should be removed from imported resources. */
    private List m_ignoredProperties;

    /** List of immutable resources that should remain unchanged when resources are imported. */
    private List m_immutableResources;

    /** The initialized import/export handlers. */
    private List m_importExportHandlers;    
    
    /** Import princial group translations. */
    private Map m_importGroupTranslations;
    
    /** Import princial user translations. */
    private Map m_importUserTranslations;    
    
    /** The configured import versions class names. */
    private List m_importVersionClasses;     

    /** Boolean flag whether colliding resources should be overwritten during the import. */
    private boolean m_overwriteCollidingResources;

    /** The URL of a 4.x OpenCms app. to import content correct into 5.x OpenCms apps. */
    private String m_webAppUrl;
    
    /**
     * Creates a new instance for the import/export manager, will be called by the import/export configuration manager.
     */
    public CmsImportExportManager() {
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Import manager init  : starting");
        }
        m_importExportHandlers = new ArrayList();
        m_immutableResources = new ArrayList();
        m_ignoredProperties = new ArrayList();
        m_convertToXmlPage = true;
        m_importGroupTranslations = new HashMap();
        m_importUserTranslations = new HashMap();
        m_overwriteCollidingResources = true;
        m_importVersionClasses = new ArrayList();
    }

    /**
     * Returns the "manifest.xml" of an available import resource as a dom4j document.<p>
     * 
     * The manifest is either read as a ZIP entry, or from a subfolder of the specified
     * file resource.<p>
     * 
     * @param resource a File resource
     * @return the "manifest.xml" as a dom4j document
     */
    public static Document getManifest(File resource) {
        Document manifest = null;
        ZipFile zipFile = null;
        ZipEntry zipFileEntry = null;
        InputStream input = null;
        Reader reader = null;
        SAXReader saxReader = null;
        File manifestFile = null;

        try {
            if (resource.isFile()) {
                if (!resource.getName().toLowerCase().endsWith(".zip")) {
                    // skip non-ZIP files
                    return null;
                }

                // create a Reader either from a ZIP file's manifest.xml entry...
                zipFile = new ZipFile(resource);
                zipFileEntry = zipFile.getEntry("manifest.xml");
                input = zipFile.getInputStream(zipFileEntry);
                reader = new BufferedReader(new InputStreamReader(input));
            } else if (resource.isDirectory()) {
                // ...or from a subresource inside a folder
                manifestFile = new File(resource, "manifest.xml");
                reader = new BufferedReader(new FileReader(manifestFile));
            }

            // transform the manifest.xml file into a dom4j Document
            saxReader = new SAXReader();
            manifest = saxReader.read(reader);
        } catch (Exception e) {
            OpenCms.getLog(CmsImportExportManager.class.getName()).error("Error reading manifest.xml from resource: " + resource, e);
            manifest = null;
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Exception e) {
                // noop
            }
        }

        return manifest;
    }
    
    /**
     * Adds a property name to the list of properties that should be removed from imported resources.<p>
     * 
     * @param propertyName a property name
     */
    public void addIgnoredProperty(String propertyName) {
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Added property to ignore: " + propertyName);
        }            
        m_ignoredProperties.add(propertyName);
    }

    /**
     * Adds a resource to the list of immutable resources that should remain 
     * unchanged when resources are imported.<p>
     * 
     * @param immutableResource a resources uri in the OpenCms VFS
     */
    public void addImmutableResource(String immutableResource) {
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Added immutable resource: " + immutableResource);
        }           
        m_immutableResources.add(immutableResource);
    }
    
    /**
     * Adds an import/export handler to the list of configured handlers.<p>
     * 
     * @param handler the import/export handler to add
     */
    public void addImportExportHandler(I_CmsImportExportHandler handler) {
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Added import/export handler " + handler);
        }
        m_importExportHandlers.add(handler);
    }    

    /**
     * Adds an import princial translation to the configuration.<p>
     * 
     * @param type the princial type ("USER" or "GROUP")
     * @param from the "from" translation source
     * @param to the "to" translation target
     */
    public void addImportPrincipalTranslation(String type, String from, String to) {
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Added princial translation type:" + type + " from: " + from + " to:" + to);
        }           
        if (type.equalsIgnoreCase(I_CmsPrincipal.C_PRINCIPAL_GROUP)) {
            m_importGroupTranslations.put(from, to);  
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Name translation     : group " + from + " to " + to);
            }                
        } else if (type.equalsIgnoreCase(I_CmsPrincipal.C_PRINCIPAL_USER)) {
            m_importUserTranslations.put(from, to);      
            if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Name translation     : user " + from + " to " + to);
            }                             
        }
    }    
    
    
    /**
     * Adds a import version class name to the configuration.<p>
     * 
     * @param importVersionClass the import version class name to add
     */
    public void addImportVersionClass(I_CmsImport importVersionClass) {
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Added import version: " + importVersionClass);
        }           
        m_importVersionClasses.add(importVersionClass);
    }    

    /**
     * Checks if imported pages should be converted into XML pages.<p>
     * 
     * @return true, if imported pages should be converted into XML pages
     */
    public boolean convertToXmlPage() {
        return m_convertToXmlPage;
    }

    /**
     * Checks if the current user has permissions to export Cms data of a specified export handler,
     * and if so, triggers the handler to write the export.<p>
     * 
     * @param cms the current OpenCms context object
     * @param handler handler containing the export data
     * @param report a Cms report to print log messages
     * @throws CmsSecurityException if the current user is not a member of the administrators group
     * @throws CmsException if operation was not successful
     * @see I_CmsImportExportHandler
     */
    public void exportData(CmsObject cms, I_CmsImportExportHandler handler, I_CmsReport report) throws CmsException, CmsSecurityException {
        if (cms.isAdmin()) {
            handler.exportData(cms, report);
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "]", CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }

    /**
     * Returns the list of property keys that should be removed from imported resources.<p>
     * 
     * @return the list of property keys that should be removed from imported resources, or Collections.EMPTY_LIST
     */
    public List getIgnoredProperties() {
        return m_ignoredProperties;
    }

    /**
     * Returns the list of immutable resources that should remain unchanged when resources are 
     * imported.<p>
     * 
     * Certain system resources should not be changed during import. This is the case for the main 
     * folders in the /system/ folder. Changes to these folders usually should not be imported to 
     * another system.<p>
     * 
     * @return the list of immutable resources, or Collections.EMPTY_LIST
     */
    public List getImmutableResources() {
        return m_immutableResources;
    }

    /**
     * Returns an instance of an import/export handler implementation that is able to import
     * a specified resource.<p>
     * 
     * @param importFile the name (absolute path) of the resource (zipfile or folder) to be imported
     * @return an instance of an import/export handler implementation
     * @throws CmsException if somethong goes wrong
     */
    public I_CmsImportExportHandler getImportExportHandler(String importFile) throws CmsException {
        String classname = null;
        Document manifest = null;
        I_CmsImportExportHandler handler = null;
        
        File file = new File(importFile);
        if (!file.exists()) {
            // file does not exist
            throw new CmsException("Import file '" + importFile + "' does not exist");
        }
        
        try {
            manifest = getManifest(file);
            for (int i = 0; i < m_importExportHandlers.size(); i++) {
                handler = (I_CmsImportExportHandler)m_importExportHandlers.get(i);               
                if (handler.matches(manifest)) {
                    return handler;
                }
                
                handler = null;
            }
        } catch (Exception e) {
            throw new CmsException("Error creating instance of import/export handler " + classname, e);
        }

        if (handler == null) {
            throw new CmsException("Cannot find matching import/export handler for import of " + importFile);
        }

        return null;
    }
    
    /**
     * Returns the list of configured import/export handlers.<p>
     * 
     * @return the list of configured import/export handlers
     */
    public List getImportExportHandlers() {
        return m_importExportHandlers;
    }
    
    /**
     * Returns the configured principal group translations.<p>
     * 
     * @return the configured principal group translations
     */
    public Map getImportGroupTranslations() {
        return m_importGroupTranslations;
    }
    
    /**
     * Returns the configured principal user translations.<p>
     * 
     * @return the configured principal user translations
     */
    public Map getImportUserTranslations() {
        return m_importUserTranslations;
    }
    
    /**
     * Returns the configured import version class names.<p>
     * 
     * @return the configured import version class names
     */
    public List getImportVersionClasses() {
        return m_importVersionClasses;
    }

    /**
     * Returns the URL of a 4.x OpenCms app. (e.g. http://localhost:8080/opencms/opencms/)
     * from which content was exported.<p>
     * 
     * This setting is required to import content of 4.x OpenCms apps. correct into 5.x OpenCms apps.<p>
     * 
     * @return the webAppUrl.
     */
    public String getOldWebAppUrl() {
        return m_webAppUrl;
    }

    /**
     * Checks if the current user has permissions to import data into the Cms,
     * and if so, creates a new import handler instance that imports the data.<p>
     * 
     * @param cms the current OpenCms context object
     * @param importFile the name (absolute path) of the resource (zipfile or folder) to be imported
     * @param importPath the name (absolute path) of the destination folder in the Cms if required, or null
     * @param report a Cms report to print log messages
     * @throws CmsSecurityException if the current user is not a member of the administrators group
     * @throws CmsException if operation was not successful
     * @see I_CmsImportExportHandler
     */
    public void importData(CmsObject cms, String importFile, String importPath, I_CmsReport report) throws CmsException, CmsSecurityException {
        I_CmsImportExportHandler handler = null;

        try {
            if (cms.isAdmin()) {
                handler = getImportExportHandler(importFile);
                handler.importData(cms, importFile, importPath, report);
            } else {
                throw new CmsSecurityException("[" + this.getClass().getName() + "]", CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
            }
        } finally {
            if (cms.isAdmin()) {
                OpenCms.fireCmsEvent(new CmsEvent(cms, I_CmsEventListener.EVENT_CLEAR_CACHES, Collections.EMPTY_MAP, false));
            }
        }
    }

    /**
     * Checks if colliding resources should be overwritten during the import.<p>
     * 
     * @return true, if colliding resources should be overwritten during the import
     * @see #setOverwriteCollidingResources(boolean)
     */
    public boolean overwriteCollidingResources() {
        return m_overwriteCollidingResources;
    }

    /**
     * Sets if imported pages should be converted into XML pages.<p>
     * 
     * @param convertToXmlPage true, if imported pages should be converted into XML pages.
     */
    public void setConvertToXmlPage(boolean convertToXmlPage) {
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Import convert parameter: " + convertToXmlPage);
        }             
        m_convertToXmlPage = convertToXmlPage;
    }

    /**
     * Sets if imported pages should be converted into XML pages.<p>
     * 
     * @param convertToXmlPage "true", if imported pages should be converted into XML pages.
     */
    public void setConvertToXmlPage(String convertToXmlPage) {
        setConvertToXmlPage(Boolean.valueOf(convertToXmlPage).booleanValue());
    }

    /**
     * Sets the URL of a 4.x OpenCms app. (e.g. http://localhost:8080/opencms/opencms/)
     * from which content was exported.<p>
     * 
     * This setting is required to import content of 4.x OpenCms apps. correct into 5.x OpenCms apps.<p>
     * 
     * @param webAppUrl a URL of the a OpenCms app. (e.g. http://localhost:8080/opencms/opencms/)
     */
    public void setOldWebAppUrl(String webAppUrl) {
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Import old webapp URL: " + webAppUrl);
        }         
        m_webAppUrl = webAppUrl;
    }

    /**
     * Sets whether colliding resources should be overwritten during the import for a
     * specified import implementation.<p>
     * 
     * v1 and v2 imports (without resource UUIDs in the manifest) *MUST* overwrite colliding 
     * resources. Don't forget to set this flag back to it's original value in v1 and v2
     * import implementations!<p>
     * 
     * This flag must be set to false to force imports > v2 to move colliding resources to 
     * /system/lost-found/.<p>
     * 
     * The import implementation has to take care to set this flag correct!<p>
     * 
     * @param overwriteCollidingResources true if colliding resources should be overwritten during the import
     */
    public void setOverwriteCollidingResources(boolean overwriteCollidingResources) {
        if (OpenCms.getLog(this).isDebugEnabled()) {
            OpenCms.getLog(this).debug("Import overwrite parameter: " + overwriteCollidingResources);
        }        
        m_overwriteCollidingResources = overwriteCollidingResources;
    }
    
    /**
     * @see CmsImportExportManager#setOverwriteCollidingResources(boolean)
     * 
     * @param overwriteCollidingResources "true" if colliding resources should be overwritten during the import
     */
    public void setOverwriteCollidingResources(String overwriteCollidingResources) {
        setOverwriteCollidingResources(Boolean.valueOf(overwriteCollidingResources).booleanValue());
    }
    
    /**
     * Returns the translated name for the given group name.<p>
     * 
     * If no matching name is found, the given group name is returned.<p>
     * 
     * @param name the group name to translate
     * @return the translated name for the given group name
     */
    public String translateGroup(String name) {
        if (m_importGroupTranslations == null) {
            return name;
        }
        String match = (String)m_importGroupTranslations.get(name);
        if (match != null) {
            return match;
        } else {
            return name;
        }
    }    
    
    /**
     * Returns the translated name for the given user name.<p>
     * 
     * If no matching name is found, the given user name is returned.<p>
     * 
     * @param name the user name to translate
     * @return the translated name for the given user name
     */
    public String translateUser(String name) {
        if (m_importUserTranslations == null) {
            return name;
        }
        String match = (String)m_importUserTranslations.get(name);
        if (match != null) {
            return match;
        } else {
            return name;
        }
    }

    /**
     * @see java.lang.Object#finalize()
     */
    protected void finalize() throws Throwable {
        try {
            if (m_immutableResources != null) {
                m_immutableResources.clear();
            }
            m_immutableResources = null;

            if (m_ignoredProperties != null) {
                m_ignoredProperties.clear();
            }
            m_ignoredProperties = null;
        } catch (Throwable t) {
            // noop
        } finally {
            super.finalize();
        }
    }

}
