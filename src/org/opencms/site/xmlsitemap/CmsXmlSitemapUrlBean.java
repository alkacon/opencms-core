/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) Alkacon Software (http://www.alkacon.com)
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

package org.opencms.site.xmlsitemap;

import org.opencms.i18n.CmsEncoder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A bean which represents an entry in an XML sitemap for SEO purposes.<p>
 */
public class CmsXmlSitemapUrlBean {

    /** The format used to format the last modification date. */
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    /** The change frequency. */
    private String m_changeFrequency;

    /** The last modification date. */
    private Date m_lastModified;

    /** The priority. */
    private double m_priority;

    /** The URL. */
    private String m_url;

    /**
     * Creates a new instance.<p>
     * 
     * @param url the URL
     * @param lastModified the last modification date 
     * @param changeFrequency the change frequency string 
     * @param priority the priority 
     */
    public CmsXmlSitemapUrlBean(String url, long lastModified, String changeFrequency, double priority) {

        m_url = url;
        if (lastModified >= 0) {
            m_lastModified = new Date(lastModified);
        }
        m_changeFrequency = changeFrequency;
        m_priority = priority;
    }

    /**
     * Helper method to format a date in the W3C datetime format.<p>
     * 
     * @param date the date to format 
     * 
     * @return the formatted date 
     */
    private static String formatDate(Date date) {

        String dateStr = dateFormat.format(date);
        // insert colon into timezone 
        return dateStr.substring(0, 22) + ":" + dateStr.substring(22);
    }

    /** 
     * Gets the change frequency string.<p>
     * 
     * @return the change frequency string 
     */
    public String getChangeFrequency() {

        return m_changeFrequency;
    }

    /**
     * Gets the last modification date.<p>
     * 
     * @return the last modification date 
     */
    public Date getDateLastModified() {

        return m_lastModified;
    }

    /**
     * Gets the last modification date formatted as W3C datetime.<p>
     * 
     * @return the formatted last modification date 
     */
    public String getFormattedDate() {

        return formatDate(m_lastModified);
    }

    /**
     * Gets the priority for the page.<p>
     * 
     * @return the priority 
     */
    public double getPriority() {

        return m_priority;
    }

    /**
     * Gets the page URL.<p>
     * 
     * @return the page URL 
     */
    public String getUrl() {

        return m_url;
    }

    /** 
     * Renders this single bean as XML for the XML sitemap format.<p>
     * 
     * @return an XML representation of this bean
     */
    public String renderXml() {

        StringBuffer buffer = new StringBuffer();
        buffer.append("<url>");
        writeElement(buffer, "loc", getUrl());
        if (m_lastModified != null) {
            writeElement(buffer, "lastmod", getFormattedDate());
        }
        if (m_changeFrequency != null) {
            writeElement(buffer, "changefreq", getChangeFrequency());
        }
        if ((m_priority >= 0) && (m_priority <= 1)) {
            writeElement(buffer, "priority", "" + getPriority());
        }
        buffer.append("</url>");
        return buffer.toString();

    }

    /**
     * Writes a single XML element with text content to a string buffer.<p>
     * 
     * @param buffer the string buffer to write to 
     * @param tag the XML tag name 
     * @param content the content of the XML element 
     */
    private void writeElement(StringBuffer buffer, String tag, String content) {

        buffer.append("<" + tag + ">");
        buffer.append(CmsEncoder.escapeXml(content));
        buffer.append("</" + tag + ">");
    }

}
