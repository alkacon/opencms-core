/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapCacheSettings.java,v $
 * Date   : $Date: 2009/12/14 09:41:04 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) 2002 - 2009 Alkacon Software GmbH (http://www.alkacon.com)
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
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.xml.sitemap;

/**
 * The cache settings for the sitemap.<p>
 * 
 * @author Michael Moossen
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 7.9.2
 */
public class CmsSitemapCacheSettings {

    /** Default size for documents caches. */
    private static final int DEFAULT_DOCUMENT_SIZE = 32;

    /** Default size for file caches. */
    private static final int DEFAULT_FILE_SIZE = 32;

    /** Default size for missing URI caches. */
    private static final int DEFAULT_MISSING_URI_SIZE = 4096;

    /** Default size for properties caches. */
    private static final int DEFAULT_PROPERTY_SIZE = 4096;

    /** Default size for URI caches. */
    private static final int DEFAULT_URI_SIZE = 4096;

    /** The size of the documents offline cache. */
    private int m_documentOfflineSize;

    /** The size of the documents online cache. */
    private int m_documentOnlineSize;

    /** The size of the file offline cache. */
    private int m_fileOfflineSize;

    /** The size of the file online cache. */
    private int m_fileOnlineSize;

    /** The size of the missing URI offline cache. */
    private int m_missingUriOfflineSize;

    /** The size of the missing URI online cache. */
    private int m_missingUriOnlineSize;

    /** The size of the properties offline cache. */
    private int m_propertyOfflineSize;

    /** The size of the properties online cache. */
    private int m_propertyOnlineSize;

    /** The size of the URI offline cache. */
    private int m_uriOfflineSize;

    /** The size of the URI online cache. */
    private int m_uriOnlineSize;

    /**
     * Default constructor.<p>
     */
    public CmsSitemapCacheSettings() {

        super();
    }

    /**
     * Returns the size of the document offline cache.<p>
     * 
     * @return the size of the document offline cache
     */
    public int getDocumentOfflineSize() {

        if (m_documentOfflineSize <= 0) {
            return DEFAULT_DOCUMENT_SIZE;
        }
        return m_documentOfflineSize;
    }

    /**
     * Returns the size of the document online cache.<p>
     * 
     * @return the size of the document online cache
     */
    public int getDocumentOnlineSize() {

        if (m_documentOnlineSize <= 0) {
            return DEFAULT_DOCUMENT_SIZE;
        }
        return m_documentOnlineSize;
    }

    /**
     * Returns the size of the file offline cache.<p>
     * 
     * @return the size of the file offline cache
     */
    public int getFileOfflineSize() {

        if (m_fileOfflineSize <= 0) {
            return DEFAULT_FILE_SIZE;
        }
        return m_fileOfflineSize;
    }

    /**
     * Returns the size of the file online cache.<p>
     * 
     * @return the size of the file online cache
     */
    public int getFileOnlineSize() {

        if (m_fileOnlineSize <= 0) {
            return DEFAULT_FILE_SIZE;
        }
        return m_fileOnlineSize;
    }

    /**
     * Returns the size of the missing URI offline cache.<p>
     * 
     * @return the size of the missing URI offline cache
     */
    public int getMissingUriOfflineSize() {

        if (m_missingUriOfflineSize <= 0) {
            return DEFAULT_MISSING_URI_SIZE;
        }
        return m_missingUriOfflineSize;
    }

    /**
     * Returns the size of the missing URI online cache.<p>
     * 
     * @return the size of the missing URI online cache
     */
    public int getMissingUriOnlineSize() {

        if (m_missingUriOnlineSize <= 0) {
            return DEFAULT_MISSING_URI_SIZE;
        }
        return m_missingUriOnlineSize;
    }

    /**
     * Returns the size of the properties offline cache.<p>
     * 
     * @return the size of the properties offline cache
     */
    public int getPropertyOfflineSize() {

        if (m_propertyOfflineSize <= 0) {
            return DEFAULT_PROPERTY_SIZE;
        }
        return m_propertyOfflineSize;
    }

    /**
     * Returns the size of the properties online cache.<p>
     * 
     * @return the size of the properties online cache
     */
    public int getPropertyOnlineSize() {

        if (m_propertyOnlineSize <= 0) {
            return DEFAULT_PROPERTY_SIZE;
        }
        return m_propertyOnlineSize;
    }

    /**
     * Returns the size of the URI offline cache.<p>
     * 
     * @return the size of the URI offline cache
     */
    public int getUriOfflineSize() {

        if (m_uriOfflineSize <= 0) {
            return DEFAULT_URI_SIZE;
        }
        return m_uriOfflineSize;
    }

    /**
     * Returns the size of the URI online cache.<p>
     * 
     * @return the size of the URI online cache
     */
    public int getUriOnlineSize() {

        if (m_uriOnlineSize <= 0) {
            return DEFAULT_URI_SIZE;
        }
        return m_uriOnlineSize;
    }

    /**
     * Sets the size of the cache for sitemap documents.<p>
     *
     * @param size the size of the cache for sitemap documents
     */
    public void setDocumentOfflineSize(String size) {

        m_documentOfflineSize = getIntValue(size, DEFAULT_DOCUMENT_SIZE);
    }

    /**
     * Sets the size of the cache for sitemap documents.<p>
     *
     * @param size the size of the cache for sitemap documents
     */
    public void setDocumentOnlineSize(String size) {

        m_documentOnlineSize = getIntValue(size, DEFAULT_DOCUMENT_SIZE);
    }

    /**
     * Sets the size of the cache for sitemap files.<p>
     *
     * @param size the size of the cache for sitemap files
     */
    public void setFileOfflineSize(String size) {

        m_fileOfflineSize = getIntValue(size, DEFAULT_FILE_SIZE);
    }

    /**
     * Sets the size of the cache for sitemap files.<p>
     *
     * @param size the size of the cache for sitemap files
     */
    public void setFileOnlineSize(String size) {

        m_fileOnlineSize = getIntValue(size, DEFAULT_FILE_SIZE);
    }

    /**
     * Sets the size of the cache for missing URIs.<p>
     *
     * @param size the size of the cache for missing URIs
     */
    public void setMissingUriOfflineSize(String size) {

        m_missingUriOfflineSize = getIntValue(size, DEFAULT_MISSING_URI_SIZE);
    }

    /**
     * Sets the size of the cache for missing URIs.<p>
     *
     * @param size the size of the cache for missing URIs
     */
    public void setMissingUriOnlineSize(String size) {

        m_missingUriOnlineSize = getIntValue(size, DEFAULT_MISSING_URI_SIZE);
    }

    /**
     * Sets the size of the cache for sitemap properties.<p>
     *
     * @param size the size of the cache for sitemap properties
     */
    public void setPropertyOfflineSize(String size) {

        m_propertyOfflineSize = getIntValue(size, DEFAULT_PROPERTY_SIZE);
    }

    /**
     * Sets the size of the cache for sitemap properties.<p>
     *
     * @param size the size of the cache for sitemap properties
     */
    public void setPropertyOnlineSize(String size) {

        m_propertyOnlineSize = getIntValue(size, DEFAULT_PROPERTY_SIZE);
    }

    /**
     * Sets the size of the cache for URIs.<p>
     *
     * @param size the size of the cache for URIs
     */
    public void setUriOfflineSize(String size) {

        m_uriOfflineSize = getIntValue(size, DEFAULT_URI_SIZE);
    }

    /**
     * Sets the size of the cache for URIs.<p>
     *
     * @param size the size of the cache for URIs
     */
    public void setUriOnlineSize(String size) {

        m_uriOnlineSize = getIntValue(size, DEFAULT_URI_SIZE);
    }

    /**
     * Turns a string into an integer.<p>
     * 
     * @param str the string to be converted
     * @param defaultValue a default value to be returned in case the string could not be parsed or the parsed integer value is <= 0
     * 
     * @return the integer value of the string
     */
    private int getIntValue(String str, int defaultValue) {

        try {
            int intValue = Integer.parseInt(str);
            return (intValue > 0) ? intValue : defaultValue;
        } catch (NumberFormatException e) {
            // intentionally left blank
        }
        return defaultValue;
    }
}
