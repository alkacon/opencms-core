/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/repository/cms/Attic/CmsRepositoryItem.java,v $
 * Date   : $Date: 2007/01/25 09:09:27 $
 * Version: $Revision: 1.1.2.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2006 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.repository.cms;

import org.opencms.repository.I_CmsRepositoryItem;


/**
 *
 */
public class CmsRepositoryItem implements I_CmsRepositoryItem {

    /** Is the item a collection? */
    private boolean m_collection;

    /** The content of the item as a byte array. */
    private byte[] m_content;

    /** The length of the content of the item. */
    private long m_contentLength;

    /** The creation date of the item. */
    private long m_creationDate;

    /** The date of the last modification of the item. */
    private long m_lastModifiedDate;

    /** The mime type of the item. */
    private String m_mimeType;

    /** The name of the item. */
    private String m_name;
    
    /**
     * 
     */
    public CmsRepositoryItem() {

        // TODO Auto-generated constructor stub

    }

    /**
     * @see org.opencms.repository.I_CmsRepositoryItem#getContent()
     */
    public byte[] getContent() {

        return m_content;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositoryItem#getContentLength()
     */
    public long getContentLength() {

        return m_contentLength;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositoryItem#getCreationDate()
     */
    public long getCreationDate() {

        return m_creationDate;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositoryItem#getLastModifiedDate()
     */
    public long getLastModifiedDate() {

        return m_lastModifiedDate;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositoryItem#getMimeType()
     */
    public String getMimeType() {

        return m_mimeType;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositoryItem#getName()
     */
    public String getName() {

        return m_name;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositoryItem#isCollection()
     */
    public boolean isCollection() {

        return m_collection;
    }

    /**
     * Sets the collection.<p>
     *
     * @param collection the collection to set
     */
    public void setCollection(boolean collection) {

        m_collection = collection;
    }

    /**
     * Sets the content.<p>
     *
     * @param content the content to set
     */
    public void setContent(byte[] content) {

        m_content = content;
    }

    /**
     * Sets the contentLength.<p>
     *
     * @param contentLength the contentLength to set
     */
    public void setContentLength(long contentLength) {

        m_contentLength = contentLength;
    }

    /**
     * Sets the creationDate.<p>
     *
     * @param creationDate the creationDate to set
     */
    public void setCreationDate(long creationDate) {

        m_creationDate = creationDate;
    }

    /**
     * Sets the lastModifiedDate.<p>
     *
     * @param lastModifiedDate the lastModifiedDate to set
     */
    public void setLastModifiedDate(long lastModifiedDate) {

        m_lastModifiedDate = lastModifiedDate;
    }

    /**
     * Sets the mimeType.<p>
     *
     * @param mimeType the mimeType to set
     */
    public void setMimeType(String mimeType) {

        m_mimeType = mimeType;
    }

    /**
     * Sets the name.<p>
     *
     * @param name the name to set
     */
    public void setName(String name) {

        m_name = name;
    }
}
