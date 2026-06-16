/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (https://www.alkacon.com)
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
 * company website: https://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: https://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.site.xmlsitemap;

import org.opencms.file.CmsObject;
import org.opencms.file.types.CmsResourceTypeXmlContent;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.util.CmsStringUtil;
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContent;
import org.opencms.xml.content.CmsXmlContentFactory;
import org.opencms.xml.types.I_CmsXmlContentValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Stores the generated information for the llms.txt file.<p>
 */
public class CmsLlmsFile {

    /** The XML content node name for the date. */
    protected static final String NODE_DATE = "Date";

    /** The XML content node name for the page. */
    protected static final String NODE_PAGE = "Page";

    /** The XML content node name for the result. */
    protected static final String NODE_RESULT = "Result";

    /** The XML content node name for the skip structure. */
    protected static final String NODE_SKIPSTRUCTURE = "SkipStructure";

    /** The locale to use for reading the generated XML content. */
    protected static final Locale LOCALE = Locale.ENGLISH;

    /** The type name of the llms file. */
    protected static final String VFS_FILE_TYPE_NAME = "llms_file";

    /** The last modification date of the generated information. */
    private long m_date;

    /** The pages to include. */
    private List<CmsLlmsPage> m_pages;

    /** The pages to include as map. */
    private Map<CmsUUID, CmsLlmsPage> m_pagesMap;

    /** The generated result text. */
    private String m_result;

    /** The flag to skip structure creation. */
    private boolean m_skipStructure;

    /**
     * Empty constructor.<p>
     */
    private CmsLlmsFile() {

    }

    /**
     * Creates the files bean from the given XML content.<p>
     *
     * @param content the XML content
     * @param cms the current users context
     * @return the files bean from the given XML content
     */
    protected static CmsLlmsFile createLlmsFileFromContent(CmsXmlContent content, CmsObject cms) {

        CmsLlmsFile result = new CmsLlmsFile();
        result.setDate(Long.parseLong(content.getStringValue(cms, NODE_DATE, LOCALE)));
        result.setSkipStructure(Boolean.parseBoolean(content.getStringValue(cms, NODE_SKIPSTRUCTURE, LOCALE)));
        List<CmsLlmsPage> pages = new ArrayList<CmsLlmsPage>();
        for (I_CmsXmlContentValue page : content.getValues(NODE_PAGE, LOCALE)) {
            String pathPrefix = page.getPath() + "/";
            String url = content.getStringValue(cms, pathPrefix + CmsLlmsPage.NODE_URL, LOCALE);
            CmsUUID id = CmsUUID.valueOf(content.getStringValue(cms, pathPrefix + CmsLlmsPage.NODE_ID, LOCALE));
            long date = Long.parseLong(content.getStringValue(cms, pathPrefix + CmsLlmsPage.NODE_DATE, LOCALE));
            String title = content.getStringValue(cms, pathPrefix + CmsLlmsPage.NODE_TITLE, LOCALE);
            String summary = content.getStringValue(cms, pathPrefix + CmsLlmsPage.NODE_SUMMARY, LOCALE);
            String prefix = content.getStringValue(cms, pathPrefix + CmsLlmsPage.NODE_PREFIX, LOCALE);
            boolean hide = Boolean.parseBoolean(
                content.getStringValue(cms, pathPrefix + CmsLlmsPage.NODE_HIDE, LOCALE));
            String overrideSummary = content.getStringValue(cms, pathPrefix + CmsLlmsPage.NODE_OVERRIDESUMMARY, LOCALE);
            pages.add(new CmsLlmsPage(url, id, date, title, summary, prefix, hide, overrideSummary));
        }
        result.setPages(pages);
        result.setResult(content.getStringValue(cms, NODE_RESULT, LOCALE));

        return result;
    }

    /**
     * Returns the last modification date of the generated information.<p>
     *
     * @return the last modification date of the generated information
     */
    public long getDate() {

        return m_date;
    }

    /**
     * Returns the pages to include.<p>
     *
     * @return the pages to include
     */
    public List<CmsLlmsPage> getPages() {

        return m_pages;
    }

    /**
     * Returns a map of the pages to include.<p>
     *
     * @return a map of the pages to include
     */
    public Map<CmsUUID, CmsLlmsPage> getPagesMap() {

        return m_pagesMap;
    }

    /**
     * Returns the generated result text.<p>
     *
     * @return the generated result text
     */
    public String getResult() {

        return m_result;
    }

    /**
     * Returns the XML content generated from the llms file bean.<p>
     *
     * @param cms the current users instance to use for setting the values
     * @return the generated XML content
     *
     * @throws CmsException if generation of the XML content fails
     */
    public CmsXmlContent getXmlContentFromLlmsFile(CmsObject cms) throws CmsException {

        // create empty content to fill with updated data
        CmsXmlContent content = CmsXmlContentFactory.createDocument(
            cms,
            LOCALE,
            (CmsResourceTypeXmlContent)OpenCms.getResourceManager().getResourceType(VFS_FILE_TYPE_NAME));

        // iterate the updated pages, generate XML content values from them
        int pageIndex = 0;
        for (CmsLlmsPage page : getPages()) {
            I_CmsXmlContentValue pageValue = content.addValue(cms, NODE_PAGE, LOCALE, pageIndex);
            String pageXmlPathPrefix = pageValue.getPath() + "/";

            content.getValue(pageXmlPathPrefix + CmsLlmsPage.NODE_DATE, LOCALE).setStringValue(
                cms,
                String.valueOf(page.getDate()));
            content.getValue(pageXmlPathPrefix + CmsLlmsPage.NODE_ID, LOCALE).setStringValue(
                cms,
                page.getId().getStringValue());
            content.getValue(pageXmlPathPrefix + CmsLlmsPage.NODE_URL, LOCALE).setStringValue(cms, page.getUrl());
            content.getValue(pageXmlPathPrefix + CmsLlmsPage.NODE_TITLE, LOCALE).setStringValue(cms, page.getTitle());
            content.getValue(pageXmlPathPrefix + CmsLlmsPage.NODE_SUMMARY, LOCALE).setStringValue(
                cms,
                page.getSummary());
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(page.getPrefix())) {
                content.addValue(cms, pageXmlPathPrefix + CmsLlmsPage.NODE_PREFIX, LOCALE, 0);
                content.getValue(pageXmlPathPrefix + CmsLlmsPage.NODE_PREFIX, LOCALE).setStringValue(
                    cms,
                    page.getPrefix());
            }
            content.getValue(pageXmlPathPrefix + CmsLlmsPage.NODE_HIDE, LOCALE).setStringValue(
                cms,
                Boolean.toString(page.isHide()));
            if (CmsStringUtil.isNotEmptyOrWhitespaceOnly(page.getOverrideSummary())) {
                content.addValue(cms, pageXmlPathPrefix + CmsLlmsPage.NODE_OVERRIDESUMMARY, LOCALE, 0);
                content.getValue(pageXmlPathPrefix + CmsLlmsPage.NODE_OVERRIDESUMMARY, LOCALE).setStringValue(
                    cms,
                    page.getOverrideSummary());
            }
            pageIndex++;
        }

        // set the result value
        content.getValue(NODE_RESULT, LOCALE).setStringValue(cms, getResult());

        // set time stamp
        content.getValue(NODE_DATE, LOCALE).setStringValue(cms, String.valueOf(getDate()));

        // set skip structure flag
        content.getValue(NODE_SKIPSTRUCTURE, LOCALE).setStringValue(cms, Boolean.toString(isSkipStructure()));

        return content;
    }

    /**
     * Returns the flag to skip structure creation.<p>
     *
     * @return the flag to skip structure creation
     */
    public boolean isSkipStructure() {

        return m_skipStructure;
    }

    /**
     * Sets the last modification date of the generated information.<p>
     *
     * @param date the last modification date of the generated information
     */
    public void setDate(long date) {

        m_date = date;
    }

    /**
     * Sets the pages to include.<p>
     *
     * @param pages the pages to include
     */
    public void setPages(List<CmsLlmsPage> pages) {

        m_pages = pages;
        buildPagesMap();
    }

    /**
     * Sets the generated result text.<p>
     *
     * @param result the generated result text
     */
    public void setResult(String result) {

        m_result = result;
    }

    /**
     * Sets the flag to skip structure creation.<p>
     *
     * @param skipStructure the flag to skip structure creation
     */
    public void setSkipStructure(boolean skipStructure) {

        m_skipStructure = skipStructure;
    }

    /**
     * Builds a map of the pages to include.<p>
     */
    private void buildPagesMap() {

        m_pagesMap = new HashMap();
        for (CmsLlmsPage page : m_pages) {
            m_pagesMap.put(page.getId(), page);
        }
    }

}
