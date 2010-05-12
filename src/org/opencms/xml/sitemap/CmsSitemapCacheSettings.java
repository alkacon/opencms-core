/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/xml/sitemap/Attic/CmsSitemapCacheSettings.java,v $
 * Date   : $Date: 2010/05/12 09:19:10 $
 * Version: $Revision: 1.4 $
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
 * @version $Revision: 1.4 $
 * 
 * @since 7.9.2
 */
public class CmsSitemapCacheSettings {

    /** Default size for documents caches. */
    private static final int DEFAULT_DOCUMENT_SIZE = 32;

    /** The size of the documents offline cache. */
    private int m_documentOfflineSize;

    /** The size of the documents online cache. */
    private int m_documentOnlineSize;

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
