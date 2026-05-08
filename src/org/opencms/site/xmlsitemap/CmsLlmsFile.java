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
import org.opencms.util.CmsUUID;
import org.opencms.xml.content.CmsXmlContent;
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

    /** The locale to use for reading the generated XML content. */
    protected static final Locale LOCALE = Locale.ENGLISH;

    /** The last modification date of the generated information. */
    private long m_date;

    /** The pages to include. */
    private List<CmsLlmsPage> m_pages;

    /** The pages to include as map. */
    private Map<CmsUUID, CmsLlmsPage> m_pagesMap;

    /** The generated result text. */
    private String m_result;

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
        List<CmsLlmsPage> pages = new ArrayList<CmsLlmsPage>();
        for (I_CmsXmlContentValue page : content.getValues(NODE_PAGE, LOCALE)) {
            String pathPrefix = page.getPath() + "/";
            String url = content.getStringValue(cms, pathPrefix + CmsLlmsPage.NODE_URL, LOCALE);
            CmsUUID id = CmsUUID.valueOf(content.getStringValue(cms, pathPrefix + CmsLlmsPage.NODE_ID, LOCALE));
            long date = Long.parseLong(content.getStringValue(cms, pathPrefix + CmsLlmsPage.NODE_DATE, LOCALE));
            String title = content.getStringValue(cms, pathPrefix + CmsLlmsPage.NODE_TITLE, LOCALE);
            String summary = content.getStringValue(cms, pathPrefix + CmsLlmsPage.NODE_SUMMARY, LOCALE);
            String overrideSummary = content.getStringValue(cms, pathPrefix + CmsLlmsPage.NODE_OVERRIDESUMMARY, LOCALE);
            pages.add(new CmsLlmsPage(url, id, date, title, summary, overrideSummary));
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
     * Builds a map of the pages to include.<p>
     */
    private void buildPagesMap() {

        m_pagesMap = new HashMap();
        for (CmsLlmsPage page : m_pages) {
            m_pagesMap.put(page.getId(), page);
        }
    }

}
