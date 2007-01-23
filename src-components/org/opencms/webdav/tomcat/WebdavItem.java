/*
 * File   : $Source: /alkacon/cvs/opencms/src-components/org/opencms/webdav/tomcat/Attic/WebdavItem.java,v $
 * Date   : $Date: 2007/01/23 16:58:11 $
 * Version: $Revision: 1.1.2.2 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2005 Alkacon Software GmbH (http://www.alkacon.com)
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

package org.opencms.webdav.tomcat;

import org.opencms.webdav.I_CmsWebdavItem;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * This class represents a WebDAV resource item to work with the Tomcat to write
 * to the hard drive.
 * 
 * @author Peter Bonrad
 */
public class WebdavItem implements I_CmsWebdavItem {

    private boolean m_collection;

    private byte[] m_content;

    private long m_contentLength;

    private long m_creationDate;

    private long m_lastModifiedDate;

    private String m_mimeType;

    private String m_name;

    /**
     * @see org.opencms.webdav.I_CmsWebdavItem#getContent()
     */
    public byte[] getContent() {

        return m_content;
    }

    /**
     * @see org.opencms.webdav.I_CmsWebdavItem#getContentLength()
     */
    public long getContentLength() {

        return m_contentLength;
    }

    /**
     * @see org.opencms.webdav.I_CmsWebdavItem#getCreationDate()
     */
    public long getCreationDate() {

        return m_creationDate;
    }

    /**
     * @see org.opencms.webdav.I_CmsWebdavItem#getLastModifiedDate()
     */
    public long getLastModifiedDate() {

        return m_lastModifiedDate;
    }

    /**
     * @see org.opencms.webdav.I_CmsWebdavItem#getMimeType()
     */
    public String getMimeType() {

        return m_mimeType;
    }

    /**
     * @see org.opencms.webdav.I_CmsWebdavItem#getName()
     */
    public String getName() {

        return m_name;
    }

    /**
     * @see org.opencms.webdav.I_CmsWebdavItem#getStreamContent()
     */
    public InputStream getStreamContent() {

        return new ByteArrayInputStream(m_content);
    }

    /**
     * @see org.opencms.webdav.I_CmsWebdavItem#isCollection()
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
