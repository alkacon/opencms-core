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

package org.opencms.repository;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.wrapper.CmsObjectWrapper;
import org.opencms.loader.CmsResourceManager;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;

/**
 * Represents a single entry in the repository. In the context of OpenCms
 * this means a single {@link CmsResource}.<p>
 *
 * @since 6.5.6
 */
public class CmsRepositoryItem implements I_CmsRepositoryItem {

    /** The actual {@link CmsObjectWrapper}. */
    private CmsObjectWrapper m_cms;

    /** The content of the item as a byte array. */
    private byte[] m_content;

    /** The MIME type of the item. */
    private String m_mimeType;

    /** The {@link CmsResource} the CmsRepositoryItem is for. */
    private CmsResource m_resource;

    /**
     * Construct a new CmsRepositoryItem initialized with the {@link CmsResource}
     * to use and the {@link CmsObjectWrapper} needed for some operations.<p>
     *
     * @param res the CmsResource this CmsRepositoryItem is used for
     * @param cms the actual CmsObjectWrapper
     */
    public CmsRepositoryItem(CmsResource res, CmsObjectWrapper cms) {

        m_resource = res;
        m_cms = cms;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositoryItem#getContent()
     */
    public byte[] getContent() {

        if (!m_resource.isFile()) {
            return null;
        }

        if (m_content == null) {
            try {
                String filename = m_cms.getSitePath(m_resource);

                // read and return the file
                CmsFile file = m_cms.readFile(filename, CmsResourceFilter.IGNORE_EXPIRATION);

                m_content = file.getContents();
            } catch (CmsException ex) {
                // noop
            }
        }

        return m_content;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositoryItem#getContentLength()
     */
    public long getContentLength() {

        return m_resource.getLength();
    }

    /**
     * @see org.opencms.repository.I_CmsRepositoryItem#getCreationDate()
     */
    public long getCreationDate() {

        return m_resource.getDateCreated();
    }

    /**
     * @see org.opencms.repository.I_CmsRepositoryItem#getLastModifiedDate()
     */
    public long getLastModifiedDate() {

        return m_resource.getDateLastModified();
    }

    /**
     * @see org.opencms.repository.I_CmsRepositoryItem#getMimeType()
     */
    public String getMimeType() {

        if (!m_resource.isFile()) {
            return null;
        }

        if (m_mimeType == null) {
            try {
                String encoding = m_cms.readPropertyObject(
                    m_resource,
                    CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING,
                    true).getValue(OpenCms.getSystemInfo().getDefaultEncoding());

                m_mimeType = OpenCms.getResourceManager().getMimeType(
                    m_resource.getRootPath(),
                    encoding,
                    CmsResourceManager.MIMETYPE_TEXT);

            } catch (CmsException ex) {
                // noop
            }
        }

        return m_mimeType;
    }

    /**
     * @see org.opencms.repository.I_CmsRepositoryItem#getName()
     */
    public String getName() {

        return m_cms.getRequestContext().removeSiteRoot(m_resource.getRootPath());
    }

    /**
     * @see org.opencms.repository.I_CmsRepositoryItem#isCollection()
     */
    public boolean isCollection() {

        return m_resource.isFolder();
    }

}
