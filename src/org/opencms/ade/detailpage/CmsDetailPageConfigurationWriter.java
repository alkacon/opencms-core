/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.ade.detailpage;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsLocaleManager;
import org.opencms.main.CmsException;
import org.opencms.util.CmsUUID;
import org.opencms.xml.I_CmsXmlDocument;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.CmsXmlVfsFileValue;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.List;
import java.util.Locale;

/**
 * Class for writing detail page information to an XML configuration file.<p>
 * 
 * @since 8.0.0
 */
public class CmsDetailPageConfigurationWriter {

    /** The detail page node. */
    public static final String N_DETAIL_PAGE = "DetailPage";

    /** The name of the node containing the reference to the actual detail page. */
    public static final String N_PAGE = "Page";

    /** The name of the node which contains the type which the detail page renders. */
    public static final String N_TYPE = "Type";

    /** The CMS context. */
    private CmsObject m_cms;

    /** The content of the configuration file. */
    private CmsXmlContent m_document;

    /** The configuration file record. */
    private CmsFile m_file;

    /** The configuration file resource. */
    private CmsResource m_resource;

    /**
     * Creates a new detail page configuration writer.<p>
     * 
     * @param cms the current CMS context 
     * @param res the configuration file resource
     */
    public CmsDetailPageConfigurationWriter(CmsObject cms, CmsResource res) {

        m_cms = cms;
        m_resource = res;
    }

    /**
     * Writes the new detail page information to the configuration file.<p>
     * 
     * @param infos the new detail page information
     * @param newId the id to use for new pages  
     * 
     * @throws CmsException if something goes wrong 
     */
    public void updateAndSave(List<CmsDetailPageInfo> infos, CmsUUID newId) throws CmsException {

        //lock(m_cms, m_resource);
        getDocument();
        removeOldValues();
        writeDetailPageInfos(infos, newId);
        byte[] content = m_document.marshal();
        m_file.setContents(content);
        m_cms.writeFile(m_file);
        //m_cms.unlockResource(m_cms.getSitePath(m_resource));
    }

    /**
     * Helper method for loading the XML content from the configuration file.<p>
     * 
     * @return the parsed XML document 
     * 
     * @throws CmsException if something goes wrong 
     */
    private I_CmsXmlDocument getDocument() throws CmsException {

        if (m_document == null) {
            m_file = m_cms.readFile(m_resource);
            CmsXmlContent content = CmsXmlContentFactory.unmarshal(m_cms, m_file);
            m_document = content;
        }
        return m_document;
    }

    /**
     * Helper method for getting the locale from which to read the configuration data.<p>
     * 
     * @return the locale from which to read the configuration data
     *  
     * @throws CmsException if something goes wrong 
     */
    private Locale getLocale() throws CmsException {

        getDocument();
        List<Locale> locales = m_document.getLocales();
        if (locales.contains(Locale.ENGLISH) || locales.isEmpty()) {
            return Locale.ENGLISH;
        }
        return locales.get(0);
    }

    /**
     * Removes the old detail page information from the XML content.<p>
     * 
     * @throws CmsException if something goes wrong 
     */
    private void removeOldValues() throws CmsException {

        Locale locale = getLocale();
        I_CmsXmlContentValue value = m_document.getValue(N_DETAIL_PAGE, locale);
        do {
            value = m_document.getValue(N_DETAIL_PAGE, locale);
            if (value != null) {
                m_document.removeValue(value.getPath(), locale, 0);
            }
        } while (value != null);
    }

    /**
     * Writes the detail page information to the XML content.<p>
     * 
     * @param infos the list of detail page information bean 
     * @param newId the id to use for new pages 
     */
    private void writeDetailPageInfos(List<CmsDetailPageInfo> infos, CmsUUID newId) {

        int i = 0;
        for (CmsDetailPageInfo info : infos) {
            CmsUUID id = info.getId();
            if (id == null) {
                id = newId;
            }
            writeValue(info.getType(), id, i);
            i += 1;
        }
    }

    /**
     * Writes a single item of detail page information to the XML content.<p>
     * 
     * @param type the type which the detail page should render  
     * @param id the page id of the detail page 
     * @param index the position at which the detail page info should be added 
     */
    private void writeValue(String type, CmsUUID id, int index) {

        Locale locale = CmsLocaleManager.getLocale("en");
        // todo: check actual locale.
        m_document.addValue(m_cms, N_DETAIL_PAGE, locale, index);
        String typePath = N_DETAIL_PAGE + "[" + (1 + index) + "]/" + N_TYPE;
        I_CmsXmlContentValue typeVal = m_document.getValue(typePath, locale);
        String pagePath = N_DETAIL_PAGE + "[" + (1 + index) + "]/" + N_PAGE;
        CmsXmlVfsFileValue pageVal = (CmsXmlVfsFileValue)m_document.getValue(pagePath, locale);
        typeVal.setStringValue(m_cms, type);
        pageVal.setIdValue(m_cms, id);
    }
}
