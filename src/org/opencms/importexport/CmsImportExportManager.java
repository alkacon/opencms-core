/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/importexport/CmsImportExportManager.java,v $
 * Date   : $Date: 2004/02/25 14:12:43 $
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

package org.opencms.importexport;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsRegistry;
import org.opencms.main.CmsEvent;
import org.opencms.main.CmsException;
import org.opencms.main.CmsLog;
import org.opencms.main.I_CmsConstants;
import org.opencms.main.I_CmsEventListener;
import org.opencms.main.OpenCms;
import org.opencms.report.I_CmsReport;
import org.opencms.security.CmsSecurityException;

import com.opencms.legacy.CmsCosImportExportHandler;

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

import org.apache.commons.collections.ExtendedProperties;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

/**
 * Provides information about how to handle imported resources.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.4 $ $Date: 2004/02/25 14:12:43 $
 * @since 5.3
 * @see OpenCms#getImportExportManager()
 */
public class CmsImportExportManager extends Object {

    /** Boolean flag whether imported pages should be converted into XML pages.<p> */
    private boolean m_convertToXmlPage;

    /** List of property keys that should be removed from imported resources.<p> */
    private List m_ignoredProperties;

    /** List of immutable resources that should remain unchanged when resources are imported.<p> */
    private List m_immutableResources;

    /** Boolean flag whether colliding resources should be overwritten during the import.<p> */
    private boolean m_overwriteCollidingResources;

    /** The URL of a 4.x OpenCms app. to import content correct into 5.x OpenCms apps.<p> */
    private String m_webAppUrl;
    
    /** The class names of the import/export handlers keyed by the type names of the import/export handlers.<p> */
    private Map m_importExportHandlerClassNames;

    /**
     * Creates a new import/export manager.<p>
     * 
     * @param immutableResources a list of immutable resources that should remain unchanged when resources are imported
     * @param convertToXmlPage true, if imported pages should be converted into XML pages
     * @param overwriteCollidingResources true, if collding resources should be overwritten during an import
     * @param webAppUrl the URL of a 4.x OpenCms app. to import content of 4.x OpenCms apps. correct into 5.x OpenCms apps.
     * @param ignoredProperties a list of property keys that should be removed from imported resources
     * @param importExportHandlerClassNames a map with the class names of the import/export handlers keyed by the type names of the import/export handlers
     */
    public CmsImportExportManager(List immutableResources, boolean convertToXmlPage, boolean overwriteCollidingResources, String webAppUrl, List ignoredProperties, Map importExportHandlerClassNames) {
        m_immutableResources = (immutableResources != null && immutableResources.size() > 0) ? immutableResources : Collections.EMPTY_LIST;
        m_ignoredProperties = (ignoredProperties != null && ignoredProperties.size() > 0) ? ignoredProperties : Collections.EMPTY_LIST;
        m_importExportHandlerClassNames = (importExportHandlerClassNames != null && importExportHandlerClassNames.keySet().size() > 0) ? importExportHandlerClassNames : Collections.EMPTY_MAP;
        
        m_convertToXmlPage = convertToXmlPage;
        m_overwriteCollidingResources = overwriteCollidingResources;
        m_webAppUrl = webAppUrl;
    }

