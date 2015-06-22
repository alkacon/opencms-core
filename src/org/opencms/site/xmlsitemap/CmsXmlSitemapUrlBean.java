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

import org.opencms.file.CmsResource;
import org.opencms.i18n.CmsEncoder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang.builder.ReflectionToStringBuilder;

/**
 * A bean which represents an entry in an XML sitemap for SEO purposes.<p>
 */
public class CmsXmlSitemapUrlBean {

    /** The format used to format the last modification date. */
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

    /** The change frequency. */
    private String m_changeFrequency;

    /** The detail page resource. */
    private CmsResource m_detailPageResource;

    /** The last modification date. */
    private Date m_lastModified;

    /** The locale for which the bean has been created (only used for detail pages). */
    private Locale m_locale;

    /** The original resource. */
    private CmsResource m_origResource;

    /** The priority. */
    private double m_priority;

    /** The subsite for which the bean has been created (only used for detail pages). */
    private String m_subsite;

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
     * Gets the detail page resource in case the link is the link to a detail page, else returns null.<p>
     *
     * @return the container page used as the detail page
     */
    public CmsResource getDetailPageResource() {

        return m_detailPageResource;
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
     * Returns the locale.<p>
     *
     * @return the locale
     */
    public Locale getLocale() {

        return m_locale;
    }

    /**
     * Gets the original resource belonging to the link.<p>
     *
     * In case this is a link to a detail page, the resource will be the resource displayed on the detail page
     *
     * @return the original resource
     */
    public CmsResource getOriginalResource() {

        return m_origResource;
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
     * Returns the subsite.<p>
     *
     * @return the subsite
     */
    public String getSubsite() {

        return m_subsite;
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
     * Sets the detail page resource.<p>
     *
     * @param detailPageResource the detail page resource
     */
    public void setDetailPageResource(CmsResource detailPageResource) {

        m_detailPageResource = detailPageResource;
    }

    /**
     * Sets the locale.<p>
     *
     * @param locale the locale to set
     */
    public void setLocale(Locale locale) {

        m_locale = locale;
    }

    /**
     * Sets the original resource.<p>
     *
     * @param resource the original resource
     */
    public void setOriginalResource(CmsResource resource) {

        m_origResource = resource;
    }

    /**
     * Sets the subsite.<p>
     *
     * @param subsite the subsite to set
     */
    public void setSubsite(String subsite) {

        m_subsite = subsite;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        return m_url + "   [" + ReflectionToStringBuilder.toString(this) + "]";
    }

    /**
     * Writes the changefreq node to the buffer.<p>
     *
     * @param buffer the buffer to write to
     */
    public void writeChangefreq(StringBuffer buffer) {

        if (m_changeFrequency != null) {
            writeElement(buffer, "changefreq", getChangeFrequency());
        }
    }

    /**
     * Writes a single XML element with text content to a string buffer.<p>
     *
     * @param buffer the string buffer to write to
     * @param tag the XML tag name
     * @param content the content of the XML element
     */
    public void writeElement(StringBuffer buffer, String tag, String content) {

        buffer.append("<" + tag + ">");
        buffer.append(CmsEncoder.escapeXml(content));
        buffer.append("</" + tag + ">");
    }

    /**
     * Writes the lastmod node to the buffer.<p>
     *
     * @param buffer the buffer to write to
     */
    public void writeLastmod(StringBuffer buffer) {

        if (m_lastModified != null) {
            writeElement(buffer, "lastmod", getFormattedDate());
        }
    }

    /**
     * Writes the priority node to the buffer.<p>
     *
     * @param buffer the buffer to write to
     */
    public void writePriority(StringBuffer buffer) {

        if ((m_priority >= 0) && (m_priority <= 1)) {
            writeElement(buffer, "priority", "" + getPriority());
        }
    }

}
