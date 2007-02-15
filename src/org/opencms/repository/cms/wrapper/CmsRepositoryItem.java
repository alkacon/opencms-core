/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/repository/cms/wrapper/Attic/CmsRepositoryItem.java,v $
 * Date   : $Date: 2007/02/15 15:54:20 $
 * Version: $Revision: 1.1.4.2 $
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

package org.opencms.repository.cms.wrapper;

import org.opencms.file.CmsFile;
import org.opencms.file.CmsPropertyDefinition;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsResourceFilter;
import org.opencms.file.types.I_CmsResourceType;
import org.opencms.file.wrapper.CmsObjectWrapper;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.repository.I_CmsRepositoryItem;

/**
 * Represents a single entry in the repository. In the context of OpenCms
 * this means a single resource.<p>
 * 
 * @author Peter Bonrad
 * 
 * @version $Revision: 1.1.4.2 $
 * 
 * @since 6.5.6
 */
public class CmsRepositoryItem implements I_CmsRepositoryItem {

    /** The content of the item as a byte array. */
    private byte[] m_content = null;

    /** The mime type of the item. */
    private String m_mimeType = null;

    /** The CmsResource the CmsRepositoryItem is for. */
    private CmsResource m_resource;

    /** The actual CmsObjectWrapper. */
    private CmsObjectWrapper m_cms;

    /**
     * Construct a new CmsRepositoryItem initialized with the CmsResource to use
     * and the CmsObject needed for some operations.<p>
     * 
     * @param res The CmsResource this CmsRepositoryItem is used for
     * @param cms The actual CmsObjectWrapper
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
                m_mimeType = OpenCms.getResourceManager().getResourceType(m_resource.getTypeId()).getInternalMimeType();
                String encoding = m_cms.readPropertyObject(
                    m_resource,
                    CmsPropertyDefinition.PROPERTY_CONTENT_ENCODING,
                    true).getValue(OpenCms.getSystemInfo().getDefaultEncoding());

                if (m_mimeType == null) {
                    m_mimeType = OpenCms.getResourceManager().getMimeType(
                        m_resource.getRootPath(),
                        encoding,
                        I_CmsResourceType.MIME_TYPE_TEXT_PLAIN);
                } else {
                    if ((encoding != null) && m_mimeType.startsWith("text") && (m_mimeType.indexOf("charset") == -1)) {
                        m_mimeType += "; charset=" + encoding;
                    }
                }
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
