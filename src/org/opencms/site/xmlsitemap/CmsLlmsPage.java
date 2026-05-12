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

import org.opencms.util.CmsUUID;

/**
 *  A single page item for usage in the llms.txt file.<p>
 */
public class CmsLlmsPage {

    /** The XML content node name for the URL. */
    protected static final String NODE_URL = "URL";

    /** The XML content node name for the ID. */
    protected static final String NODE_ID = "ID";

    /** The XML content node name for the date. */
    protected static final String NODE_DATE = "Date";

    /** The XML content node name for the hide flag. */
    protected static final String NODE_HIDE = "Hide";

    /** The XML content node name for the title. */
    protected static final String NODE_TITLE = "Title";

    /** The XML content node name for the summary. */
    protected static final String NODE_SUMMARY = "Summary";

    /** The XML content node name for the override summary. */
    protected static final String NODE_OVERRIDESUMMARY = "OverrideSummary";

    /** The URL of the page item. */
    private String m_url;

    /** The ID of the page item. */
    private CmsUUID m_id;

    /** The last modification date of the page item. */
    private long m_date;

    /** The hide flag of the page item. */
    private boolean m_hide;

    /** The title of the page item. */
    private String m_title;

    /** The summary text of the page item. */
    private String m_summary;

    /** The optional override summary text of the page item. */
    private String m_overrideSummary;

    /**
     * Empty constructor.<p>
     */
    public CmsLlmsPage() {

    }

    /**
     * Constructor, with parameters.<p>
     *
     * @param url the URL of the page item
     * @param id the ID of the page item
     * @param date the last modification date of the page item
     * @param title the title of the page item
     * @param summary the summary text of the page item
     * @param hide the hide flag of the page item
     * @param overrideSummary the optional override summary text of the page item
     */
    public CmsLlmsPage(
        String url,
        CmsUUID id,
        long date,
        String title,
        String summary,
        boolean hide,
        String overrideSummary) {

        m_url = url;
        m_id = id;
        m_date = date;
        m_title = title;
        m_summary = summary;
        m_hide = hide;
        m_overrideSummary = overrideSummary;
    }

    /**
     * Returns the last modification date of the page item.<p>
     *
     * @return the last modification date of the page item
     */
    public long getDate() {

        return m_date;
    }

    /**
     * Returns the ID of the page item.<p>
     *
     * @return the ID of the page item
     */
    public CmsUUID getId() {

        return m_id;
    }

    /**
     * Returns the optional override summary text of the page item.<p>
     *
     * @return the optional override summary text of the page item
     */
    public String getOverrideSummary() {

        return m_overrideSummary;
    }

    /**
     * Returns the summary text of the page item.<p>
     *
     * @return the summary text of the page item
     */
    public String getSummary() {

        return m_summary;
    }

    /**
     * Returns the title of the page item.<p>
     *
     * @return the title of the page item
     */
    public String getTitle() {

        return m_title;
    }

    /**
     * Returns the URL of the page item.<p>
     *
     * @return the URL of the page item
     */
    public String getUrl() {

        return m_url;
    }

    /**
     * Returns the hide flag of the page item.<p>
     *
     * @return the ID of the page item
     */
    public boolean isHide() {

        return m_hide;
    }

    /**
     * Sets the last modification date of the page item.<p>
     *
     * @param date the last modification date of the page item
     */
    public void setDate(long date) {

        m_date = date;
    }

    /**
     * Sets the hide flag of the page item.<p>
     *
     * @param id the hide flag of the page item
     */
    public void setHide(boolean hide) {

        m_hide = hide;
    }

    /**
     * Sets the ID of the page item.<p>
     *
     * @param id the ID of the page item
     */
    public void setId(CmsUUID id) {

        m_id = id;
    }

    /**
     * Sets the optional override summary text of the page item.<p>
     *
     * @param overrideSummary the optional override summary text of the page item
     */
    public void setOverrideSummary(String overrideSummary) {

        m_overrideSummary = overrideSummary;
    }

    /**
     * Sets the summary text of the page item.<p>
     *
     * @param summary the summary text of the page item
     */
    public void setSummary(String summary) {

        m_summary = summary;
    }

    /**
     * Sets the title of the page item.<p>
     *
     * @param title the title of the page item
     */
    public void setTitle(String title) {

        m_title = title;
    }

    /**
     * Sets the URL of the page item.<p>
     *
     * @param url the URL of the page item
     */
    public void setUrl(String url) {

        m_url = url;
    }

}
