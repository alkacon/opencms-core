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
 * For further information about Alkacon Software GmbH & Co. KG, please see the
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

/**
 * The CmsExtendedHtmlImportManager keeps the default values for the HTML-> OpenCms Template converter.<p>
 *
 * This default values are saved in the configuration file <code>opencms-importexport.xml</code>.<p>
 *
 * @see org.opencms.importexport.CmsImportExportManager#getExtendedHtmlImportDefault()
 *
 * @since 7.0.2
 */
public class CmsExtendedHtmlImportDefault {

    /** the destination directory in the OpenCms VFS. */
    private String m_destinationDir;

    /** the gallery name of the downloads. */
    private String m_downloadGallery;

    /** the element name of the template. */
    private String m_element;

    /** the encoding used for all imported input files. */
    private String m_encoding;

    /** the end pattern for extracting content. */
    private String m_endPattern;

    /** the image gallery name. */
    private String m_imageGallery;

    /** the input directory in the "real" file system. */
    private String m_inputDir;

    /** should broken links be kept. */
    private String m_keepBrokenLinks;

    /** the external link gallery name. */
    private String m_linkGallery;

    /** the local use for content definition. */
    private String m_locale;

    /** the overwrite value new resources. */
    private String m_overwrite;

    /** the start pattern for extracting content. */
    private String m_startPattern;

    /** the template use for all pages. */
    private String m_template;

    /**
     * Default Constructor.<p>
     */
    public CmsExtendedHtmlImportDefault() {

        m_overwrite = "true";
        m_keepBrokenLinks = "";
        m_template = "/system/modules/org.opencms.welcome/templates/empty";
        m_startPattern = "";
        m_locale = "en";
        m_linkGallery = "";
        m_encoding = "ISO-8859-1";
        m_inputDir = "";
        m_imageGallery = "";
        m_endPattern = "";
        m_element = "body";
        m_downloadGallery = "";
        m_destinationDir = "";
    }

    /**
     * Returns the destinationDir.<p>
     *
     * @return the destinationDir
     */
    public String getDestinationDir() {

        return m_destinationDir;
    }

    /**
     * Returns the downloadGallery.<p>
     *
     * @return the downloadGallery
     */
    public String getDownloadGallery() {

        return m_downloadGallery;
    }

    /**
     * Returns the element.<p>
     *
     * @return the element
     */
    public String getElement() {

        return m_element;
    }

    /**
     * Returns the encoding.<p>
     *
     * @return the encoding
     */
    public String getEncoding() {

        return m_encoding;
    }

    /**
     * Returns the endPattern.<p>
     *
     * @return the endPattern
     */
    public String getEndPattern() {

        return m_endPattern;
    }

    /**
     * Returns the imageGallery.<p>
     *
     * @return the imageGallery
     */
    public String getImageGallery() {

        return m_imageGallery;
    }

    /**
     * Returns the inputDir.<p>
     *
     * @return the inputDir
     */
    public String getInputDir() {

        return m_inputDir;
    }

    /**
     * Returns the keepBrokenLinks.<p>
     *
     * @return the keepBrokenLinks
     */
    public String getKeepBrokenLinks() {

        return m_keepBrokenLinks;
    }

    /**
     * Returns the linkGallery.<p>
     *
     * @return the linkGallery
     */
    public String getLinkGallery() {

        return m_linkGallery;
    }

    /**
     * Returns the locale.<p>
     *
     * @return the locale
     */
    public String getLocale() {

        return m_locale;
    }

    /**
     * Returns the overwrite.<p>
     *
     * @return the overwrite
     */
    public String getOverwrite() {

        return m_overwrite;
    }

    /**
     * Returns the startPattern.<p>
     *
     * @return the startPattern
     */
    public String getStartPattern() {

        return m_startPattern;
    }

    /**
     * Returns the template.<p>
     *
     * @return the template
     */
    public String getTemplate() {

        return m_template;
    }

    /**
     * Sets the destinationDir.<p>
     *
     * @param destinationDir the destinationDir to set
     */
    public void setDestinationDir(String destinationDir) {

        m_destinationDir = destinationDir;
    }

    /**
     * Sets the downloadGallery.<p>
     *
     * @param downloadGallery the downloadGallery to set
     */
    public void setDownloadGallery(String downloadGallery) {

        m_downloadGallery = downloadGallery;
    }

    /**
     * Sets the element.<p>
     *
     * @param element the element to set
     */
    public void setElement(String element) {

        m_element = element;
    }

    /**
     * Sets the encoding.<p>
     *
     * @param encoding the encoding to set
     */
    public void setEncoding(String encoding) {

        m_encoding = encoding;
    }

    /**
     * Sets the endPattern.<p>
     *
     * @param endPattern the endPattern to set
     */
    public void setEndPattern(String endPattern) {

        m_endPattern = endPattern;
    }

    /**
     * Sets the imageGallery.<p>
     *
     * @param imageGallery the imageGallery to set
     */
    public void setImageGallery(String imageGallery) {

        m_imageGallery = imageGallery;
    }

    /**
     * Sets the inputDir.<p>
     *
     * @param inputDir the inputDir to set
     */
    public void setInputDir(String inputDir) {

        m_inputDir = inputDir;
    }

    /**
     * Sets the keepBrokenLinks.<p>
     *
     * @param keepBrokenLinks the keepBrokenLinks to set
     */
    public void setKeepBrokenLinks(String keepBrokenLinks) {

        m_keepBrokenLinks = keepBrokenLinks;
    }

    /**
     * Sets the linkGallery.<p>
     *
     * @param linkGallery the linkGallery to set
     */
    public void setLinkGallery(String linkGallery) {

        m_linkGallery = linkGallery;
    }

    /**
     * Sets the locale.<p>
     *
     * @param locale the locale to set
     */
    public void setLocale(String locale) {

        m_locale = locale;
    }

    /**
     * Sets the overwrite.<p>
     *
     * @param overwrite the overwrite to set
     */
    public void setOverwrite(String overwrite) {

        m_overwrite = overwrite;
    }

    /**
     * Sets the startPattern.<p>
     *
     * @param startPattern the startPattern to set
     */
    public void setStartPattern(String startPattern) {

        m_startPattern = startPattern;
    }

    /**
     * Sets the template.<p>
     *
     * @param template the template to set
     */
    public void setTemplate(String template) {

        m_template = template;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {

        StringBuffer result = new StringBuffer();
        result.append("\n DestinationDir:").append(getDestinationDir());
        result.append("\n InputDir:").append(getInputDir());
        result.append("\n DownloadGallery:").append(getDownloadGallery());
        result.append("\n ImageGallery:").append(getImageGallery());
        result.append("\n LinkGallery:").append(getLinkGallery());
        result.append("\n Template:").append(getTemplate());
        result.append("\n Element:").append(getElement());
        result.append("\n Locale:").append(getLocale());
        result.append("\n Encoding:").append(getEncoding());
        result.append("\n StartPattern:").append(getStartPattern());
        result.append("\n EndPattern:").append(getEndPattern());
        result.append("\n Overwrite:").append(getOverwrite());
        result.append("\n KeepBrokenLinks:").append(getKeepBrokenLinks());
        return result.toString();
    }
}