    /**
     * Initializes the import/export manager with the OpenCms system configuration.<p>
     * 
     * @param cms the current OpenCms context object
     * @param configuration the OpenCms configuration
     * @return the initialized import/export manager
     */
    public static CmsImportExportManager initialize(ExtendedProperties configuration, CmsObject cms) {
        // read the immutable import resources
        String[] immuResources = configuration.getStringArray("import.immutable.resources");
        if (immuResources == null) {
            immuResources = new String[0];
        }

        List immutableResourcesOri = java.util.Arrays.asList(immuResources);
        ArrayList immutableResources = new ArrayList();
        for (int i = 0; i < immutableResourcesOri.size(); i++) {
            // remove possible white space
            String path = ((String) immutableResourcesOri.get(i)).trim();
            if (path != null && !"".equals(path)) {
                immutableResources.add(path);
                if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                    OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Immutable resource   : " + (i + 1) + " - " + path);
                }
            }
        }
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Immutable resources  : " + ((immutableResources.size() > 0) ? "enabled" : "disabled"));
        }

        // read the conversion setting
        String convertToXmlPageValue = configuration.getString("import.convert.xmlpage");
        boolean convertToXmlPage = (convertToXmlPageValue != null) ? "true".equalsIgnoreCase(convertToXmlPageValue.trim()) : false;
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Convert to XML page  : " + (convertToXmlPage ? "enabled" : "disabled"));
        }

        // convert import files from 4.x versions old webapp URL
        String webappUrl = configuration.getString("compatibility.support.import.old.webappurl", null);
        webappUrl = (webappUrl != null) ? webappUrl.trim() : null;
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Old webapp URL       : " + ((webappUrl == null) ? "not set!" : webappUrl));
        }

        // unwanted resource properties which are deleted during import
        String[] propNames = configuration.getStringArray("compatibility.support.import.remove.propertytags");
        if (propNames == null) {
            propNames = new String[0];
        }
        List propertyNamesOri = java.util.Arrays.asList(propNames);
        ArrayList propertyNames = new ArrayList();
        for (int i = 0; i < propertyNamesOri.size(); i++) {
            // remove possible white space
            String name = ((String) propertyNamesOri.get(i)).trim();
            if (name != null && !"".equals(name)) {
                propertyNames.add(name);
                if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
                    OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Clear import property: " + (i + 1) + " - " + name);
                }
            }
        }
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Remove properties    : " + ((propertyNames.size() > 0) ? "enabled" : "disabled"));
        }

        // should colliding resources be overwritten or moved to lost+found?
        String overwriteCollidingResourcesValue = configuration.getString("import.overwrite.colliding.resources");
        boolean overwriteCollidingResources = (overwriteCollidingResourcesValue != null) ? "true".equalsIgnoreCase(overwriteCollidingResourcesValue.trim()) : false;
        if (OpenCms.getLog(CmsLog.CHANNEL_INIT).isInfoEnabled()) {
            OpenCms.getLog(CmsLog.CHANNEL_INIT).info(". Overwrite collisions : " + (overwriteCollidingResources ? "enabled" : "disabled"));
        }
        
        Map importExportHandlerClassNames = (Map) new HashMap();
        CmsRegistry registry = cms.getRegistry();
        Element systemElement = registry.getDom4jSystemElement();
        Element handlerElement = null;
        Attribute handlerAttribute = null;
        List handlerClasses = systemElement.selectNodes("./importexport/handler");
        for (int i=0;i<handlerClasses.size();i++) {
            handlerElement = (Element) handlerClasses.get(i);
            handlerAttribute = handlerElement.attribute("name");
            importExportHandlerClassNames.put(handlerAttribute.getValue(), handlerElement.getTextTrim());
        }
        
        // create and return the import/export manager 
        return new CmsImportExportManager(immutableResources, convertToXmlPage, overwriteCollidingResources, webappUrl, propertyNames, importExportHandlerClassNames);
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
        } catch (Exception e) {
            // noop
        } finally {
            super.finalize();
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
     * Returns the URL of a 4.x OpenCms app. (e.g. http://localhost:8080/opencms/opencms/)
     * from which content was exported.<p>
     * 
     * This setting is required to import content of 4.x OpenCms apps. correct into 5.x OpenCms apps.<p>
     * 
     * @return the webAppUrl.
     */
    public String getWebAppUrl() {
        return m_webAppUrl;
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
    void setConvertToXmlPage(boolean convertToXmlPage) {
        m_convertToXmlPage = convertToXmlPage;
    }

    /**
     * Sets if imported pages should be converted into XML pages.<p>
     * 
     * @param convertToXmlPage "true", if imported pages should be converted into XML pages.
     */
    void setConvertToXmlPage(String convertToXmlPage) {
        m_convertToXmlPage = "true".equalsIgnoreCase(convertToXmlPage);
    }

    /**
     * Sets the list of property keys that should be removed from imported resources.<p>
     * 
     * @param ignoredProperties a list of property keys that should be removed from imported resources
     */
    void setIgnoredProperties(List ignoredProperties) {
        m_ignoredProperties = (ignoredProperties != null && ignoredProperties.size() > 0) ? ignoredProperties : Collections.EMPTY_LIST;
    }

    /**
     * Sets the list of immutable resources that should remain unchanged when resources are 
     * imported.<p>
     * 
     * @param immutableResources a list of immutable resources
     */
    void setImmutableResources(List immutableResources) {
        m_immutableResources = (immutableResources != null && immutableResources.size() > 0) ? immutableResources : Collections.EMPTY_LIST;
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
    void setOverwriteCollidingResources(boolean overwriteCollidingResources) {
        m_overwriteCollidingResources = overwriteCollidingResources;
    }

    /**
     * Sets the URL of a 4.x OpenCms app. (e.g. http://localhost:8080/opencms/opencms/)
     * from which content was exported.<p>
     * 
     * This setting is required to import content of 4.x OpenCms apps. correct into 5.x OpenCms apps.<p>
     * 
     * @param webAppUrl a URL of the a OpenCms app. (e.g. http://localhost:8080/opencms/opencms/)
     */
    void setWebAppUrl(String webAppUrl) {
        m_webAppUrl = webAppUrl;
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
     */
    public void exportData(CmsObject cms, I_CmsImportExportHandler handler, I_CmsReport report) throws CmsException, CmsSecurityException {
        if (cms.isAdmin()) {
            handler.exportData(cms, report);
        } else {
            throw new CmsSecurityException("[" + this.getClass().getName() + "]", CmsSecurityException.C_SECURITY_ADMIN_PRIVILEGES_REQUIRED);
        }
    }
    
    /**
     * Checks if the current user has permissions to import COS/VFS/module data into the Cms,
     * and if so, creates a new import handler instance that imports the data.<p>
     * 
     * @param cms the current OpenCms context object
     * @param importFile the name (absolute path) of the resource (zipfile or folder) to be imported
     * @param importPath the name (absolute path) of the destination folder in the Cms in case of a COS/VFS import, or null
     * @param report a Cms report to print log messages
     * @throws CmsSecurityException if the current user is not a member of the administrators group
     * @throws CmsException if operation was not successful
     */
    public void importData(CmsObject cms, String importFile, String importPath, I_CmsReport report) throws CmsException, CmsSecurityException {
        I_CmsImportExportHandler handler = null;
        String handlerKey = null;

        try {
            if (cms.isAdmin()) {
                //report.println(report.key("report.clearcache"), I_CmsReport.C_FORMAT_NOTE);
                //OpenCms.fireCmsEvent(new CmsEvent(cms, I_CmsEventListener.EVENT_CLEAR_CACHES, Collections.EMPTY_MAP, false));

                handlerKey = getImportExportHandlerKey(importFile);
                handler = getImportExportHandler(handlerKey);
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
     * Returns a new import/export handler class instance for a specified key.<p>
     * 
     * @param key the key name to get an instance of the import/export handler implementation
     * @return a new import/export handler class instance
     * @throws CmsException if the import/export handler instance couldn't be instanciated
     */
    I_CmsImportExportHandler getImportExportHandler(String key) throws CmsException {
        String classname = null;
        
        try {
            classname = (String) m_importExportHandlerClassNames.get(key);
            if (classname == null) {
                throw new CmsException("No import/export handler class found for key: " + key);
            }

            return (I_CmsImportExportHandler) Class.forName(classname).newInstance();
        } catch (Exception e) {
            throw new CmsException("Instanciation of import/export handler class " + classname + " for key " + key + " failed", e);
        }
    }
    
    /**
     * Returns the key name of the import handler implementation to import a specified file.<p>
     * 
     * @param importFile the name (absolute path) of the resource (zipfile or folder) to be imported
     * @return the key name of the import handler implementation
     * @throws CmsException if the specified import file does not exist
     */
    String getImportExportHandlerKey(String importFile) throws CmsException {
        File file = new File(importFile);
        
        if (!file.exists()) {
            throw new CmsException("The specified import file " + importFile + " does not exist!", CmsException.C_NOT_FOUND);
        }
        
        Document manifest = getManifest(file);
        String rootElementName = manifest.getRootElement().getName();
        String key = null;
        
        if (manifest.getRootElement().selectNodes("./module/name").size() > 0) {
            key = CmsModuleImportExportHandler.C_TYPE_MODDATA;
        } else if (I_CmsConstants.C_EXPORT_TAG_MODULEXPORT.equals(rootElementName)) {
            key = CmsCosImportExportHandler.C_TYPE_COSDATA;
        } else {
            key = CmsVfsImportExportHandler.C_TYPE_VFSDATA;
        }
        
        return key;
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
            System.err.println("Error reading manifest.xml from resource: " + resource + ", " + e.toString());
            e.printStackTrace(System.err);
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

}
