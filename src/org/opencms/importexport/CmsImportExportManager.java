/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/importexport/CmsImportExportManager.java,v $
 * Date   : $Date: 2004/02/12 14:54:52 $
 * Version: $Revision: 1.3 $
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

import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.ExtendedProperties;

/**
 * Provides information about how to handle imported resources.<p>
 * 
 * @author Thomas Weckert (t.weckert@alkacon.com)
 * @version $Revision: 1.3 $ $Date: 2004/02/12 14:54:52 $
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

    /**
     * Creates a new import/export manager.<p>
     * 
     * @param immutableResources a list of immutable resources that should remain unchanged when resources are imported
     * @param convertToXmlPage true, if imported pages should be converted into XML pages
     * @param overwriteCollidingResources true, if collding resources should be overwritten during an import
     * @param webAppUrl the URL of a 4.x OpenCms app. to import content of 4.x OpenCms apps. correct into 5.x OpenCms apps.
     * @param ignoredProperties a list of property keys that should be removed from imported resources
     */
    public CmsImportExportManager(List immutableResources, boolean convertToXmlPage, boolean overwriteCollidingResources, String webAppUrl, List ignoredProperties) {
        m_immutableResources = (immutableResources != null && immutableResources.size() > 0) ? immutableResources : Collections.EMPTY_LIST;
        m_ignoredProperties = (ignoredProperties != null && ignoredProperties.size() > 0) ? ignoredProperties : Collections.EMPTY_LIST;

        m_convertToXmlPage = convertToXmlPage;
        m_overwriteCollidingResources = overwriteCollidingResources;
        m_webAppUrl = webAppUrl;
    }

    /**
     * Initializes the import/export manager with the OpenCms system configuration.<p>
     * 
     * @param configuration the OpenCms configuration
     * @return the initialized import/export manager
     */
    public static CmsImportExportManager initialize(ExtendedProperties configuration) {
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

        // create and return the import/export manager 
        return new CmsImportExportManager(immutableResources, convertToXmlPage, overwriteCollidingResources, webappUrl, propertyNames);
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

}
