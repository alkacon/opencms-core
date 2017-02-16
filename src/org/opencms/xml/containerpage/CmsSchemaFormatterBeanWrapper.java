/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
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

package org.opencms.xml.containerpage;

import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsLog;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContentProperty;
import org.opencms.xml.content.I_CmsXmlContentHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

/**
 * Wrapper class for formatter beans which delegates all methods to the wrapped formatter bean except those
 * for which we need to ask the content handler for additional data like element settings or head includes.<p>
 */
public class CmsSchemaFormatterBeanWrapper implements I_CmsFormatterBean {

    /** Logger instance for this class. */
    private static final Log LOG = CmsLog.getLog(CmsSchemaFormatterBeanWrapper.class);

    /** The CMS context to use. */
    private CmsObject m_cms;

    /** The content handler to use. */
    private I_CmsXmlContentHandler m_contentHandler;

    /** The resource to use. */
    private CmsResource m_elementResource;

    /** The wrapped formatter. */
    private I_CmsFormatterBean m_wrappedFormatter;

    /**
     * Creates a new wrapper instance.<p>
     *
     * @param cms the CMS context to use
     * @param wrappedBean the wrapped formatter
     * @param contentHandler the content handler to ask for additional information
     * @param resource the resource which should be used to ask the content handler for additional information
     */
    public CmsSchemaFormatterBeanWrapper(
        CmsObject cms,
        I_CmsFormatterBean wrappedBean,
        I_CmsXmlContentHandler contentHandler,
        CmsResource resource) {

        m_elementResource = resource;
        m_contentHandler = contentHandler;
        m_wrappedFormatter = wrappedBean;
        m_cms = cms;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getContainerTypes()
     */
    public Set<String> getContainerTypes() {

        return m_wrappedFormatter.getContainerTypes();
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getCssHeadIncludes()
     */
    public Set<String> getCssHeadIncludes() {

        try {
            return m_contentHandler.getCSSHeadIncludes(m_cms, m_elementResource);
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return Collections.emptySet();
        }
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getId()
     */
    public String getId() {

        return m_wrappedFormatter.getId();
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getInlineCss()
     */
    public String getInlineCss() {

        return "";
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getInlineJavascript()
     */
    public String getInlineJavascript() {

        return "";
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getJavascriptHeadIncludes()
     */
    public List<String> getJavascriptHeadIncludes() {

        try {
            return new ArrayList<String>(m_contentHandler.getJSHeadIncludes(m_cms, m_elementResource));
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getJspRootPath()
     */
    public String getJspRootPath() {

        return m_wrappedFormatter.getJspRootPath();
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getJspStructureId()
     */
    public CmsUUID getJspStructureId() {

        return m_wrappedFormatter.getJspStructureId();
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getLocation()
     */
    public String getLocation() {

        return m_wrappedFormatter.getLocation();
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getMaxWidth()
     */
    public int getMaxWidth() {

        return m_wrappedFormatter.getMaxWidth();
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getMinWidth()
     */
    public int getMinWidth() {

        return m_wrappedFormatter.getMinWidth();
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getNiceName(Locale)
     */
    public String getNiceName(Locale locale) {

        return m_wrappedFormatter.getNiceName(locale);
    }

    /**
     *
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getRank()
     */
    public int getRank() {

        return m_wrappedFormatter.getRank();
    }

    /**
     *
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getResourceTypeNames()
     */
    public Collection<String> getResourceTypeNames() {

        try {
            return Collections.singleton(OpenCms.getResourceManager().getResourceType(m_elementResource).getTypeName());
        } catch (Exception e) {
            LOG.error(e.getLocalizedMessage(), e);
            return Collections.singleton("xmlcontent");
        }
    }

    /**
     *
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#getSettings()
     */
    public Map<String, CmsXmlContentProperty> getSettings() {

        return m_contentHandler.getSettings(m_cms, m_elementResource);
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#hasNestedContainers()
     */
    public boolean hasNestedContainers() {

        return false;
    }

    /**
     *
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#isAutoEnabled()
     */
    public boolean isAutoEnabled() {

        return m_wrappedFormatter.isAutoEnabled();
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#isDetailFormatter()
     */
    public boolean isDetailFormatter() {

        return true;
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#isDisplayFormatter()
     */
    public boolean isDisplayFormatter() {

        return false;
    }

    /**
     *
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#isFromFormatterConfigFile()
     */
    public boolean isFromFormatterConfigFile() {

        return m_wrappedFormatter.isFromFormatterConfigFile();
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#isMatchAll()
     */
    public boolean isMatchAll() {

        return m_wrappedFormatter.isMatchAll();
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#isPreviewFormatter()
     */
    public boolean isPreviewFormatter() {

        return m_wrappedFormatter.isPreviewFormatter();
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#isSearchContent()
     */
    public boolean isSearchContent() {

        return m_wrappedFormatter.isSearchContent();
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#isTypeFormatter()
     */
    public boolean isTypeFormatter() {

        return m_wrappedFormatter.isTypeFormatter();
    }

    /**
     * @see org.opencms.xml.containerpage.I_CmsFormatterBean#setJspStructureId(org.opencms.util.CmsUUID)
     */
    public void setJspStructureId(CmsUUID jspStructureId) {

        m_wrappedFormatter.setJspStructureId(jspStructureId);
    }

}
